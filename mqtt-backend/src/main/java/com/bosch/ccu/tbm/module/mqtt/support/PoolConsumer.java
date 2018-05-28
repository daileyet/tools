package com.bosch.ccu.tbm.module.mqtt.support;

import java.util.function.Consumer;

public interface PoolConsumer<T> extends Consumer<T> {

  public void flush();
  
}
