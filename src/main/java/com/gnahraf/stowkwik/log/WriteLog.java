/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik.log;

import java.io.Closeable;

/**
 * 
 */
public interface WriteLog extends Closeable {
  
  void objectWritten(String id);

}
