package com.satoshihirose.awsdata

import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.util.{Date, TimeZone}

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}
import com.amazonaws.util.IOUtils
import org.apache.commons.codec.Charsets
import org.fusesource.scalate.TemplateEngine

import scala.concurrent.duration._
import scala.util.Random

object AWSDataGenerator extends App {

  val system = ActorSystem("awsdata")

  val parser = new scopt.OptionParser[Config]("awsdata") {
    head("AWS Data Generator")
    cmd("s3").action( (_, c) => c.copy(service = "s3") ).
      text("put data to S3")
      .children(
        arg[String]("bucket-name").action( (x, c) =>
          c.copy(bucketName = x) ).text("s3 bucket name"),
        arg[String]("path").action( (x, c) =>
          c.copy(path = x) ).text("s3 key name"),
        opt[String]("file-format").abbr("f").action( (x, c) =>
          c.copy(fileFormat = x) ).text("type of file format, supported formats are csv, json"),
        opt[String]("input-file").abbr("i").action( (x, c) =>
          c.copy(inputFile = x) ).text("input template file path"),
        opt[Int]("number-of-files").abbr("n").action( (x, c) =>
          c.copy(numberOfFiles = x) ).text("number of files"),
        opt[Unit]("partitioning").abbr("p").action( (x, c) =>
        c.copy(partitioning = true) ).text("enable partitioning"),
      )
    cmd("kinesis").action( (_, c) => c.copy(service = "kinesis") ).
      text("put data to Kinesis").children(
        arg[String]("<kinesis stream name>...")
      )
    help("help").text("show usage")
  }

  val config = parser.parse(args, Config()).getOrElse(Config())

  config match {
    case config: Config if config.service == "s3" => {
      val s3Actor = system.actorOf(Props(new S3Actor()))
      s3Actor ! config
      system.terminate()
    }
    case config: Config if config.service == "kinesis" => {
      val kinesisActor = system.actorOf(Props(new KinesisActor()))
      import system.dispatcher
      val cancellable = system.scheduler.schedule(0 seconds, 1 seconds, kinesisActor, config)
    }
    case _ => {
      parser.showUsage()
      system.terminate()
    }
  }

}

case class Config(service: String = "", bucketName: String = "", path: String = "", fileFormat: String = "csv", numberOfFiles: Int = 10, partitioning: Boolean = false, inputFile: String = "")

class S3Actor extends Actor with ActorLogging {

  def receive = {
    case config: Config if config.service == "s3" => {
      // put data file to s3
      val s3 = AmazonS3ClientBuilder.defaultClient()
      // TODO: what kind of data template? sample, elb logs like, cf logs like?
      (0 until config.numberOfFiles).foreach(i => {
        val usersStr = if (config.fileFormat == "json") {
          (0 until 100).map(_ => SampleUserData.random().toJSON).mkString("\n")
        } else if (config.fileFormat == "csv") {
          (0 until 100).map(_ => SampleUserData.random().toCSV).mkString("\n")
        } else {
          try {
          val alpha = fabricator.Alphanumeric()
          val calendar = fabricator.Calendar()
          val contact = fabricator.Contact()
          val finance = fabricator.Finance()
          val internet = fabricator.Internet()
          val userAgent = fabricator.UserAgent()
          val location = fabricator.Location()
          val mobile = fabricator.Mobile()
          val words = fabricator.Words()
          val bindings = Map("alpha" -> alpha, "calendar" -> calendar, "contact" -> contact, "finance" -> finance,
            "internet" -> internet, "userAgent" -> userAgent, "location" -> location, "mobile" -> mobile, "words" -> words)
          val engine = new TemplateEngine
          (0 until 100).map(_ => engine.layout(config.inputFile, bindings).replaceAll("\n", "")).mkString("\n")
          }  catch {
              case e: Exception =>
                log.error(e.getMessage)
                e.printStackTrace()
                log.info("error")
                ""
          }
        }

        val contentLength = IOUtils.toByteArray(strToInputStream(usersStr)).length
        val metadata = new ObjectMetadata()
        metadata.setContentLength(contentLength)

        val date = LocalDate.now().minusDays(i)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val objectName = date.format(formatter) + "." + config.fileFormat

        try {
          if (config.partitioning) {
            val year =  objectName.substring(0, 4)
            val month =  objectName.substring(4, 6)
            val day =  objectName.substring(6, 8)
            val keyName = s"${config.path}/year=$year/month=$month/day=$day/$objectName"
            s3.putObject(new PutObjectRequest(config.bucketName, keyName, strToInputStream(usersStr), metadata))
            log.info(s"successfully put object to s3://${config.bucketName}/$keyName")
          } else {
            val keyName = config.path + "/" + objectName
            s3.putObject(new PutObjectRequest(config.bucketName, keyName, strToInputStream(usersStr), metadata))
            log.info(s"successfully put object to s3://${config.bucketName}/$keyName")
          }
        } catch {
          case e: AmazonServiceException =>
            log.error(e.getErrorMessage)
          case e: Exception =>
            log.error(e.getMessage)
            e.printStackTrace()
        }
      })
    }
  }

  def strToInputStream(str: String) = new ByteArrayInputStream(str.getBytes(Charsets.UTF_8))

}

class KinesisActor extends Actor {

  // TODO: implement
  def receive = {
    case config: Config if config.service == "kinesis" => {
      // put data file to kinesis
      println(s"${SampleUserData.random().toCSV}")
    }
  }

}

case class SampleUserData(name: String, email: String, ipAddress: String, phoneNumber: String, company: String, lat: Double, lng: Double, createdAt: String, timestamp: Long, expired: Boolean) {

  // TODO: file formats selection? CSV? JSON? Parquet? ORC? ION?
  def toCSV = s"$name,$email,$ipAddress,$phoneNumber,$company,$lat,$lng,$createdAt,$timestamp,$expired"

  def toJSON = s"""{"name":"$name","email":"$email","ip_address":"$ipAddress","phone_number":"$phoneNumber","company":"$company",""" +
    s""""coords":{"lat":$lat,"lng":$lng},"created_at":"$createdAt","timestamp":$timestamp,"expired":$expired}"""

}

object SampleUserData {
  import faker._

  def random(): SampleUserData = {
    val now = new Date()
    val name = Name.name
    val coords = Geo.coords
    SampleUserData(name, Internet.email(name), Internet.ip_v4_address, PhoneNumber.phone_number, Company.name, coords._1, coords._2, dateToISO8601UTC(now), now.getTime, Random.nextBoolean)
  }

  def dateToISO8601UTC(date: Date): String = {
    val tz = TimeZone.getTimeZone("UTC")
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    df.setTimeZone(tz)
    df.format(date)
  }

}
