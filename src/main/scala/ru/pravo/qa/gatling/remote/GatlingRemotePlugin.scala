package ru.pravo.qa.gatling.remote

import ru.pravo.qa.gatling.remote.GatlingRemoteTasks._
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
    // I think this is a bad code, but mappings in (Compile, packageBin) ~= removesFileFromJar(config).value do not work.
    // Also we need to have an ability to determine where files are instead of removing them from main and test
    mappings in (Compile, packageBin) := (mappings in (Compile, packageBin)).value.filter { case (file, toPath) ⇒
      val configFiles = configurationFiles.value ++ userFilesDataFiles.value
      configFiles.contains(file)
    },
    mappings in (config, packageBin) := (mappings in (Compile, packageBin)).value.filter { case (file, toPath) ⇒
      val configFiles = configurationFiles.value ++ userFilesDataFiles.value
      configFiles.contains(file)
    },
    testOnlyRemote := testOnlyRemoteTask(config).evaluated,
    mappings in Universal ++= generateMapping(config).value,
    gatlingConfigFilePath := (resourceDirectory in config).value / "gatling.conf",
    gatlingAkkaConfigFilePath := (resourceDirectory in config).value / "gatling-akka.conf",
    gatlingRemoteConfigFilePath := (resourceDirectory in config).value / "gatling-remote.conf",
    logbackConfigFilePath := (resourceDirectory in config).value / "logback.xml",
    grafiteRootPathPrefix := "gatling",
    remoteWorkDirectoryPath := "/tmp",
    deployTimeoutDuration := Duration.Inf,
    runTimeoutDuration := Duration.Inf,
    configurationFiles := ConfigurationFiles.defaultConfigurationFiles(config).value,
    userFilesDataFiles := Seq()
  )
}
