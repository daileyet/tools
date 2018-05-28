/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jolbox.bonecp.BoneCP;
import com.openthinks.libs.utilities.lookup.Lookups;

/**
 * ClassName: DBHelper <br>
 * date: May 23, 2018 9:01:06 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public final class DBHelper {

  public static final Connection getConnection() throws SQLException {
    BoneCP connectionPool = Lookups.global().lookup(BoneCP.class);
    return connectionPool.getConnection();
  }

  static final String FETCH_TBOXLOG_TABLE_NAME =
      "SELECT * FROM tbox_log_table order by table_name desc limit 1";

  public static final String fetchTBoxLogTableName() throws SQLException {
    Connection conn = getConnection();
    String tableName = null;
    PreparedStatement ps = conn.prepareStatement(FETCH_TBOXLOG_TABLE_NAME);
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      tableName = rs.getString(1);
    }
    rs.close();
    conn.close();
    return tableName;
  }

}
