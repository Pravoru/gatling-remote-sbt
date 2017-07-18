package com.pravoru.qa.gatling.remote

import com.pravoru.qa.gatling.remote.GatlingRemoteTasks._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stage
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.GatlingPlugin
import sbt.Keys._
import sbt._

import scala.concurrent.duration.Duration

object GatlingRemotePlugin extends AutoPlugin {

  override def requires: Plugins = GatlingPlugin && JavaAppPackaging

  val autoImport = GatlingRemoteKeys

  import autoImport._

  def gatlingRemoteSettings: Seq[Def.Setting[_]] = inConfig(Gatling)(gatlingRemoteBaseSettings(Gatling))

  override def projectSettings: Seq[Def.Setting[_]] = gatlingRemoteSettings

  private def gatlingRemoteBaseSettings(config: Configuration) = Seq(
    assembleProject := (stage in Universal).value,
    testOnlyRemote := testOnlyRemoteTask(config).evaluated,
    mappings in Universal ++= generateMapping(config).value,
    gatlingConfigFilePath := (resourceDirectory in config).value / "gatling.conf",
    gatlingAkkaConfigFilePath := (resourceDirectory in config).value / "gatling-akka.conf",
    gatlingRemoteConfigFilePath := (resourceDirectory in config).value / "gatling-remote.conf",
    logbackConfigFilePath := (resourceDirectory in config).value / "logback.xml",
    grafiteRootPathPrefix := "gatling",
    remoteWorkDirectoryPath := "/tmp",
    deployTimeoutDuration := Duration.Inf,
    runTimeoutDuration := Duration.Inf
  )
}
