package com.github.liyue2008.ipcount

import java.io._
import java.net.ServerSocket
import java.text.SimpleDateFormat
import java.util.Date

import scala.concurrent.forkjoin.ThreadLocalRandom

/**
  * @author LiYue
  *         Date: 2019-09-14
  */
object SourceSocketServer {
  def main(args: Array[String]): Unit = {
    val ipList: List[String] = List("192.168.1.1","192.168.1.2","192.168.1.3","192.168.1.4")
    val server = new ServerSocket(9999)
    println("Serve initialized:")
    while (true) {
      val client = server.accept
      new Thread(new Runnable {
        override def run(): Unit = {
          println("Accepted new connection.")
          val out = new PrintStream(client.getOutputStream)
          val sdf = new SimpleDateFormat("HH:mm:ss");
          while (client.isConnected) {
            val data = IpAndCount(new Date(), ipList(ThreadLocalRandom.current().nextInt(ipList.size)))
            out.println(sdf.format(data.date) + " " + data.ip)
            out.flush()
            Thread.sleep(ThreadLocalRandom.current().nextLong(100L))
          }
          println("Connection closed.")
        }
      }).start
    }
  }


  case class IpAndCount(date: Date, ip: String)
}
