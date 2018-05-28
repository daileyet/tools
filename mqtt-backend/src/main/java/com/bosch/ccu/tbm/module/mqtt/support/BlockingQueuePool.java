/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.module.mqtt.support;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: RMLogBlockingQueuePool <br>
 * date: May 21, 2018 1:26:54 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public interface BlockingQueuePool<T> {

  public void offer(Serializable tag, T bean);

  public void pollAll(long timeout, TimeUnit unit, final PoolConsumer<T> beanConsumer)
      throws InterruptedException;

  public Map<Serializable, StateInfo> getPoolStates();

}
