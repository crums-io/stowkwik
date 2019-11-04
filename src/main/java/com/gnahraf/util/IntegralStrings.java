/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;


import java.util.List;
import java.util.Locale;

/**
 * 
 */
public class IntegralStrings {
  
  private IntegralStrings() {  }
  
  

  
  private final static String[] BYTE_HEX_VALS;
  
  /**
   * Ordered list of 2 character wide, lowercase hex strings representing
   * the byte values from 0 to 255. Immutable.
   */
  public final static List<String> BYTE_HEX_VALUES;
  
  static {
    BYTE_HEX_VALS = new String[256];
    for (int i = 0; i < 16; ++i)
      BYTE_HEX_VALS[i] = "0" + Integer.toHexString(i);
    for (int i = 16; i < 256; ++i)
      BYTE_HEX_VALS[i] = Integer.toHexString(i);
    
    BYTE_HEX_VALUES = Lists.asReadOnlyList(BYTE_HEX_VALS);
  }
  
  
  
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
  
  
  public static String toHex(byte b) {
    return BYTE_HEX_VALS[((int) b) & 0xff];
  }
  
  
  public static String toHex(byte[] bytes) {
    int len = bytes.length;
    if (len == 0)
      throw new IllegalArgumentException("empty array: " + bytes);
    StringBuilder string = new StringBuilder(2 * len);
    for (int i = 0; i < len; ++i)
      string.append(toHex(bytes[i]));
    return string.toString();
  }

}
