/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
  public static final String DEFAULT_DATE_FORMAT = "YYYY-MM-dd HH:mm:ss";
  public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("Asia/Shanghai");

  private DateUtil() {}


  public static long now() {
    return System.currentTimeMillis();
  }

  public static boolean isDiffGreaterGap(long compareTime1, long compareTime2, long gap) {
    return Math.abs(compareTime1 - compareTime2) > gap;
  }

  public static boolean isDiffGreaterGapWithNow(long compareTime, long gap) {
    return isDiffGreaterGap(now(), compareTime, gap);
  }
  
  public static boolean isDiffNotGreaterGap(long compareTime1, long compareTime2, long gap) {
    return Math.abs(compareTime1 - compareTime2) <= gap;
  }

  public static boolean isDiffNotGreaterGapWithNow(long compareTime, long gap) {
    return isDiffGreaterGap(now(), compareTime, gap);
  }

  /**
   * 
   * 
   * @return
   */
  public static final String formatNow() {
    return getDateFormat().format(new Date());
  }

  public static final String format(Date date) {
    return getDateFormat().format(date);
  }

  public static final String format(long time) {
    Date date = new Date(time);
    return getDateFormat().format(date);
  }

  private static final DateFormat getDateFormat() {
    DateFormat dateFormat = DateFormatUtil.getDateFormat(DEFAULT_DATE_FORMAT, DEFAULT_TIMEZONE);
    return dateFormat;
  }

  /**
   * 
   * parse:parse the given time string to {@link Date}. <br>
   * 
   * @author dailey.dai@cn.bosch.com DAD2SZH
   * @param source date and time string
   * @return {@link Date}
   * @throws ParseException
   */
  public static final Date parse(String source) throws ParseException {
    return getDateFormat().parse(source);
  }
}
