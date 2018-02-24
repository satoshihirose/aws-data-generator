# aws-data-generator
a tool generates dummy data on AWS resources
## Setup
download and build
```
$ sbt assembly
```
## How to use
now only supported for S3
```
$ ./target/scala-2.12/awsdata s3 mybucketname mys3path --partitioning -n 100
successfully put object to s3://mybucketname/mys3path/year=2018/month=02/day=24/20180224.txt
successfully put object to s3://mybucketname/mys3path/year=2018/month=02/day=23/20180223.txt
successfully put object to s3://mybucketname/mys3path/year=2018/month=02/day=22/20180222.txt
successfully put object to s3://mybucketname/mys3path/year=2018/month=02/day=21/20180221.txt
successfully put object to s3://mybucketname/mys3path/year=2018/month=02/day=20/20180220.txt
...
```
## Dummy data format
now only supported for CSV file format
### Columns
$name,$email,$ipAddress,$phoneNumber,$company,$rat,$let,$createdAt,$timestamp,$expired
### Example
```
$ head ./20180224.txt                                                                                                         
Freddie Rogahn IV,iv_freddie_rogahn@harris.org,8.198.9.180,548.338.7084 x6383,Lowe Group,-19.060074800315093,-34.891407190715825,2018-02-24T12:47:58Z,1519476478851,true
Matteo Erdman,erdman.matteo@becker.biz,121.255.104.81,(294)403-4847,Funk, Fahey and Kohler,-10.357129393880768,155.0825569778416,2018-02-24T12:47:59Z,1519476479094,false
Bernard Little,bernard_little@maggioadams.org,22.212.67.225,(527)371-7124 x713,Auer-Conroy,26.86255973289248,20.291732626771818,2018-02-24T12:47:59Z,1519476479099,true
Brisa Mann DVM,brisa.dvm.mann@streich.info,15.5.114.17,(979)725-5774,Pacocha-Roberts,-53.88939517567083,140.98607168037478,2018-02-24T12:47:59Z,1519476479106,true
Harvey Feil,harveyfeil@runte.net,127.189.8.76,847.670.8377 x2808,Powlowski-Morar,4.993006883258943,-105.25005441137455,2018-02-24T12:47:59Z,1519476479109,true
```
