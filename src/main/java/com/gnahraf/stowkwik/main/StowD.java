/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik.main;

import static com.gnahraf.util.main.Args.getValue;
import static com.gnahraf.util.main.Args.getValues;
import static com.gnahraf.util.main.Args.getTrue;
import static com.gnahraf.util.main.Args.help;

import java.io.File;
import java.io.PrintStream;

import com.gnahraf.stowkwik.FileManager;
import com.gnahraf.stowkwik.FileStower;
import com.gnahraf.util.main.TablePrint;

/**
 * 
 */
public class StowD {

  private StowD() {  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length == 0)
      exitInputError("No arguments specified");
    
    if (help(args))
      printHelp();
    
    try {
      
      File root;
      {
        String dir = getRequiredParam(args, DIR);
        root = new File(dir);
        boolean create = getTrue(args, NEW);
        if (!root.isDirectory()) {
          if (create) {
            if (!root.mkdirs())
              exitInputError("Failed to create directory: " + DIR + "=" + dir);
          } else
            exitInputError("Not an existing directory: " + DIR + "=" + dir);
        }
      }
      
      String ext = getRequiredParam(args, EXT);
      
      String algo = getValue(args, ALGO, FileManager.DEFAULT_HASH_ALGO);
      
      String[] stowDirectories = getValues(args, STOW);
      
      try (FileStower manager = new FileStower(root, ext, algo)) {
        
        if (stowDirectories.length == 0)
          manager.addDefaultStowDirectory();
        
        else for (String dir : stowDirectories)
          manager.addStowDirectory(new File(dir));
        
        manager.joinClose();
      } catch (InterruptedException ix) {
        System.out.println("Closing by interrupt..");
      }
      
    } catch (IllegalArgumentException x) {
      exitInputError(errorMessage(x));
    } catch (Exception e) {
      printError(errorMessage(e));
      System.exit(2);
    }

    System.out.println("Bye.");
  }
  
  
  private static String errorMessage(Exception e) {
    String message = e.getMessage();
    return e.getClass().getSimpleName() + (message == null ? "" : " "  + message);
  }
  



  private static String getRequiredParam(String[] args, String param) {
    String value = getValue(args, param, null);
    if (value == null || value.isEmpty())
      exitInputError("Missing required " + param + "={value} parameter");
    return value;
  }
  
  
  private static void printError(String message) {
    System.err.println("[ERROR] " + message);
    System.err.println();
  }


  private static void exitInputError(String message) {
    printError(message);
    printUsage(System.err);
    System.err.println("Input 'help' for a bit more details");
    System.err.println();
    System.exit(1);
  }


  
  private static void printHelp() {
    System.out.println();
    System.out.println("Description:");
    System.out.println();
    System.out.println("Watches one or more \"stow\" directories for newly dropped files which are then");
    System.out.println("archived (moved) under a hex tree directory structure. Files are named after the");
    System.out.println("crypotgraphic hash of their contents. Note you should only ever MOVE a file to");
    System.out.println("a stow directory; you should never again write to a file after it's been moved");
    System.out.println("there. It's easy to forget: this means NEVER COPY files directly into a stow");
    System.out.println("directory.");
    System.out.println();
    printUsage(System.out);
    printLegend(System.out);
    System.exit(0);
  }
  
  private static void printUsage(PrintStream out) {
    out.println("Usage:");
    out.println();
    out.println("Arguments are specified as 'name=value' pairs.");
    out.println();
    TablePrint table = new TablePrint(out, 10, 67, 3);
    table.setIndentation(1);
    table.printRow(EXT + "=*",  "the file extension (include the period)", REQ);
    out.println();
    table.printRow(DIR + "=*",  "store root directory. Must already exist, unless..", REQ);
    out.println();
    table.printRow(NEW + "=true",  "create the root directory; ignoring case, any value", OPT);
    table.printRow(null,        "other than 'true' means 'false'", null);
    out.println();
    table.printRow(STOW + "=*", "path to stow directory (default {store root}/" + FileStower.DEFAULT_STOW_DIR + " )", OPT);
    table.printRow(null,        "Multiple directories may be specified as separate arguments.", null);
    out.println();
    table.printRow(ALGO + "=*", "cryptographic hash algo (e.g. MD5, SHA-1, SHA-256). Default " + FileManager.DEFAULT_HASH_ALGO, OPT);
    
    out.println();
  }
  

  
  private static void printLegend(PrintStream out) {
    TablePrint legend = new TablePrint(out, 5, 65);
    legend.setIndentation(11);
    legend.println("______");
    legend.println("Legend:");
    out.println();
    legend.printRow("*", "denotes an arbitrary input value (not a wildcard)");
    legend.printRow(REQ, "denotes a required name={value} arg");
    out.println();
  }
  

  private final static String EXT = "ext";
  private final static String DIR = "dir";
  private final static String NEW = "new";
  private final static String STOW = "stow";
  private final static String ALGO = "algo";

  
  private final static String REQ = "R";
//  private final static String REQ_PLUS = "R+";
  private final static String OPT = "";
  
}
