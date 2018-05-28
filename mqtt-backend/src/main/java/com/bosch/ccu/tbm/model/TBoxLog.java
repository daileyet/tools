/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved.
 * This software is property of Robert Bosch (Suzhou). 
 * Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.model;

import java.util.Date;

/** 
 * ClassName: TBoxLog <br> 
 * date: May 23, 2018 4:41:28 PM <br> 
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH 
 * @since JDK 1.8
 */
public class TBoxLog {
  private Long id;
  private String imsi;
  private Date logTime;
  private String msg;
  private Date lastModify;
  private String mac;
  /**
   * id. 
   * @return the id
   */
  public Long getId() {
    return id;
  }
  /** 
   * id. 
   * 
   * @param   id    the id to set 
   */
  public void setId(Long id) {
    this.id = id;
  }
  /**
   * imsi. 
   * @return the imsi
   */
  public String getImsi() {
    return imsi;
  }
  /** 
   * imsi. 
   * 
   * @param   imsi    the imsi to set 
   */
  public void setImsi(String imsi) {
    this.imsi = imsi;
  }
  /**
   * logTime. 
   * @return the logTime
   */
  public Date getLogTime() {
    return logTime;
  }
  /** 
   * logTime. 
   * 
   * @param   logTime    the logTime to set 
   */
  public void setLogTime(Date logTime) {
    this.logTime = logTime;
  }
  /**
   * msg. 
   * @return the msg
   */
  public String getMsg() {
    return msg;
  }
  /** 
   * msg. 
   * 
   * @param   msg    the msg to set 
   */
  public void setMsg(String msg) {
    this.msg = msg;
  }
  /**
   * lastModify. 
   * @return the lastModify
   */
  public Date getLastModify() {
    return lastModify;
  }
  /** 
   * lastModify. 
   * 
   * @param   lastModify    the lastModify to set 
   */
  public void setLastModify(Date lastModify) {
    this.lastModify = lastModify;
  }
  /**
   * mac. 
   * @return the mac
   */
  public String getMac() {
    return mac;
  }
  /** 
   * mac. 
   * 
   * @param   mac    the mac to set 
   */
  public void setMac(String mac) {
    this.mac = mac;
  }
  
  
}
