# Cloudera-Auto-Ops 

## 简介
随着公司业务数据增加，需要提供的服务越来越多，服务器资源捉襟见肘，在高爆发查询的情况下，会导致Hbase Heap空间不足，从而引发Hbase Full Gc，导致线程暂停时间过长，致使所有RegionServer下线，至此Hbase瘫痪。这就是臭名昭著的“STOP THE WORLD”引发的惨案。

在资源不能快速协调的情况下，怎么快速响应这个问题，让我们能够安心的睡觉。由此，实现CHD组件自动化运维的需求应运而生。

该项目主要的功能点就是监控CHD集群中的组件的健康状态，如果检测到异常状态，自动重启，实现自动化运维工作。

## 环境

需要Java 8+版本

依赖[Cloudera Manager API](https://www.cloudera.com/documentation/enterprise/latest.html)

## 运行

主函数入口:
```java 
 com.mispower.autoops.automonitor.AutoOps
```

配置接入的Cloudera集群文件:cloudera.properties

```xml 
webUrl=10.10.11.11
webPort=7180
loginName=admin
loginPwd=!qaz@wsx
clusterName=Cluster 1
serviceName=hbase,hdfs,kafka
```

|参数名|描述|
|-|-|
|webUrl|Clouder Manager Ui Url|
|webPort|Clouder Manager Ui 端口|
|loginName|Clouder Manager 用户名|
|loginPwd|Clouder Manager 密码|
|clusterName|集群名称|
|serviceName|组件服务名|

其中主要是serviceName设置，加入想要监控的组件服务名称就可以了，比如Hbase、Hdfs、Kafka.等

Maven编译jar包后，运行
```bash
nohup java -jar -Xmx512m -Xms512m -Xmn128m  cloudera-auto-monitor-1.0.0.jar>nohup.out 2>&1 &
```

至此，已经开启懒人模式。


