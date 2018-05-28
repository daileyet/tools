/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.module.mqtt.support;

import java.util.List;
import com.bosch.ccu.tbm.model.TBoxLog;
import com.bosch.ccu.tbm.util.DateUtil;

/**
 * ClassName: RMLogProcessCacher <br>
 * date: May 23, 2018 10:25:05 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public class RMLogProcessCacher implements ProcessCacher<TBoxLog> {
  private static final int DEFAULT_MAXCACHESIZE = 100;
  private static final int CACHE_ALIVE_TIME = 2 * 60 * 1000;
  private final int maxCacheSize;
  private final int cacheAliveTime;
  private volatile List<TBoxLog> mainCache;
  private volatile long lastActiveTimestamp = 0L;

  public RMLogProcessCacher() {
    this(DEFAULT_MAXCACHESIZE, CACHE_ALIVE_TIME);
  }

  public RMLogProcessCacher(final int maxCacheSize, final int cacheAliveTime) {
    this.maxCacheSize = maxCacheSize;
    this.cacheAliveTime = cacheAliveTime;
    this.mainCache = new FixedSizeArrayList<>(maxCacheSize);
  }

  /**
   * 
   * @see com.bosch.ccu.tbm.module.mqtt.support.ProcessCacher#tryCache(com.bosch.ccu.tbm.model.TBoxLog)
   */
  @Override
  public boolean tryCache(TBoxLog logBean) {
    boolean isAdded = mainCache.add(logBean);
    lastActiveTimestamp = DateUtil.now();
    return isAdded;
  }

  /**
   * 
   * @see com.bosch.ccu.tbm.module.mqtt.support.ProcessCacher#purge()
   */
  @Override
  public synchronized List<TBoxLog> purge() {
    List<TBoxLog> purged = mainCache;
    this.mainCache = new FixedSizeArrayList<>(maxCacheSize);
    return purged;
  }

  @Override
  public synchronized int size() {
    return mainCache.size();
  }

  public synchronized boolean isEmpty() {
    return mainCache == null || mainCache.size() == 0;
  }

  public long getLastActiveTimestamp() {
    return lastActiveTimestamp;
  }

  @Override
  public boolean isActive() {
    return DateUtil.isDiffNotGreaterGapWithNow(lastActiveTimestamp, cacheAliveTime);
  }

}
