/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved.
 * This software is property of Robert Bosch (Suzhou). 
 * Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.module.mqtt.support;

/** 
 * ClassName: RMLogTopicResolve <br> 
 * date: May 21, 2018 2:24:33 PM <br> 
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH 
 * @since JDK 1.8
 */
@FunctionalInterface
public interface RMLogTopicResolve {

  String extractIMSI(String topic);

}
