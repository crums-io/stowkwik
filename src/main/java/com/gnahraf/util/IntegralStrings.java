/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;


import java.nio.ByteBuffer;
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

  
  public static byte[] hexToBytes(CharSequence hex) {
    return hexToBytes(hex, null);
  }
  
  public static byte[] hexToBytes(CharSequence hex, byte[] out) {
    int len = hex.length();
    if ((len & 1) != 0)
      throw new IllegalArgumentException("odd hex string length: " + hex);
    if (out == null)
      out = new byte[len / 2];
    else if (out.length != len / 2)
      throw new IllegalArgumentException("out.length != " + (len / 2) + " (hex.length()/2)");
    
    for (int index = 0; index < len; index += 2) {
      int hi = Character.digit(hex.charAt(index), 16);
      int lo = Character.digit(hex.charAt(index + 1), 16);
      
      if (hi == -1 || lo == -1)
        throw new IllegalArgumentException("not hex: " + hex);
      
      out[index / 2] = (byte) ((hi << 4) + lo);
    }
    
    return out;
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
  
  
  /**
   * Returns a hex string representing the remaining contents of the
   * given buffer. The positional state of the buffer is never modified.
   * 
   * @param buffer  with remaining bytes
   * 
   * @return lowercase hex string
   */
  public static String toHex(ByteBuffer buffer) {
    int len = buffer.remaining();
    if (len == 0)
      throw new IllegalArgumentException("empty buffer: " + buffer);
    StringBuilder string = new StringBuilder(2 * len);
    for (int i = buffer.position(); i < buffer.limit(); ++i)
      string.append(toHex(buffer.get(i)));
    return string.toString();
  }

}
