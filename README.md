# 消息队列高手课

极客时间《消息队列高手课》案例篇《29. 流计算与消息（一）：通过Flink理解流计算的原理》示例源代码。

## 环境要求

运行示例之前需要先安装：

* JDK 1.8
* Scala 2.12
* Maven 3.3.9

```bash
$java -version
java version "1.8.0_202"
Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)

$scala -version
Scala code runner version 2.12.4 -- Copyright 2002-2017, LAMP/EPFL and Lightbend, Inc.

$mvn -version
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-11T00:41:47+08:00)
```

## 下载编译源代码

```bash
$git clone git@github.com:liyue2008/IpCount.git
$cd IpCount
$mvn package
```

## 下载启动Flink

去[Flink官网下载页面](https://flink.apache.org/downloads.html)，下载Apache Flink 1.9.0 for Scala 2.12，文件名为：flink-1.9.0-bin-scala_2.12.tgz。

解压到目录：flink-1.9.0

修改flink-1.9.0/conf/flink-conf.yaml，将槽数改为8：

```yaml
 # The number of task slots that each TaskManager offers. Each slot runs one parallel pipeline.

taskmanager.numberOfTaskSlots: 8
```

启动Flink集群：

```bash
$ bin/start-cluster.sh
Starting cluster.
Starting standalonesession daemon on host localhost.
Starting taskexecutor daemon on host localhost.
```

## 启动模拟日志服务

首先需要启动模拟日志服务，作为数据源

```bash
$scala -classpath target/IpCount-1.0.jar com.github.liyue2008.ipcount.SourceSocketServer
Serve initialized:
```

## 提交任务

```bash
$flink run target/IpCount-1.0.jar
Starting execution of program
```

查看日志输出：

```bash
$tail -f flink-1.9.0/log/flink-*-taskexecutor-*.out
1> 18:40:10 192.168.1.2 23
4> 18:40:10 192.168.1.4 16
4> 18:40:15 192.168.1.4 27
3> 18:40:15 192.168.1.3 23
1> 18:40:15 192.168.1.2 25
4> 18:40:15 192.168.1.1 21
1> 18:40:20 192.168.1.2 21
```