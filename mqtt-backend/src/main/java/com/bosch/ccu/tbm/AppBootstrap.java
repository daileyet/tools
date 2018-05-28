/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm;


import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bosch.ccu.tbm.module.mqtt.RMLogMQTTClient;
import com.bosch.ccu.tbm.service.TBoxLogService;
import com.bosch.ccu.tbm.util.DBHelper;
import com.bosch.ccu.tbm.util.LifeCycle;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.openthinks.libs.utilities.Checker;
import com.openthinks.libs.utilities.lookup.Lookups;

/**
 * ClassName: AppBootstrap <br>
 * date: May 23, 2018 9:36:24 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public final class AppBootstrap implements LifeCycle {
  final static Logger LOGGER = LoggerFactory.getLogger(AppBootstrap.class);

  private final LifeCycle mqttClient;
  private final ScheduledExecutorService scheduledExecutorService;
  private AppConfig appConfig;
  private Future<?> monitorStoreTable;

  private AppBootstrap() {
    this.appConfig = Lookups.global().lookup(AppConfig.class);
    this.mqttClient = Lookups.global().lookup(RMLogMQTTClient.class);
    this.scheduledExecutorService =
        Executors.newScheduledThreadPool(appConfig.getThreadPoolCoreSize());
    Lookups.global().register(ScheduledExecutorService.class, scheduledExecutorService);
  }


  @Override
  public void initial() {
    try {
      Class.forName(appConfig.getDbDriver());
      BoneCPConfig config = new BoneCPConfig();
      config.setJdbcUrl(appConfig.getDbUrl());
      config.setUsername(appConfig.getDbUserName());
      config.setPassword(appConfig.getDbUserPass());
      config.setMinConnectionsPerPartition(appConfig.getMinConnPerPartition());
      config.setMaxConnectionsPerPartition(appConfig.getMaxConnPerPartition());
      config.setPartitionCount(appConfig.getPartitionCount());
      BoneCP connectionPool = new BoneCP(config); // setup the connection pool
      Lookups.global().register(BoneCP.class, connectionPool);
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e);
    }
    LOGGER.info("Initial database connection pool success.");
    mqttClient.initial();
  }


  @Override
  public void start() {
    monitorStoreTable = scheduledExecutorService.scheduleAtFixedRate(FETCH_STORE_TABLE_NAME, 0, 5, TimeUnit.MINUTES);
    mqttClient.start();
  }


  @Override
  public void stop() {
    mqttClient.stop();
    if (monitorStoreTable != null) {
      monitorStoreTable.cancel(true);
    }
    monitorStoreTable = null;
  }

  @Override
  public void destory() {
    mqttClient.destory();
    BoneCP boneCP = Lookups.global().lookup(BoneCP.class);
    if (boneCP != null) {
      boneCP.shutdown();
    }
    Lookups.cleanUp();
  }

  public static final LifeCycle getInstance() {
    return LifeCycleHolder.INSTANCE;
  }

  private static final class LifeCycleHolder {
    private static final LifeCycle INSTANCE;
    static {
      INSTANCE = new AppBootstrap();
    }
  }

  final Runnable FETCH_STORE_TABLE_NAME = () -> {
    try {
      String tableName = DBHelper.fetchTBoxLogTableName();
      Checker.require(tableName).notEmpty();
      TBoxLogService.setTableName(tableName);
    } catch (Exception e) {
      LOGGER.error("Failed to fecth TBox log table name.", e);
    }

  };

}
