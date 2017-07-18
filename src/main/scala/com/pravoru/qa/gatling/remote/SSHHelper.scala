package com.pravoru.qa.gatling.remote

import java.io.File

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.transport.verification.PromiscuousVerifier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}


object SSHHelper {

  def createSSHSession(host: Host): SSHClient = {
    val ssh = new SSHClient()
    ssh.getTransport.addHostKeyVerifier(new PromiscuousVerifier())
    ssh.connect(host.url, host.port)
    ssh.authPassword(host.login, host.password)
    ssh
  }

  def uploadFile(sourceFile: File, destinationFolder: String)(implicit ssh: SSHClient): Future[Unit] = Future {
    val SFTPClient = ssh.newSFTPClient()
    SFTPClient.put(sourceFile.getAbsolutePath, destinationFolder)
    SFTPClient.close()
  }

  def runCommand(command: String)(implicit ssh: SSHClient): Future[Integer] = Future {
    val session = ssh.startSession()
    session.allocateDefaultPTY()
    val cmd = session.exec(command)
    blocking {
      cmd.join()
    }
    cmd.close()
    if (cmd.getExitStatus != 0) println(IOUtils.readFully(cmd.getInputStream).toString)
    cmd.getExitStatus
  }

}
