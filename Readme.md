# Gatling remote sbt plugin 

## Goal

Gatling is excellent load test framework, but is has some limitations. The biggest limitation is absence of remote execution. 
This plugin brings ability to run simulation remotely through ssh.

## Dependencies 

* [gatling-sbt](https://github.com/gatling/gatling-sbt)
* [sbt-native-packager](https://github.com/sbt/sbt-native-packager)
* [sshj](https://github.com/hierynomus/sshj)

## Instalation

`plugins.sbt`
```scala
addSbtPlugin("ru.pravo" % "gatling-remote-sbt" % "0.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.1")
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
