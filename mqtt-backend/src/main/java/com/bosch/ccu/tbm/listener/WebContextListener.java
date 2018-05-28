/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bosch.ccu.tbm.AppBootstrap;
import com.bosch.ccu.tbm.util.LifeCycle;

/**
 * ClassName: WebContextListener <br>
 * date: May 23, 2018 5:28:13 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
@WebListener
public class WebContextListener implements ServletContextListener {
  final static Logger LOGGER = LoggerFactory.getLogger(AppBootstrap.class);
  LifeCycle appBootstrap;

  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    try {
      appBootstrap = AppBootstrap.getInstance();
      appBootstrap.initial();
      appBootstrap.start();
    } catch (Throwable e) {
      LOGGER.error("Failed to initial context", e);
    }
  }


  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    try {
      if (appBootstrap != null) {
        appBootstrap.stop();
        appBootstrap.destory();
      }
    } catch (Throwable e) {
      LOGGER.error("Failed to destory context completed:", e);
    }
  }

}
