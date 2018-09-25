name := "gatling-remote-sbt"

organization := "ru.pravo"

version := "3.0.0.0-RC1"

sbtPlugin := true

addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.3.6")
addSbtPlugin("io.gatling" %% "gatling-sbt" % "2.2.2")

enablePlugins(SbtPlugin)

licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
publishMavenStyle := false
bintrayRepository := "sbt-plugins"
bintrayOrganization := Some("pravoru")

libraryDependencies ++= Seq(
  "com.hierynomus" % "sshj" % "0.26.0",
  "com.typesafe" % "config" % "1.3.3"
)

crossSbtVersions := Seq("1.0.0", "0.13.17")