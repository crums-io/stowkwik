/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.util.main;

import java.util.Arrays;

import com.gnahraf.xcept.NotFoundException;

/**
 *
 */
public class Args {
  
  private final static char EQ = '=';
  private final static String[] HELP = { "help", "-help", "--help", "-h" };

  private Args() {  }
  
  

  public static String getValue(String[] args, String name) {
    String value = getValue(args, name, null);
    if (value == null)
      throw new NotFoundException(name + EQ + ".. in args " + Arrays.asList(args));
    return value;
  }
  
  
  public static String getValue(String[] args, String name, String defaultValue) {
    String searchString = name.toString() + EQ;
    for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith(searchString))
        return args[i].substring(searchString.length());
    }
    return defaultValue;
  }



  public static int getIntValue(String[] args, String name, int defaultValue) {
    String value = getValue(args, name, null);
    try {
      return value == null ? defaultValue : Integer.parseInt(value);
    } catch (NumberFormatException nfx) {
      throw new IllegalArgumentException("while parsing " + name + "=" + value, nfx);
    }
  }
  
  
  public static boolean containsAny(String[] args, String... targets) {
    for (String target : targets)
      if (contains(args, target))
        return true;
    return false;
  }
  
  public static boolean contains(String[] args, String target) {
    for (String arg : args)
      if (arg.equals(target))
        return true;
    return false;
  }
  
  
  public static boolean help(String[] args) {
    return containsAny(args, HELP);
  }

}
