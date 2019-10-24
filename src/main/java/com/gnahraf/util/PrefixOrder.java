/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;


/**
 * Encapsulates string comparison with prefixes.
 */
public enum PrefixOrder {
  
  /**
   * Before the prefix
   */
  BEFORE,
  /**
   * Starts with the prefix
   */
  AT,
  /**
   * After the prefix
   */
  AFTER;
  
  
  /**
   * Returns the position of the given <tt>string</tt> relative
   * to the specified <tt>prefix</tt>. Note this property is not reflexive.
   */
  public static PrefixOrder compareToPrefix(String string, String prefix) {

    int lexicomp = string.compareTo(prefix);
    PrefixOrder order;

    if (lexicomp < 0)
      order = BEFORE;
    else if (lexicomp == 0 || string.startsWith(prefix))
      order = AT;
    else
      order = AFTER;
    
    return order;
  }
  
  
  public boolean isBefore() {
    return this == BEFORE;
  }
  
  public boolean isAt() {
    return this == AT;
  }
  
  public boolean isAfter() {
    return this == AFTER;
  }

}
