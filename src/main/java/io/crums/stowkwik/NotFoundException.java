/*
 * Copyright 2017 Babak Farhang
 */
package io.crums.stowkwik;


/**
 * Didn't find what you where expecting.
 */
@SuppressWarnings("serial")
public class NotFoundException extends IllegalArgumentException {

  
  public NotFoundException() {
    // TODO Auto-generated constructor stub
  }

  public NotFoundException(String message) {
    super(message);
  }

  
  public NotFoundException(Throwable cause) {
    super(cause);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
