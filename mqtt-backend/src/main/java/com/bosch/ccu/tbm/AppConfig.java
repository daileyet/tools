/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm;

import java.lang.reflect.Field;
import java.util.Properties;
import com.bosch.ccu.tbm.util.PropertyKey;
import com.openthinks.libs.utilities.Converter;

/**
 * ClassName: AppConfig <br>
 * date: May 23, 2018 8:15:24 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public final class AppConfig {
  public AppConfig(final Properties props) {
    load(props);
  }

  private void load(final Properties props) {
    resolveValues(props);
  }

  private void resolveValues(final Properties props) {
    Field[] fields = AppConfig.class.getDeclaredFields();
    for (Field field : fields) {
      PropertyKey pk = field.getAnnotation(PropertyKey.class);
      if (pk == null) {
        continue;
      }
      String key = pk.value();
      String value = props.getProperty(key);
      if (value == null) {
        value = pk.defaultValue();
      }
      if (value == null) {
        continue;
      }
      Class<?> fieldType = field.getType();
      Object targetValue = Converter.source(value).convertToSingle(fieldType);
      field.setAccessible(true);
      try {
        field.set(this, targetValue);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  @PropertyKey("jdbc.driverClassName")
  private String dbDriver;
  @PropertyKey("jdbc.url")
  private String dbUrl;
  @PropertyKey("jdbc.username")
  private String dbUserName;
  @PropertyKey("jdbc.password")
  private String dbUserPass;
  @PropertyKey("jdbc.minConnectionsPerPartition")
  private Integer minConnPerPartition;
  @PropertyKey("jdbc.maxConnectionsPerPartition")
  private Integer maxConnPerPartition;
  @PropertyKey("jdbc.partitionCount")
  private Integer partitionCount;
  @PropertyKey(value = "app.threadPoolCoreSize", defaultValue = "15")
  private Integer threadPoolCoreSize;
  @PropertyKey("mqtt.server")
  private String mqttServer;
  @PropertyKey("mqtt.tcp.port")
  private Integer mqttTcpPort;
  @PropertyKey("mqtt.topic")
  private String mqttTopic;
  @PropertyKey("mqtt.tcp.broker")
  private String mqttTcpBroker;
  @PropertyKey("http.port")
  private String httpPort;
  @PropertyKey(value = "app.batch.insert", defaultValue = "100")
  private Integer insertBatchCount;
  @PropertyKey(value = "app.process.inactive", defaultValue = (2 * 60 * 1000) + "")
  private Integer maxProcessInactive;
  @PropertyKey(value = "app.fastquery.factor", defaultValue = "1000")
  private Integer fastQueryFactor;
  @PropertyKey(value = "app.queue.size", defaultValue = "100000")
  private Integer queueSize;
  /**
   * dbDriver.
   * 
   * @return the dbDriver
   */
  public String getDbDriver() {
    return dbDriver;
  }

  /**
   * dbDriver.
   * 
   * @param dbDriver the dbDriver to set
   */
  public void setDbDriver(String dbDriver) {
    this.dbDriver = dbDriver;
  }

  /**
   * dbUrl.
   * 
   * @return the dbUrl
   */
  public String getDbUrl() {
    return dbUrl;
  }

  /**
   * dbUrl.
   * 
   * @param dbUrl the dbUrl to set
   */
  public void setDbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  /**
   * dbUserName.
   * 
   * @return the dbUserName
   */
  public String getDbUserName() {
    return dbUserName;
  }

  /**
   * dbUserName.
   * 
   * @param dbUserName the dbUserName to set
   */
  public void setDbUserName(String dbUserName) {
    this.dbUserName = dbUserName;
  }

  /**
   * dbUserPass.
   * 
   * @return the dbUserPass
   */
  public String getDbUserPass() {
    return dbUserPass;
  }

  /**
   * dbUserPass.
   * 
   * @param dbUserPass the dbUserPass to set
   */
  public void setDbUserPass(String dbUserPass) {
    this.dbUserPass = dbUserPass;
  }

  /**
   * minConnPerPartition.
   * 
   * @return the minConnPerPartition
   */
  public Integer getMinConnPerPartition() {
    return minConnPerPartition;
  }

  /**
   * minConnPerPartition.
   * 
   * @param minConnPerPartition the minConnPerPartition to set
   */
  public void setMinConnPerPartition(Integer minConnPerPartition) {
    this.minConnPerPartition = minConnPerPartition;
  }

  /**
   * maxConnPerPartition.
   * 
   * @return the maxConnPerPartition
   */
  public Integer getMaxConnPerPartition() {
    return maxConnPerPartition;
  }

  /**
   * maxConnPerPartition.
   * 
   * @param maxConnPerPartition the maxConnPerPartition to set
   */
  public void setMaxConnPerPartition(Integer maxConnPerPartition) {
    this.maxConnPerPartition = maxConnPerPartition;
  }

  /**
   * partitionCount.
   * 
   * @return the partitionCount
   */
  public Integer getPartitionCount() {
    return partitionCount;
  }

  /**
   * partitionCount.
   * 
   * @param partitionCount the partitionCount to set
   */
  public void setPartitionCount(Integer partitionCount) {
    this.partitionCount = partitionCount;
  }

  public Integer getThreadPoolCoreSize() {
    return threadPoolCoreSize;
  }

  public void setThreadPoolCoreSize(Integer threadPoolCoreSize) {
    this.threadPoolCoreSize = threadPoolCoreSize;
  }

  /**
   * mqttServer.
   * 
   * @return the mqttServer
   */
  public String getMqttServer() {
    return mqttServer;
  }

  /**
   * mqttServer.
   * 
   * @param mqttServer the mqttServer to set
   */
  public void setMqttServer(String mqttServer) {
    this.mqttServer = mqttServer;
  }

  /**
   * mqttTcpPort.
   * 
   * @return the mqttTcpPort
   */
  public Integer getMqttTcpPort() {
    return mqttTcpPort;
  }

  /**
   * mqttTcpPort.
   * 
   * @param mqttTcpPort the mqttTcpPort to set
   */
  public void setMqttTcpPort(Integer mqttTcpPort) {
    this.mqttTcpPort = mqttTcpPort;
  }

  /**
   * mqttTopic.
   * 
   * @return the mqttTopic
   */
  public String getMqttTopic() {
    return mqttTopic;
  }

  /**
   * mqttTopic.
   * 
   * @param mqttTopic the mqttTopic to set
   */
  public void setMqttTopic(String mqttTopic) {
    this.mqttTopic = mqttTopic;
  }


  public String getMqttTcpBroker() {
    return mqttTcpBroker;
  }

  public void setMqttTcpBroker(String mqttTcpBroker) {
    this.mqttTcpBroker = mqttTcpBroker;
  }

  public String getHttpPort() {
    return httpPort;
  }

  public void setHttpPort(String httpPort) {
    this.httpPort = httpPort;
  }

  public Integer getInsertBatchCount() {
    return insertBatchCount;
  }

  public void setInsertBatchCount(Integer insertBatchCount) {
    this.insertBatchCount = insertBatchCount;
  }

  public Integer getMaxProcessInactive() {
    return maxProcessInactive;
  }

  public void setMaxProcessInactive(Integer maxProcessInactive) {
    this.maxProcessInactive = maxProcessInactive;
  }
  
  public Integer getFastQueryFactor() {
    return fastQueryFactor;
  }
  
  public void setFastQueryFactor(Integer fastQueryFactor) {
    this.fastQueryFactor = fastQueryFactor;
  }
  
  public Integer getQueueSize() {
    return queueSize;
  }
  
  public void setQueueSize(Integer queueSize) {
    this.queueSize = queueSize;
  }
}
