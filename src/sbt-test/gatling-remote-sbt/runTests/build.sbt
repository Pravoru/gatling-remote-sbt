enablePlugins(GatlingPlugin, JavaAppPackaging, GatlingRemotePlugin)

scalaVersion := "2.11.8"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.0"
libraryDependencies += "io.gatling" % "gatling-test-framework" % "2.2.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5"
