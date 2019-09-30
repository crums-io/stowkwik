/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.io;


/**
 * Something's rotten.
 */
@SuppressWarnings("serial")
public class CorruptionException extends IllegalStateException {

  public CorruptionException() {
  }

  public CorruptionException(String s) {
    super(s);
  }

  public CorruptionException(Throwable cause) {
    super(cause);
  }

  public CorruptionException(String message, Throwable cause) {
    super(message, cause);
  }

}
