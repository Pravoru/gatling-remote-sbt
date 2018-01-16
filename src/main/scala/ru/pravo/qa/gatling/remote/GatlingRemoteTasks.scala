package ru.pravo.qa.gatling.remote

import java.io.File

import ru.pravo.qa.gatling.remote.GatlingRemoteKeys._
import sbt.Keys._
import sbt.complete.DefaultParsers._
import sbt.{Def, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

object GatlingRemoteTasks {

  def testOnlyRemoteTask(config: Configuration): Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val args: Seq[String] = spaceDelimited("<arg>").parsed

    val log = streams.value.log
    asserts(config)
    val directory = (assembleProject in config).value
    val gatlingRemoteConfigFile = (gatlingRemoteConfigFilePath in config).value
    val remoteHosts = GatlingRemoteConfig(gatlingRemoteConfigFile).hosts
    val workDirectory = (remoteWorkDirectoryPath in config).value
    val projectName = (normalizedName in config).value
    val deployTimeout = (deployTimeoutDuration in config).value
    val runTimeout = (runTimeoutDuration in config).value

    log.info("Deploying assembled tests.")
    deployFiles(
      workDirectory = workDirectory,
      projectName = projectName,
      hosts = remoteHosts,
      timeout = deployTimeout,
      assembledDirectory = directory
    )
    log.info("Done deploying.")

    log.info("Running test on remote hosts.")
    runSimulation(
      simulationName = args.head,
      workDirectory = workDirectory,
      projectName = projectName,
      hosts = remoteHosts,
      timeout = runTimeout
    )
    log.info("Done running.")

  }

  def generateMapping(config: Configuration): Def.Initialize[Task[Seq[(File, String)]]] = Def.task {
    val baseDirectoryValue = baseDirectory.value
    val assembledTests = assembleTests(config).value → s"lib/${name.value}-${config.name}.jar"
    val aggregatedClassFiles = aggregateClassFiles(config).value.map { file =>
      file -> file.getAbsolutePath.replace(s"${baseDirectoryValue.absolutePath}${File.separator}", "")
    }
    val materializedBinScript = materializeBinScripts(config).value.map { file ⇒
      file → s"bin/${file.getName}"
    }

    configurationFilesToMapping(config).value ++
      userFilesDataFilesToMapping(config).value ++
      aggregatedClassFiles ++
      materializedBinScript :+
      assembledTests
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

  private def materializeBinScripts(config: Configuration): Def.Initialize[Task[List[File]]] = Def.task {
    val grafiteRootPathPrefixValue = (grafiteRootPathPrefix in config).value
    val taskTemporaryDirectoryValue = taskTemporaryDirectory.value
    List("run.sh", "run.bat").map { file ⇒
      val content = Source.fromInputStream(getClass.getResourceAsStream(s"/$file")).mkString
        .replace("grafiteRootPathPrefix", grafiteRootPathPrefixValue)
      val tempFile = taskTemporaryDirectoryValue / file
      IO.write(tempFile, content)
      tempFile
    }
  }

  private def asserts(config: Configuration): Def.Initialize[Task[Unit]] = Def.task {

    def fileAssert(file: File): Unit = {
      assert(
        file.exists(),
        s"File ${file.getAbsolutePath} is not founded"
      )
    }

    fileAssert(ConfigurationFiles.gatlingConfigFile(config).value)
    fileAssert(ConfigurationFiles.gatlingAkkaConfigFile(config).value)
    fileAssert(ConfigurationFiles.gatlingRemoteConfigFile(config).value)
  }

  private def configurationFilesToMapping(config: Configuration): Def.Initialize[Task[Seq[(File, String)]]] = Def.task {
    val gatlingConfigFile = ConfigurationFiles.gatlingConfigFile(config).value
    configurationFiles.value.map { file ⇒
      file → s"${BundleDirectoriesStructure(gatlingConfigFile).configurationDirectory}/${file.name}"
    }
  }

  private def userFilesDataFilesToMapping(config: Configuration): Def.Initialize[Task[Seq[(File, String)]]] = Def.task {
    val gatlingConfigFile = ConfigurationFiles.gatlingConfigFile(config).value
    val resourceDirectoryValue = (resourceDirectory in config).value
    val resourceDirectoryInCompileValue = (resourceDirectory in Compile).value
    userFilesDataFiles.value.flatMap { file ⇒
      if (file.isFile) {
        Seq(file → s"${BundleDirectoriesStructure(gatlingConfigFile).userFilesDataDirectory}/${file.name}")
      } else {
        file.**(AllPassFilter).get.map { file ⇒
          //We do not know where file came from
          val testResourceDirectoryAbsolutePath = resourceDirectoryValue.getAbsolutePath
          val mainResourceDirectoryAbsolutePath = resourceDirectoryInCompileValue.getAbsolutePath
          val getAbsolutePath = file.getAbsolutePath
          val filePath = file.getAbsolutePath
            .replace(testResourceDirectoryAbsolutePath, "")
            .replace(mainResourceDirectoryAbsolutePath, "")
          file → s"${BundleDirectoriesStructure(gatlingConfigFile).userFilesDataDirectory}/$filePath"
        }
      }
    }
  }

  def removesFileFromJar(config: Configuration): Def.Initialize[Task[Seq[(File, String)] ⇒ Seq[(File, String)]]] = Def.task {
    val configurationFilesValue = configurationFiles.value
    val userFilesDataFilesValue = userFilesDataFiles.value
    ms: Seq[(File, String)] ⇒ {
      val configFiles = configurationFilesValue ++ userFilesDataFilesValue
      ms filter {
        case (file, toPath) => !configFiles.contains(file)
      }
    }
  }
}
