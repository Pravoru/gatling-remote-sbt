{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("ru.pravo" % "gatling-remote-sbt" % pluginVersion)
}
addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.3.2")
addSbtPlugin("io.gatling" %% "gatling-sbt" % "3.0.0")

