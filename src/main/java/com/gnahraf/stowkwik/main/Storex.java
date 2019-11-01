/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik.main;


import static com.gnahraf.util.main.Args.*;

import java.io.File;
import java.io.PrintStream;
import java.util.Locale;

import com.gnahraf.io.HexPathTree;
import com.gnahraf.util.IntegralStrings;
import com.gnahraf.util.main.TablePrint;

/**
 * 
 */
public class Storex {
  
  public final static String PROG_NAME = Storex.class.getSimpleName().toLowerCase(Locale.ROOT);
  
  private final static String EXT = "ext";
  private final static String DIR = "dir";
  private final static String HEX = "hex";

  private Storex() {  }

  
  
  
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length == 0)
      exit("No arguments specified");
    
    if (help(args))
      exit(null);
    
    File root;
    {
      String dir = getRequiredParam(args, DIR);
      root = new File(dir);
      if (!root.isDirectory())
        exit("Not an existing directory: " + DIR + "=" + dir);
    }
    
    String ext = getRequiredParam(args, EXT);
    String hex = getRequiredParam(args, HEX);
    
    if (!IntegralStrings.isHex(hex))
      exit("Invalid hex " + hex);
    
    hex = IntegralStrings.canonicalizeHex(hex);
    

    HexPathTree hexPath = new HexPathTree(root, ext);
    HexPathTree.Cursor cursor = hexPath.newCursor();
    
    try {
    
      cursor.advanceToPrefix(hex);
      if (cursor.hasRemaining()) {
        System.out.println(cursor.getHeadHex());

        System.out.println(cursor.getHeadFile());
      }
    
    } catch (IllegalArgumentException x) {
      String message = x.getMessage();
      if (message == null)
        message = x.getClass().getSimpleName();
      printError(message);
      System.exit(1);
    }
    
  }


  private static String getRequiredParam(String[] args, String param) {
    String value = getValue(args, param);
    if (value == null || value.isEmpty())
      exit("Missing required " + param + "={value} parameter");
    return value;
  }
  
  
  private static void printError(String message) {
    System.err.println("[ERROR] " + message);
    System.err.println();
  }


  private static void exit(String message) {
    boolean error = message != null;
    if (error)
      printError(message);
    printUsage(error ? System.err : System.out);
    System.exit(error ? 1 : 0);
  }

  
  private static void printUsage(PrintStream out) {
    out.println("Usage:");
    out.println("Parameters are specified as 'name=value' pairs. Below, *'s denote arbitrary values (not wildcards).");
    out.println();
    TablePrint table = new TablePrint(out, 15, 60, 3);
    table.setIndentation(2);
    table.printRow(EXT + "=*", "the file extension (include the period)", REQ);
    table.printRow(DIR + "=*", "store root directory (must already exist)", REQ);
    table.printRow(HEX + "=*", "hexadecimal id (unambiguous prefix OK)", REQ);
    out.println();
  }
  
  
  private final static String REQ = "R";
  private final static String OPT = "";

}
