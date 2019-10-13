/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;

import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

/**
 * 
 */
public class IntegralStrings {
  
  private IntegralStrings() {  }
  
  
  public static boolean isHex(String number) {
    int i = number.length();
    if (i == 0)
      return false;
    
    while (i-- > 0) {
      char c = number.charAt(i);
      if (c < '0')
        break;
      if (c <= '9')
        continue;
      
      if (c < 'A')
        break;
      if (c <= 'F')
        continue;
      
      if (c < 'a' || c > 'f')
        break;
    }
    return i == -1;
  }
  
  
  public static boolean isLowercaseHex(String number) {
    int i = number.length();
    if (i == 0)
      return false;
    
    while (i-- > 0) {
      char c = number.charAt(i);
      if (c < '0')
        break;
      if (c <= '9')
        continue;
      
//      if (c < 'A')
//        break;
//      if (c <= 'F')
//        continue;
      
      if (c < 'a' || c > 'f')
        break;
    }
    return i == -1;
  }
  
  
  public static String canonicalizeHex(String hex) {
    if (!isHex(hex))
      throw new IllegalArgumentException(hex);
    
    return hex.toLowerCase(Locale.ROOT);
  }
  
  
  
  public static String toHex(byte[] bytes) {
    if (bytes == null || bytes.length == 0)
      throw new IllegalArgumentException("identifier " + bytes);
    return DatatypeConverter.printHexBinary(bytes).toLowerCase(Locale.ROOT);
  }

}
