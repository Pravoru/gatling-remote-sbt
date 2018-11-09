enablePlugins(GatlingPlugin, JavaAppPackaging, GatlingRemotePlugin)

//scalaVersion := "2.12.3"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.0"
libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4"

gatlingConfigFilePath in Gatling := (resourceDirectory in Compile).value / "gatling.conf"
gatlingAkkaConfigFilePath in Gatling := (resourceDirectory in Compile).value / "gatling-akka.conf"
gatlingRemoteConfigFilePath in Gatling := (resourceDirectory in Compile).value / "gatling-remote.conf"
logbackConfigFilePath in Gatling := (resourceDirectory in Compile).value / "logback.xml"
userFilesDataFiles in Gatling ++= Seq(
  (resourceDirectory in Compile).value / "users.csv",
  (resourceDirectory in Compile).value / "files"
)
configurationFiles in Gatling ++= Seq(
  (resourceDirectory in Compile).value / "application.conf"
)