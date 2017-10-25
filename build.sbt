name := "gatling-remote-sbt"

organization := "ru.pravo"

version := "2.2.2.1"

scalaVersion := "2.10.6"

sbtPlugin := true

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.2")

licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
publishMavenStyle := false
bintrayRepository := "sbt-plugins"
bintrayOrganization := Some("pravoru")

libraryDependencies ++= Seq(
  "com.hierynomus" % "sshj" % "0.21.1",
  "com.typesafe" % "config" % "1.3.1"
)