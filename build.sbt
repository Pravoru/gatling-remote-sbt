name := "gatling-remote-sbt"

organization := "ru.pravo"

version := "0.1"

scalaVersion := "2.10.6"

sbtPlugin := true

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.1")

libraryDependencies ++= Seq(
  "com.hierynomus" % "sshj" % "0.21.1",
  "com.typesafe" % "config" % "1.3.1"
)