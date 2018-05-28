## MQTT BACKEND CLIENT
### run as foreground command
#### use default configuration

```shell
java -jar tsp-mqtt-backend.jar
```

#### use specified configuration

```shell
java -jar tsp-mqtt-backend.jar -C/opt/xxx/config.porperties
```

### run as backend command
```shell
#! /bin/sh                                                                                                                                                     
echo "start tsp mqtt backend client; listen on 127.0.0.1:9090"        
nohup java -jar tsp-mqtt-backend-app-1.0-SNAPSHOT-jar-with-dependencies.jar -C/opt/tmb/config.properties  > /dev/null &
```

### configuration description
```properties
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/tsp_data?characterEncoding=UTF-8
jdbc.username=root
jdbc.password=123456
jdbc.minConnectionsPerPartition=5
jdbc.maxConnectionsPerPartition=10
jdbc.partitionCount=3
app.threadPoolCoreSize=15
mqtt.server=127.0.0.1
mqtt.tcp.port=1883
mqtt.tcp.broker=tcp://127.0.0.1:11883
mqtt.topic=TBOX/NAD/+/LOG
http.port=9090
#批处理一次操作的数量
app.batch.insert=100
#处理队列为空可以存活的时间
app.process.inactive=120000
#日志级别
app.log.level=debug
```