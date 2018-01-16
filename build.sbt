name := "gatling-remote-sbt"

organization := "ru.pravo"

version := "2.2.2.3"

sbtPlugin := true

addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.3.2")
addSbtPlugin("io.gatling" %% "gatling-sbt" % "2.2.2")

licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
publishMavenStyle := false
bintrayRepository := "sbt-plugins"
bintrayOrganization := Some("pravoru")

libraryDependencies ++= Seq(
  "com.hierynomus" % "sshj" % "0.23.0",
  "com.typesafe" % "config" % "1.3.2"
)

crossSbtVersions := Seq("1.0.0", "0.13.16")