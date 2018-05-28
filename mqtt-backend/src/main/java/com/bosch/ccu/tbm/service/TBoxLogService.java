/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import com.bosch.ccu.tbm.AppConfig;
import com.bosch.ccu.tbm.model.TBoxLog;
import com.bosch.ccu.tbm.util.DBHelper;
import com.bosch.ccu.tbm.util.DateUtil;
import com.openthinks.libs.utilities.lookup.Lookups;

/**
 * ClassName: TBoxLogService <br>
 * date: May 23, 2018 8:56:02 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public class TBoxLogService {
  public final static int SCALE_FACTOR = 1000;
  public final static String BASE_TABLE_NAME = "tbox_log";
  public final static String INSERTSQL = "insert into " + BASE_TABLE_NAME
      + "(Id,imsi,log_time,msg,last_modify,mac) values(?,?,?,?,?,?)";
  private static volatile String tableName = BASE_TABLE_NAME;

  public static synchronized void setTableName(String tableName) {
    TBoxLogService.tableName = tableName;
  }

  private static synchronized String getInsertSQL() {
    return INSERTSQL.replace(BASE_TABLE_NAME, tableName);
  }

  private final AtomicLong seed = new AtomicLong(0);
  private int scale_factor = SCALE_FACTOR;

  public TBoxLogService() {
    try {
      scale_factor = Lookups.global().lookup(AppConfig.class).getFastQueryFactor();
    } catch (Exception e) {
    }
  }
  
  public boolean insert(TBoxLog tBoxLog) throws SQLException {
    Connection conn = DBHelper.getConnection();
    PreparedStatement ps = conn.prepareStatement(getInsertSQL());
    ps.setLong(1,(DateUtil.now() * scale_factor) + seed.getAndIncrement());
    ps.setString(2, tBoxLog.getImsi());
    ps.setString(3, tBoxLog.getLogTime() == null ? null : DateUtil.format(tBoxLog.getLogTime()));
    ps.setString(4, tBoxLog.getMsg());
    ps.setString(5,
        tBoxLog.getLastModify() == null ? null : DateUtil.format(tBoxLog.getLastModify()));
    ps.setString(6, tBoxLog.getMac());
    int impact = ps.executeUpdate();
    ps.close();
    conn.close();
    return impact > 0;
  }


  public int[] batchInsert(List<TBoxLog> tboxLogs) throws SQLException {
    Connection conn = DBHelper.getConnection();
    PreparedStatement ps = conn.prepareStatement(getInsertSQL());
    for (TBoxLog tBoxLog : tboxLogs) {
      ps.setLong(1,(DateUtil.now() * scale_factor) + seed.getAndIncrement());
      ps.setString(2, tBoxLog.getImsi());
      ps.setString(3, tBoxLog.getLogTime() == null ? null : DateUtil.format(tBoxLog.getLogTime()));
      ps.setString(4, tBoxLog.getMsg());
      ps.setString(5,
          tBoxLog.getLastModify() == null ? null : DateUtil.format(tBoxLog.getLastModify()));
      ps.setString(6, tBoxLog.getMac());
      ps.addBatch();
    }
    int[] rsts = ps.executeBatch();
    ps.close();
    conn.close();
    return rsts;
  }
  

}
