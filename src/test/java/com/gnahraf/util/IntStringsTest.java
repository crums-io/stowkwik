/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;


import static org.junit.Assert.*;

import static com.gnahraf.util.IntStrings.*;

import org.junit.Test;

/**
 * 
 */
public class IntStringsTest {
  
  @Test
  public void testIsHex() {
    String[] hex = { "fE", "00", "10", "0123456789abcdefABCDEF" };
    String[] notHex = { "0x5", "5G", "0g" };
    for (String n : hex)
      assertTrue(n, isHex(n));
    for (String g : notHex)
      assertFalse(g, isHex(g));
  }

}
