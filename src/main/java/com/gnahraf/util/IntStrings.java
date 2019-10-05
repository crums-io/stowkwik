/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;

import java.util.Locale;

/**
 * 
 */
public class IntStrings {
  
  private IntStrings() {  }
  
  
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
  
  
  public static String canonicalizeHex(String hex) {
    if (!isHex(hex))
      throw new IllegalArgumentException(hex);
    
    return hex.toLowerCase(Locale.ROOT);
  }

}
