package com.bosch.ccu.tbm.module.mqtt.support;

import java.util.List;

public class UnmodifiedProcessCacher<E> implements ProcessCacher<E> {
  final ProcessCacher<E> inner;

  public UnmodifiedProcessCacher(ProcessCacher<E> inner) {
    super();
    this.inner = inner;
  }

  @Override
  public boolean tryCache(E logBean) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<E> purge() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return inner.size();
  }

  @Override
  public boolean isEmpty() {
    return inner.isEmpty();
  }

  @Override
  public boolean isActive() {
    return inner.isActive();
  }

}
