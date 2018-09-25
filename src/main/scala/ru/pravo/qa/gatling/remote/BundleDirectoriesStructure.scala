package ru.pravo.qa.gatling.remote

import com.typesafe.config.ConfigFactory
import sbt.File

object BundleDirectoriesStructure {
  def apply(gatlingConf: File) = new BundleDirectoriesStructure(gatlingConf)
}

class BundleDirectoriesStructure(gatlingConf: File) {
  private def gatlingConfig = ConfigFactory.parseFile(gatlingConf)

  val configurationDirectory: String = "conf"

  val userFilesDataDirectory: String = {
    val path = "gatling.core.directory.resources"
    if (gatlingConfig.hasPath(path)) {
      gatlingConfig.getString(path)
    } else {
      "user-files/resources"
    }
  }
}
