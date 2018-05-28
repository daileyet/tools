/* Copyright � 2015 Oracle and/or its affiliates. All rights reserved. */
package com.bosch.ccu.tbm.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bosch.ccu.tbm.module.mqtt.RMLogMQTTClient;
import com.bosch.ccu.tbm.util.DateUtil;
import com.openthinks.libs.utilities.lookup.Lookups;

@WebServlet(name = "MqttMonitorServlet", urlPatterns = {"/mqtt/status"})
public class MqttMonitorServlet extends HttpServlet {

  private static final long serialVersionUID = -7838126285013021730L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    RMLogMQTTClient mqttClient=Lookups.global().lookup(RMLogMQTTClient.class);
    req.setAttribute("mqtt", mqttClient);
    req.setAttribute("refreshTime", DateUtil.formatNow());
    req.getRequestDispatcher("/jsp/mqtt-status.jsp").forward(req, resp);
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req,resp);
  }


}
