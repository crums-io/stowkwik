/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
@SuppressWarnings("serial")
public class EasyList<T> extends ArrayList<T> {

  public EasyList() {
  }

  public EasyList(int initialCapacity) {
    super(initialCapacity);
  }

  public EasyList(Collection<T> c) {
    super(c);
  }
  
  
  public T first() throws IndexOutOfBoundsException {
    return get(0);
  }
  
  
  public T last() throws IndexOutOfBoundsException {
    return get(size() - 1);
  }
  
  
  public T removeLast() throws IndexOutOfBoundsException {
    return remove(size() - 1);
  }

}
