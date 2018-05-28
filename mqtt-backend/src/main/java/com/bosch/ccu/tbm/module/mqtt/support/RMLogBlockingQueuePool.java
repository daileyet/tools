/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.module.mqtt.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bosch.ccu.tbm.model.TBoxLog;
import com.bosch.ccu.tbm.util.DateUtil;

/**
 * ClassName: RMLogBlockingQueuePool <br>
 * date: May 21, 2018 1:49:01 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public class RMLogBlockingQueuePool implements BlockingQueuePool<TBoxLog> {
  private final static Logger LOGGER = LoggerFactory.getLogger(RMLogBlockingQueuePool.class);
  private final static int QUEUE_EMPTY_ALIVE_TIME = 2 * 60 * 1000;
  private final static int QUEUE_MAX_SIZE = 2*10000;
  
  

  private final Map<Serializable, BlockingQueue<TBoxLog>> poolData;
  private final Map<Serializable, StateInfo> poolState;

  private final int queueEmptyAliveTime;
  private final int queueMaxSize;
  
  public RMLogBlockingQueuePool() {
    this(QUEUE_EMPTY_ALIVE_TIME,QUEUE_MAX_SIZE);
  }

  public RMLogBlockingQueuePool(final int queueEmptyAliveTime,int queueMaxSize) {
    this.queueEmptyAliveTime = queueEmptyAliveTime;
    this.queueMaxSize = queueMaxSize;
    poolData = new ConcurrentHashMap<>();
    poolState = new ConcurrentHashMap<>();
  }


  @Override
  public void offer(Serializable tag, TBoxLog bean) {
    BlockingQueue<TBoxLog> queue = poolData.get(tag);
    if (queue == null) {
      queue = new LinkedBlockingQueue<>(queueMaxSize);
      poolData.put(tag, queue);
    }
    queue.offer(bean);
    updatePoolState(tag);
  }

  private void updatePoolState(Serializable tag) {
    StateInfo stateInfo = poolState.get(tag);
    if (stateInfo == null) {
      stateInfo = new RMLogStateInfo(tag);
    } else {
      stateInfo.lastedProduced = DateUtil.now();
    }
    poolState.put(tag, stateInfo);
  }

  @Override
  public void pollAll(long timeout, TimeUnit unit, final PoolConsumer<TBoxLog> beanConsumer)
      throws InterruptedException {
    StringBuilder sb = new StringBuilder();
    poolData.entrySet().parallelStream().forEach((entry) -> {
      final Serializable tag = entry.getKey();
      final BlockingQueue<TBoxLog> queue = entry.getValue();
      try {
        TBoxLog bean = queue.poll(timeout, unit);
        if (bean == null) {
          StateInfo state = poolState.get(tag);
          if (state != null
              && DateUtil.isDiffGreaterGapWithNow(state.lastedProduced, queueEmptyAliveTime)) {
            poolData.remove(tag);
            poolState.remove(tag);
            LOGGER.info("Remove empty queue {} for max alive time has been out.", tag);
            beanConsumer.flush();
          }
        } else {
          beanConsumer.accept(bean);
        }
      } catch (InterruptedException e) {
        sb.append(tag).append(",");
      } catch (Exception e) {
        LOGGER.error("Failed to save TBox log.", e);
      }
    });
    if (sb.length() > 0) {
      throw new InterruptedException("Failed to save TBox log for:" + sb.toString());
    }
  }


  @Override
  public Map<Serializable, StateInfo> getPoolStates() {
    return Collections.unmodifiableMap(poolState);
  }

  class RMLogStateInfo extends StateInfo {
    private volatile long lastedProduced;
    private final Serializable tag;

    public RMLogStateInfo(Serializable tag) {
      this.tag = tag;
      this.lastedProduced = DateUtil.now();
    }

    protected BlockingQueue<TBoxLog> queueRef() {
      return poolData.get(tag);
    }

    public long getLastedProduced() {
      return lastedProduced;
    }

    public int getRefQueueSize() {
      return queueRef() == null ? -1 : queueRef().size();
    }
  }
}
