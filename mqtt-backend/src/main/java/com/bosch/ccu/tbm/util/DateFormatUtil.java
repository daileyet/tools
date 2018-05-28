/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved.
 * This software is property of Robert Bosch (Suzhou). 
 * Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: DateFormatUtil <br>
 * Function: format date to string or parse string to date. <br>
 * Reason: {@link DateFormat} has thread issue in multi-thread situation, use
 * {@link ThreadLocal} to make sure each thread use one instance of
 * {@link DateFormat}. <br>
 * date: Nov 28, 2017 1:38:11 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @since JDK 1.8
 */
public final class DateFormatUtil {

	private static final Map<String, ThreadLocal<DateFormat>> formatCache = new ConcurrentHashMap<>();

	/**
	 * 
	 * getDateFormat:get or create {@link DateFormat} which bind to current thread.
	 * <br>
	 * 
	 * @param pattern
	 *            format pattern
	 * @return {@link DateFormat}
	 * @throws NullPointerException
	 *             - if the given pattern is null
	 * @throws IllegalArgumentException
	 *             - if the given pattern is invalid
	 */
	public static final DateFormat getDateFormat(String pattern) {
		ThreadLocal<DateFormat> threadLocal = formatCache.get(pattern);
		if (threadLocal == null) {
			synchronized (DateFormatUtil.class) {
				threadLocal = formatCache.get(pattern);
				if (threadLocal == null) {
					threadLocal = new ThreadLocal<DateFormat>() {
						@Override
						protected DateFormat initialValue() {
							return new SimpleDateFormat(pattern);
						}
					};
					formatCache.put(pattern, threadLocal);
				}
			}
		}
		return threadLocal.get();
	}

	public static final DateFormat getDateFormat(String pattern, TimeZone timeZone) {
		ThreadLocal<DateFormat> threadLocal = formatCache.get(pattern);
		if (threadLocal == null) {
			synchronized (DateFormatUtil.class) {
				threadLocal = formatCache.get(pattern);
				if (threadLocal == null) {
					threadLocal = new ThreadLocal<DateFormat>() {
						@Override
						protected DateFormat initialValue() {
							DateFormat dateFormat = new SimpleDateFormat(pattern);
							dateFormat.setTimeZone(timeZone);
							return dateFormat;
						}
					};
					formatCache.put(pattern, threadLocal);
				}
			}
		}
		return threadLocal.get();
	}

	public static final String format(String pattern, Date date) {
		return getDateFormat(pattern).format(date);
	}

	public static final Date parse(String pattern, String dateString) throws ParseException {
		return getDateFormat(pattern).parse(dateString);
	}
}
