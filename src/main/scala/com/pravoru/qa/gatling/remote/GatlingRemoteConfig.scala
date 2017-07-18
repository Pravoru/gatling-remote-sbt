package com.pravoru.qa.gatling.remote

import com.typesafe.config.ConfigFactory
import sbt.File

import scala.collection.JavaConverters._

object GatlingRemoteConfig {
  def apply(file: File): GatlingRemoteConfig = new GatlingRemoteConfig(file)
}

class GatlingRemoteConfig(file: File) {
  private val config = ConfigFactory.parseFile(file)

  val hosts: List[Host] = config.getConfigList("hosts").asScala.map { element ⇒
    val host = element.getString("host")
    val login = element.getString("login")
    val password = element.getString("password")
    val port = element.getInt("port")
    Host(host, login, password, port)
  }.toList

}
