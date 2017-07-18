package com.pravoru.qa.gatling.remote

import sbt._

import scala.concurrent.duration.Duration

object GatlingRemoteKeys {

  val testOnlyRemote: InputKey[Unit] = inputKey("Assemble code -> Deploy bundle -> Run tests -> Retrieve logs -> Create report")
  val assembleProject: TaskKey[File] = taskKey("Assemble project")
  val gatlingConfigFilePath: TaskKey[File] = taskKey("Path to gatling.conf file")
  val gatlingAkkaConfigFilePath: TaskKey[File] = taskKey("Path to gatling-akka.conf file")
  val gatlingRemoteConfigFilePath: TaskKey[File] = taskKey("Path to gatling-remote.conf file")
  val logbackConfigFilePath: TaskKey[File] = taskKey("Path to logback.xml file")
  val remoteWorkDirectoryPath: TaskKey[String] = taskKey("Path to work directory on remote server")
  val deployTimeoutDuration: TaskKey[Duration] = taskKey("Timeout for deploying operation")
  val runTimeoutDuration: TaskKey[Duration] = taskKey("Timeout for running operation")
  val remoteServersConfigurationFilePath: TaskKey[File] = taskKey("Path to file with remote servers configuration")
  val grafiteRootPathPrefix: TaskKey[String] = taskKey("Root path that will be overwritten in run.sh")

}
