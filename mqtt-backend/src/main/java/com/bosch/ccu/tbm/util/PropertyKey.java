/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
/**
 * ClassName: PropertyValue <br>
 * date: May 23, 2018 8:27:20 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @version
 * @since JDK 1.8
 */
public @interface PropertyKey {
  String value() default "";
  
  String defaultValue() default "";
}
