package com.pravoru.qa.gatling.remote

import java.io.File

import com.pravoru.qa.gatling.remote.GatlingRemoteKeys._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import sbt.Keys._
import sbt.complete.DefaultParsers._
import sbt._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

object GatlingRemoteTasks {

  def testOnlyRemoteTask(config: Configuration): Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val args: Seq[String] = spaceDelimited("<arg>").parsed

    val log = streams.value.log
    val directory = (stage in Universal).value
    val gatlingRemoteConfigFile = (gatlingRemoteConfigFilePath in config).value
    val remoteHosts = GatlingRemoteConfig(gatlingRemoteConfigFile).hosts
    val workDirectory = (remoteWorkDirectoryPath in config).value
    val projectName = (normalizedName in config).value
    val deployTimeout = (deployTimeoutDuration in config).value
    val runTimeout = (runTimeoutDuration in config).value

    deployFiles(
      workDirectory = workDirectory,
      projectName = projectName,
      hosts = remoteHosts,
      timeout = deployTimeout,
      assembledDirectory = directory
    )

    runSimulation(
      simulationName = args.head,
      workDirectory = workDirectory,
      projectName = projectName,
      hosts = remoteHosts,
      timeout = runTimeout
    )

  }

  def generateMapping(config: Configuration): Def.Initialize[Task[Seq[(File, String)]]] = Def.task {
    val assembledTests = assembleTests(config).value → s"lib/${name.value}-${config.name}.jar"
    val aggregatedClassFiles = aggregateClassFiles(config).value.map { file =>
      file -> file.getAbsolutePath.replace(s"${baseDirectory.value.absolutePath}${File.separator}", "")
    }
    val materializedBinScript = materializeBinScript(config).value → "bin/run.sh"
    val gatlingConfigFiles = getGatlingConfigFiles(config).value
    gatlingConfigFiles ++ aggregatedClassFiles :+ materializedBinScript :+ assembledTests
  }

  def deployFiles(
                   workDirectory: String,
                   projectName: String,
                   hosts: List[Host],
                   timeout: Duration,
                   assembledDirectory: File
                 ): Unit = {
    val sshClientsList = hosts.map(SSHHelper.createSSHSession)

    def cleanDestinationFutureList = sshClientsList.map { implicit ssh ⇒
      SSHHelper.runCommand(s"rm -rf $workDirectory/$projectName")
    }

    def uploadFutureList = sshClientsList.map { implicit ssh ⇒
      SSHHelper.uploadFile(assembledDirectory, s"$workDirectory/$projectName")
    }

    val cleanDestinationFuture = Future.sequence(cleanDestinationFutureList)
    Await.ready(cleanDestinationFuture, timeout)

    val uploadFuture = Future.sequence(uploadFutureList)
    Await.ready(uploadFuture, timeout)

    sshClientsList.foreach(_.disconnect())
  }

  def runSimulation(
                     simulationName: String,
                     workDirectory: String,
                     projectName: String,
                     hosts: List[Host],
                     timeout: Duration
                   ): Unit = {
    val sshClientsList = hosts.map(SSHHelper.createSSHSession)

    def runLoadTestList = sshClientsList.map { implicit ssh ⇒
      SSHHelper.runCommand(
        s"cd $workDirectory/$projectName/bin/ && sh run.sh -s $simulationName > log.txt 2>&1"
      )
    }

    val runLoadTestListFuture = Future.sequence(runLoadTestList)
    Await.ready(runLoadTestListFuture, timeout)

    sshClientsList.foreach(_.disconnect())
  }

  private def aggregateClassFiles(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
    (compile in config).value
    val dir = (classDirectory in config).value
    ((dir ** "*.class") filter {
      !_.isDirectory
    }).get
  }

  private def assembleTests(config: Configuration): Def.Initialize[Task[File]] = Def.task {
    publishArtifact in(config, packageBin) := true
    (packageBin in config).value
  }

  private def materializeBinScript(config: Configuration): Def.Initialize[Task[File]] = Def.task {
    val content = Source.fromInputStream(getClass.getResourceAsStream("/run.sh")).mkString
      .replace("grafiteRootPathPrefix", (grafiteRootPathPrefix in config).value)
    val tempFile = taskTemporaryDirectory.value / "run.sh"
    IO.write(tempFile, content)
    tempFile
  }


  def getGatlingConfigFiles(config: Configuration): Def.Initialize[Task[Seq[(File, String)]]] = Def.task {
    val gatlingConfigFile = (gatlingConfigFilePath in config).value
    val gatlingAkkaConfigFile = (gatlingAkkaConfigFilePath in config).value
    val gatlingRemoteConfig = (gatlingRemoteConfigFilePath in config).value
    val logbackConfigFile = (logbackConfigFilePath in config).value
    assert(gatlingConfigFile.exists(), s"File ${gatlingConfigFile.getAbsolutePath} is not founded")
    assert(gatlingAkkaConfigFile.exists(), s"File ${gatlingConfigFile.getAbsolutePath} is not founded")
    assert(logbackConfigFile.exists(), s"File ${gatlingConfigFile.getAbsolutePath} is not founded")
    Seq(
      gatlingConfigFile → "conf/gatling.conf",
      gatlingAkkaConfigFile → "conf/gatling-akka.conf",
      gatlingRemoteConfig → "conf/gatling-remote.conf",
      logbackConfigFile → "conf/logback.xml"
    )
  }

}
