/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.liyue2008.ipcount

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

/**
  * 接收从Socket接口发送来的NGINX日志，每5秒钟按照IP地址汇总访问次数。
  *
  * 输入：每行一条数据，字段用空格分隔：[时间] [ip地址] ...
  * 例如：
  *  12:00:03 66.249.65.159
  *  12:00:03 66.249.65.3
  *  12:01:04 66.249.65.62
  *  ...
  *
  * 输出：每5分钟输出时间窗口内的所有ip和访问次数
  */
object SocketWindowIpCount {

  def main(args: Array[String]) : Unit = {

    // 获取运行时环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    // 按照EventTime来统计
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    // 设置并行度
    env.setParallelism(4)

    // 定义输入：从Socket端口中获取数据输入
    val hostname: String = "localhost"
    val port: Int = 9999
    // Task 1
    val input: DataStream[String] = env.socketTextStream(hostname, port, '\n')

    // 数据转换：将非结构化的以空格分隔的文本转成结构化数据IpAndCount
    // Task 2
    input
      .map { line => line.split("\\s") }
      .map { wordArray => IpAndCount(new SimpleDateFormat("HH:mm:ss").parse(wordArray(0)), wordArray(1), 1) }

    // 计算：每5秒钟按照ip对count求和

      .assignAscendingTimestamps(_.date.getTime) // 告诉Flink时间从哪个字段中获取


      .keyBy("ip") // 按照ip地址统计
      // Task 3
      .window(TumblingEventTimeWindows.of(Time.seconds(5))) // 每5秒钟统计一次
      .sum("count") // 对count字段求和

    // 输出：转换格式，打印到控制台上

      .map { aggData => new SimpleDateFormat("HH:mm:ss").format(aggData.date) + " " + aggData.ip + " " + aggData.count }
      .print()

    env.execute("Socket Window IpCount")
  }

  /** 中间数据结构 */

  case class IpAndCount(date: Date, ip: String, count: Long)
}
