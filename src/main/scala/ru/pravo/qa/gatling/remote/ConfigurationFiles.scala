package ru.pravo.qa.gatling.remote

import java.io.File

import ru.pravo.qa.gatling.remote.GatlingRemoteKeys.{gatlingAkkaConfigFilePath, gatlingConfigFilePath, gatlingRemoteConfigFilePath, logbackConfigFilePath}
import sbt.{Configuration, Def, Task}

object ConfigurationFiles {
  def defaultConfigurationFiles(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
    Seq(
      (gatlingConfigFilePath in config).value,
      (gatlingAkkaConfigFilePath in config).value,
      (gatlingRemoteConfigFilePath in config).value,
      (logbackConfigFilePath in config).value
    )
  }

  def gatlingConfigFile(config: Configuration): Def.Initialize[Task[File]] = gatlingConfigFilePath in config
  def gatlingAkkaConfigFile(config: Configuration): Def.Initialize[Task[File]] = gatlingAkkaConfigFilePath in config
  def gatlingRemoteConfigFile(config: Configuration): Def.Initialize[Task[File]] = gatlingRemoteConfigFilePath in config
  def logbackConfigFile(config: Configuration): Def.Initialize[Task[File]] = logbackConfigFilePath in config
}
