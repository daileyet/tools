/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved. This software is property of
 * Robert Bosch (Suzhou). Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.bosch.ccu.tbm.module.mqtt.support;

import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: FixedSizeArrayList <br>
 * Function: TODO FUNCTION description of this class. <br>
 * Reason: TODO why you add this class?(Optional). <br>
 * date: May 23, 2018 10:41:44 PM <br>
 * 
 * @author dailey.dai@cn.bosch.com DAD2SZH
 * @version
 * @since JDK 1.8
 */
public class FixedSizeArrayList<E> extends AbstractList<E> {
  private final int capacity;
  private final AtomicInteger size;
  private final Object[] elementData;

  public FixedSizeArrayList(int capacity) {
    super();
    this.size = new AtomicInteger(0);
    this.capacity = capacity;
    this.elementData = new Object[capacity];
  }

  @Override
  public boolean add(E e) {
    if (size.get() >= capacity)
      return false;
    synchronized (this) {
      try {
        elementData[size.getAndIncrement()] = e;
      } catch (ArrayIndexOutOfBoundsException ex) {
        return false;
      }
      return size.get()<capacity;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public E get(int index) {
    return (E) elementData[index];
  }

  @Override
  public synchronized int size() {
    return size.get();
  }

}
