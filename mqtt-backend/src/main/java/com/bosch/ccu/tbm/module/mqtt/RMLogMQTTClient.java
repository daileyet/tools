/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.module.mqtt;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bosch.ccu.tbm.AppConfig;
import com.bosch.ccu.tbm.model.TBoxLog;
import com.bosch.ccu.tbm.module.mqtt.support.BlockingQueuePool;
import com.bosch.ccu.tbm.module.mqtt.support.PoolConsumer;
import com.bosch.ccu.tbm.module.mqtt.support.ProcessCacher;
import com.bosch.ccu.tbm.module.mqtt.support.RMLogBlockingQueuePool;
import com.bosch.ccu.tbm.module.mqtt.support.RMLogProcessCacher;
import com.bosch.ccu.tbm.module.mqtt.support.RMLogTopicResolve;
import com.bosch.ccu.tbm.module.mqtt.support.UnmodifiedProcessCacher;
import com.bosch.ccu.tbm.service.TBoxLogService;
import com.bosch.ccu.tbm.util.LifeCycle;
import com.openthinks.libs.utilities.lookup.Lookups;

/**
 * ClassName: RMLogMQTTClient <br>
 * Function: MQTTClient bean. <br>
 * date: May 18, 2018 2:27:25 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public class RMLogMQTTClient implements LifeCycle {
  private TBoxLogService tboxService;
  private ScheduledExecutorService scheduler;
  private AppConfig appConfig;
  private volatile boolean isConnected = false;
  ////////////////////////////////////////////////////////////////////////////
  // property
  protected String broker = "tcp://localhost:1884";

  protected String topic = "TBOX/NAD/+/LOG";

  protected String clientId = "rdb-inner-receiver";

  protected String userName = "inner_user";

  protected String userPass = "inner_user_pass";

  protected int connectionTimeout = 30;

  protected int keepAliveInterval = 60000;

  protected boolean cleanSession = true;

  protected MqttCallback mqttCallback = new RMLogMqttCallback();

  protected MqttClientPersistence clientPersistence = new MemoryPersistence();

  protected BlockingQueuePool<TBoxLog> poolQueue;

  protected RMLogTopicResolve topicResolve = (resolveTopic) -> {
    return resolveTopic == null ? null : resolveTopic.replace("TBOX/NAD/", "").replace("/LOG", "");
  };

  protected ProcessCacher<TBoxLog> processCacher;

  protected MqttConnectOptions options;

  //////////////////////////////////////////////////////////////////////////
  public RMLogMQTTClient() {}


  @Override
  public void initial() {
    tboxService = Lookups.global().lookup(TBoxLogService.class);
    appConfig = Lookups.global().lookup(AppConfig.class);
    scheduler = Lookups.global().lookup(ScheduledExecutorService.class);
    setBroker(appConfig.getMqttTcpBroker());
    setTopic(appConfig.getMqttTopic());
    processCacher =
        new RMLogProcessCacher(appConfig.getInsertBatchCount(), appConfig.getMaxProcessInactive());
    poolQueue =
        new RMLogBlockingQueuePool(appConfig.getMaxProcessInactive(), appConfig.getQueueSize());
    LOGGER.info("Initial mqtt client success.");
  }

  public void start() {
    startReconnectMonitor();
  }

  @Override
  public void stop() {
    if (reconnectFuture != null) {
      reconnectFuture.cancel(true);
      reconnectFuture = null;
    }
    if (storeFuture != null) {
      storeFuture.cancel(true);
      storeFuture = null;
    }
  }


  @Override
  public void destory() {
    stop();
    disconnect();
  }

  protected void startReconnectMonitor() {
    if (reconnectFuture != null) {
      reconnectFuture.cancel(true);
      reconnectFuture = null;
    }
    reconnectFuture = scheduler.scheduleAtFixedRate(MQTT_RECONNECT_TASK, 5 * 1000, 10 * 1000,
        TimeUnit.MILLISECONDS);
  }

  protected void doConnect() throws MqttException {
    startStoreWorker();
    if (mqttlient == null) {
      createMqttClient();
    }
    mqttlient.setCallback(mqttCallback);
    mqttlient.connect(options);
    mqttlient.subscribe(topic);
    isConnected = true;
  }



  public void disconnect() {
    try {
      if (mqttlient != null)
        mqttlient.disconnect();
      isConnected = false;
    } catch (MqttException e) {
      LOGGER.error("Disconnect mqtt sever error.", e);
    }
  }



  final Runnable MQTT_RECONNECT_TASK = () -> {
    if (!isConnected) {
      try {
        doConnect();
        LOGGER.debug("reconnected.");
      } catch (MqttSecurityException e) {
        LOGGER.error("Connect mqtt sever error by security:", e);
        disconnect();
      } catch (MqttException e) {
        if (e.getReasonCode() == MqttException.REASON_CODE_CLIENT_CONNECTED) {
          isConnected = true;
        } else {
          LOGGER.error("Connect mqtt sever error.", e);
          disconnect();
        }
      }
    }
  };

  final PoolConsumer<TBoxLog> MESSAGE_CONSUMER = new PoolConsumer<TBoxLog>() {

    @Override
    public void accept(TBoxLog logBean) {
      if (logBean == null)
        return;
      boolean isSuccess = processCacher.tryCache(logBean);
      if (!isSuccess) {// cache full, need save to database
        doSave();
      }
    }

    private boolean needFlush() {
      return processCacher.isActive() && !processCacher.isEmpty();
    }

    @Override
    public void flush() {
      if (needFlush()) {
        LOGGER.info("Flush cache message and save to database for max inactive time has been out.");
        doSave();
      }
    }

    private void doSave() {
      final List<TBoxLog> tboxLogs = processCacher.purge();
      if (tboxLogs == null || tboxLogs.isEmpty()) {
        return;
      }
      scheduler.submit(() -> {
        try {
          tboxService.batchInsert(tboxLogs);
          tboxLogs.clear();
        } catch (SQLException e) {
          LOGGER.error("Failed to save TBox log to database as batch.", e);
        }
      });
    }
  };


  final Runnable MESSAGE_STORE_TASK = () -> {
    while (true && !Thread.currentThread().isInterrupted()) {
      try {
        poolQueue.pollAll(200, TimeUnit.MILLISECONDS, MESSAGE_CONSUMER);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.error("Failed to store tbox log", e);
      } catch (Exception e) {
        LOGGER.error("Failed to store tbox log", e);
      }
    }
  };


  final class RMLogMqttCallback implements MqttCallback {
    public static final int SCALE_FACTOR = 1000;
    final AtomicLong seed = new AtomicLong(0);

    @Override
    public void connectionLost(Throwable cause) {
      isConnected = false;
      LOGGER.error("mqtt connection lost:" + cause);
      // 最大可用内存，对应-Xmx
      LOGGER.error("maxMemory:{}", Runtime.getRuntime().maxMemory());
      // 当前JVM空闲内存
      LOGGER.error("freeMemory:{}", Runtime.getRuntime().freeMemory());
      // 当前JVM占用的内存总数，其值相当于当前JVM已使用的内存及freeMemory()的总和
      LOGGER.error("totalMemory:{}", Runtime.getRuntime().totalMemory());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
      LOGGER.debug("mqtt client message arrived for topic=" + topic + ";message=" + message);
      processMessage(topic, message);
    }

    private void processMessage(String topic, MqttMessage message) {
      final String imsi = topicResolve.extractIMSI(topic);
      if (imsi == null) {
        LOGGER.warn("cannot resolve IMSI from topic=" + topic + ";message=" + message);
        return;
      }
      String log = new String(message.getPayload(), Charset.forName("UTF-8"));
      TBoxLog logBean = new TBoxLog();
      logBean.setId((System.currentTimeMillis() * SCALE_FACTOR) + seed.getAndIncrement());
      logBean.setImsi(imsi);
      logBean.setMsg(log);
      logBean.setLastModify(new Date());
      poolQueue.offer(imsi, logBean);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
      LOGGER.debug("mqtt client message deliveried:" + token);
    }

  }



  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private method

  private void startStoreWorker() {
    if (storeFuture != null) {
      storeFuture.cancel(true);
      storeFuture = null;
    }
    storeFuture = scheduler.submit(MESSAGE_STORE_TASK);
  }

  private void createMqttClient() throws MqttException {
    mqttlient = new MqttClient(broker, clientId, clientPersistence);
    if (options == null) {
      options = new MqttConnectOptions();
      options.setUserName(userName);
      options.setPassword(userPass.toCharArray());
      options.setCleanSession(cleanSession);
      options.setConnectionTimeout(connectionTimeout);
      options.setKeepAliveInterval(keepAliveInterval);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // property getter/setter
  /**
   * broker.
   * 
   * @return the broker
   */
  public String getBroker() {
    return broker;
  }

  /**
   * broker.
   * 
   * @param broker the broker to set
   */
  public void setBroker(String broker) {
    this.broker = broker;
  }

  /**
   * topic.
   * 
   * @return the topic
   */
  public String getTopic() {
    return topic;
  }

  /**
   * topic.
   * 
   * @param topic the topic to set
   */
  public void setTopic(String topic) {
    this.topic = topic;
  }

  /**
   * clientId.
   * 
   * @return the clientId
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * clientId.
   * 
   * @param clientId the clientId to set
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * userName.
   * 
   * @return the userName
   */
  public String getUserName() {
    return userName;
  }

  /**
   * userName.
   * 
   * @param userName the userName to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * userPass.
   * 
   * @return the userPass
   */
  public String getUserPass() {
    return userPass;
  }

  /**
   * userPass.
   * 
   * @param userPass the userPass to set
   */
  public void setUserPass(String userPass) {
    this.userPass = userPass;
  }

  /**
   * mqttCallback.
   * 
   * @return the mqttCallback
   */
  public MqttCallback getMqttCallback() {
    return mqttCallback;
  }

  /**
   * mqttCallback.
   * 
   * @param mqttCallback the mqttCallback to set
   */
  public void setMqttCallback(MqttCallback mqttCallback) {
    this.mqttCallback = mqttCallback;
  }

  /**
   * clientPersistence.
   * 
   * @return the clientPersistence
   */
  public MqttClientPersistence getClientPersistence() {
    return clientPersistence;
  }

  /**
   * clientPersistence.
   * 
   * @param clientPersistence the clientPersistence to set
   */
  public void setClientPersistence(MqttClientPersistence clientPersistence) {
    this.clientPersistence = clientPersistence;
  }

  /**
   * options.
   * 
   * @return the options
   */
  public MqttConnectOptions getOptions() {
    return options;
  }

  /**
   * options.
   * 
   * @param options the options to set
   */
  public void setOptions(MqttConnectOptions options) {
    this.options = options;
  }

  /**
   * poolQueue.
   * 
   * @return the poolQueue
   */
  public BlockingQueuePool<TBoxLog> getPoolQueue() {
    return poolQueue;
  }

  /**
   * poolQueue.
   * 
   * @param poolQueue the poolQueue to set
   */
  public void setPoolQueue(BlockingQueuePool<TBoxLog> poolQueue) {
    this.poolQueue = poolQueue;
  }

  /**
   * topicResolve.
   * 
   * @return the topicResolve
   */
  public RMLogTopicResolve getTopicResolve() {
    return topicResolve;
  }

  /**
   * topicResolve.
   * 
   * @param topicResolve the topicResolve to set
   */
  public void setTopicResolve(RMLogTopicResolve topicResolve) {
    this.topicResolve = topicResolve;
  }

  /**
   * connectionTimeout.
   * 
   * @return the connectionTimeout
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * connectionTimeout.
   * 
   * @param connectionTimeout the connectionTimeout to set
   */
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * keepAliveInterval.
   * 
   * @return the keepAliveInterval
   */
  public int getKeepAliveInterval() {
    return keepAliveInterval;
  }

  /**
   * keepAliveInterval.
   * 
   * @param keepAliveInterval the keepAliveInterval to set
   */
  public void setKeepAliveInterval(int keepAliveInterval) {
    this.keepAliveInterval = keepAliveInterval;
  }

  /**
   * cleanSession.
   * 
   * @return the cleanSession
   */
  public boolean isCleanSession() {
    return cleanSession;
  }

  /**
   * cleanSession.
   * 
   * @param cleanSession the cleanSession to set
   */
  public void setCleanSession(boolean cleanSession) {
    this.cleanSession = cleanSession;
  }

  public ProcessCacher<TBoxLog> getProcessCacher() {
    return new UnmodifiedProcessCacher<TBoxLog>(processCacher);
  }

  public void setProcessCacher(ProcessCacher<TBoxLog> processCacher) {
    this.processCacher = processCacher;
  }

  public void setTboxService(TBoxLogService tboxService) {
    this.tboxService = tboxService;
  }

  public AppConfig getAppConfig() {
    return appConfig;
  }

  /////////////////////////////////////////////////////////////////////////////

  private volatile MqttClient mqttlient;
  private final static Logger LOGGER = LoggerFactory.getLogger(RMLogMQTTClient.class);
  private Future<?> storeFuture, reconnectFuture;
  //////////////////////////////////////////////////////////////////////////
}
