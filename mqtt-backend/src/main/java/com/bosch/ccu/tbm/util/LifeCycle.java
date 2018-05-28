/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved.
 * This software is property of Robert Bosch (Suzhou). 
 * Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.util;

/** 
 * ClassName: LifeCycle <br> 
 * date: May 23, 2018 9:51:54 PM <br> 
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH 
 * @since JDK 1.8
 */
public interface LifeCycle {

  void destory();

  void stop();

  void start();

  void initial();

}
