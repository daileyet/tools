/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.module.mqtt.support;

import java.util.List;

/**
 * ClassName: ProcessCacher <br>
 * date: May 23, 2018 10:10:42 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public interface ProcessCacher<T> {

  boolean tryCache(T logBean);

  List<T> purge();

  int size();

  public boolean isEmpty();

  boolean isActive();
}
