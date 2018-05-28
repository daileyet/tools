package com.bosch.ccu.tbm.module.mqtt.support;

import java.util.concurrent.BlockingQueue;
import com.bosch.ccu.tbm.util.DateUtil;

public abstract class StateInfo {
  protected volatile long lastedProduced;

  public StateInfo() {
    this.lastedProduced = DateUtil.now();
  }

  protected abstract BlockingQueue<?> queueRef();

  public long getLastedProduced() {
    return lastedProduced;
  }

  public int getRefQueueSize() {
    return queueRef() == null ? -1 : queueRef().size();
  }
}
