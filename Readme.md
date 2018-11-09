# Gatling remote sbt plugin 

## Goal

Gatling is excellent load test framework, but is has some limitations. The biggest limitation is absence of remote execution. 
This plugin brings ability to run simulation remotely through ssh.

## Dependencies 

* [gatling-sbt](https://github.com/gatling/gatling-sbt)
* [sbt-native-packager](https://github.com/sbt/sbt-native-packager)
* [sshj](https://github.com/hierynomus/sshj)

## Installation
We mimic gatling-sbt versioning. For our purposes we use fourth digit in version number. 

`plugins.sbt`
```scala
addSbtPlugin("ru.pravo" %% "gatling-remote-sbt" % "3.0.0.0")
addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.3.6")
addSbtPlugin("io.gatling" %% "gatling-sbt" % "3.0.0")
```

`build.sbt`
```scala
enablePlugins(GatlingPlugin, JavaAppPackaging, GatlingRemotePlugin)
```

`gatling-remote.conf`
```
hosts = [
  {
    host = "localhost",
    login = "root",
    password = "root",
    port = 2222
  }
]
```

## Configuration

There are multiple setting for configuration:

Setting | Description | Default 
------- | ----------- | -------
|`gatlingConfigFilePath`|Path to `gatling.conf` file|`src/test/resources/gatling.conf`|
|`gatlingAkkaConfigFilePath`|Path to `gatling-akka.conf` file|`src/test/resources/gatling-akka.conf`|
|`gatlingRemoteConfigFilePath`|Path to `gatling-remote.conf` file|`src/test/resources/gatling-remote.conf`|
|`logbackConfigFilePath`|Path to `logback.xml` file|`src/test/resources/logback.xml`|
|`remoteWorkDirectoryPath`|Path to work directory on remote server|`/tmp`|
|`deployTimeoutDuration`|Timeout for deploying operation|`Infinite`|
|`runTimeoutDuration`|Timeout for running operation|`Infinite`|
|`grafiteRootPathPrefix`|Root path that will be overwritten in run.sh|`gatling`|
|`configurationFiles`|Configuration files that will be deployed into /conf directory|`Seq(gatling.conf, gatling-akka.conf, gatling-remote.conf, logback.xml)`|
|`userFilesDataFiles`|User files that will be deployed into /user-files/body directory|`Seq()`|

You can override any of these settings. For example 

```scala
gatlingConfigFilePath in Gatling := (resourceDirectory in Compile).value / "gatling.conf"
```

will force plugin to take `gatling.conf` from `/src/resources/gatling.conf`

## Using

Run simulation
```bash
sbt gatling:testOnlyRemote basic.BasicExampleSimulationFails
```

Get assembled project 
```bash
sbt gatling:assembleProject
```
