/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik.main;


import static com.gnahraf.util.main.Args.*;

import java.util.Locale;

/**
 * 
 */
public class Storex {
  
  public final static String PROG_NAME = Storex.class.getSimpleName().toLowerCase(Locale.ROOT);

  private Storex() {  }

  
  
  
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (help(args)) {
      printUsage();
    }
  }






  private static void printUsage() {
    System.out.println("Usage:");
  }

}
