import sbtassembly.AssemblyPlugin.defaultShellScript

name := "AWSDataGenerator"

version := "0.1"

scalaVersion := "2.12.4"

assemblyJarName in assembly := "awsdata"

mainClass in assembly := Some("com.satoshihirose.awsdata.AWSDataGenerator")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript))

libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.9"

libraryDependencies += "it.bitbl" %% "scala-faker" % "0.4"

val awsSdkVersion = "1.11.283"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
libraryDependencies += "com.amazonaws" % "aws-java-sdk-core" % awsSdkVersion

libraryDependencies += "org.scalatra.scalate" %% "scalate-core" % "1.8.0"

libraryDependencies += "com.github.azakordonets" % "fabricator_2.12" % "2.1.5"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.3"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

unmanagedResourceDirectories in Compile += { baseDirectory.value / "src" / "main" / "resources" / "data_files" }

