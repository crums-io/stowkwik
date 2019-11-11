/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik.main;


import static com.gnahraf.util.main.Args.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.gnahraf.io.HexPathTree;
import com.gnahraf.io.HexPathTree.Entry;
import com.gnahraf.util.IntegralStrings;
import com.gnahraf.util.main.TablePrint;

/**
 * 
 */
public class Storex {
  
  public final static String PROG_NAME = Storex.class.getSimpleName().toLowerCase(Locale.ROOT);

  private final int printOpts;
  
  private Storex(int printOpts) {
    this.printOpts = printOpts;
  }

  
  void printEntry(Entry entry, PrintStream out) throws UncheckedIOException {
    if (flagged(HEX_OPT))
      out.println(entry.hex);
    if (flagged(PATH_OPT))
      out.println(entry.file);
    if (flagged(CONTENTS_OPT)) {
      try (FileReader reader = new FileReader(entry.file)) {
        CharBuffer buffer = CharBuffer.allocate(4096);
        while (true) {
          buffer.clear();
          int count = reader.read(buffer);
          if (count == -1)
            break;
          buffer.flip();
          out.append(buffer);
        }
      } catch (IOException iox) {
        throw new UncheckedIOException(iox);
      }
      out.println();
    }
  }
  
  boolean flagged(int opt) {
    return (printOpts & opt) != 0;
  }
  
  
  
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
        if (!root.isDirectory())
          exitInputError("Not an existing directory: " + DIR + "=" + dir);
      }
      
      String ext = getRequiredParam(args, EXT);
      String hex = getValue(args, HEX, null);
      String start = getValue(args, START, null);
      
      if (hex == null && start == null || hex != null && start != null)
        exitInputError("Either " + HEX + "={value} or " + START + "={value} must be specified");
      
      boolean unique = hex != null;
      final String prefix = IntegralStrings.canonicalizeHex(unique ? hex : start);
      
      int printOpts = getIntValue(args, PRINT, DEFAULT_OPTS);
      if (printOpts < 1 || printOpts > MAX_OPTS)
        exitInputError(PRINT + "=" + printOpts + " outside valid range [1," + MAX_OPTS + "]");

      final Storex instance = new Storex(printOpts);
      
      HexPathTree hexPath = new HexPathTree(root, ext);
      
      if (unique) {
        
        List<Entry> hits = hexPath.streamStartingFrom(prefix)
            .limit(2).filter(e -> e.hex.startsWith(prefix)).collect(Collectors.toList());
        
        switch (hits.size()) {
        
        case 0:
          
          exitInputError("Entry with prefix '" + prefix + "' not found");
        
        case 1:
          
          // happy path
          instance.printEntry(hits.get(0), System.out);
          return;
        
        case 2:
          
          exitInputError("Ambiguous: multiple entries with prefix '" + prefix + "' found");
        
        default:
          throw new AssertionError(hits.toString());
        }
        
      } else {

        int limit = getIntValue(args, LIMIT, DEFAULT_LIMIT);
        
        hexPath.streamStartingFrom(prefix).limit(limit).forEach(e -> instance.printEntry(e, System.out));
      }
    
    } catch (IllegalArgumentException x) {
      String message = x.getMessage();
      message = x.getClass().getSimpleName() + (message == null ? "" : " "  + message);
      exitInputError(message);
    
    } catch (Exception e) {
      String message = e.getMessage();
      message = e.getClass().getSimpleName() + (message == null ? "" : " "  + message);
      printError(message);
      System.exit(2);
    }
    
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
    System.out.println("Outputs one or an ordered list of files managed under a hex path directory");
    System.out.println("structure. This is a read only interface.");
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
    TablePrint table = new TablePrint(out, 10, 65, 3);
    table.setIndentation(1);
    table.printRow(EXT + "=*", "the file extension (include the period)", REQ);
    table.printRow(DIR + "=*", "store root directory (must already exist)", REQ);
    table.printRow(HEX + "=*", "outputs a single entry using this unambiguous prefix", REQ_STAR);
    table.printRow(null,       "of its hexadecimal ID", null);
    table.printRow(START + "=*", "lists entries in ascending order of hex IDs starting from", REQ_STAR);
    table.printRow(null,         "given hex (prefix OK)", null);
    table.printRow(PRINT + "=*", "sets what's to be output. Valid values range in [1," + MAX_OPTS + "]", OPT);
    table.printRow(null,         "(default " + DEFAULT_OPTS + ") and are bit field combinations of the following", null);
    {
      TablePrint subTable = new TablePrint(out, 5, 65);
      subTable.setIndentation(10);
      subTable.printRow(1, "hex ID");
      subTable.printRow(2, "file path");
      subTable.printRow(4, "file contents (assuming it's text)");
    }
    table.printRow(LIMIT + "=*", "max number of entries to output (default " + DEFAULT_LIMIT + ")", OPT);
    
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
    legend.printRow(REQ_STAR, "denotes a required one-of-many name={value} arg");
    legend.printRow(null, "(grouped together in adjacent rows)");
    out.println();
  }

  
  private final static String EXT = "ext";
  private final static String DIR = "dir";
  private final static String HEX = "hex";
  private final static String START = "start";
  private final static String LIMIT = "limit";
  private final static String PRINT = "print";
  
  private final static String REQ = "R";
  private final static String REQ_STAR = "R*";
  private final static String OPT = "";
  
  
  private final static int HEX_OPT = 1;
  private final static int PATH_OPT = 2;
  private final static int CONTENTS_OPT = 4;
  private final static int DEFAULT_OPTS = HEX_OPT + PATH_OPT;
  private final static int MAX_OPTS = 2 * CONTENTS_OPT - 1;
  private final static int DEFAULT_LIMIT = 10;

}
