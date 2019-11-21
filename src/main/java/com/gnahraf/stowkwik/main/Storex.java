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
import com.gnahraf.stowkwik.log.PlainTextWriteLogReader;
import com.gnahraf.stowkwik.log.WriteLogs;
import com.gnahraf.util.IntegralStrings;
import com.gnahraf.util.main.TablePrint;

/**
 * 
 */
public class Storex {
  
  public final static String PROG_NAME = Storex.class.getSimpleName().toLowerCase(Locale.ROOT);

  private int printOpts;
  
  private Storex(int printOpts) {
    this.printOpts = printOpts;
  }

  
  void printEntry(Entry entry, PrintStream out) throws UncheckedIOException {
    printEntry(null, entry, out);
  }

  
  void printEntry(String timestamp, Entry entry, PrintStream out) throws UncheckedIOException {
    if (timestamp != null && flagged(LOG_DATE_OPT))
      out.println(timestamp);
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
      String hex, start, log;
      {
        int nullCount = 0;
        hex = getValue(args, HEX, null);
        if (hex == null)
          ++nullCount;
        start = getValue(args, START, null);
        if (start == null)
          ++nullCount;
        log = getValue(args, LOG, null);
        if (log == null)
          ++nullCount;
        
        if (nullCount != 2)
          exitInputError("One of " + HEX + "=.., " + START + "=.., or " + LOG + "=.. must be specified");
      }
      
      int limit = getIntValue(args, LIMIT, DEFAULT_LIMIT);
      
      
      
      int printOpts = getIntValue(args, PRINT, DEFAULT_OPTS);
      if (printOpts < 1 || printOpts > MAX_OPTS)
        exitInputError(PRINT + "=" + printOpts + " outside valid range [1," + MAX_OPTS + "]");

      final Storex instance = new Storex(printOpts);
      
      HexPathTree hexPath = new HexPathTree(root, ext);
      
      if (hex != null) {
        
        String prefix = IntegralStrings.canonicalizeHex(hex);
        
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
        
      } else if (start != null) {
        
        String prefix = IntegralStrings.canonicalizeHex(start);
        
        hexPath.streamStartingFrom(prefix).limit(limit).forEach(e -> instance.printEntry(e, System.out));
      
      } else { // (log != null
        
        if (!WriteLogs.hasPlainTextLogFile(hexPath))
          exitInputError("No log file found");
        
        instance.printOpts = getIntValue(args, PRINT, DEFAULT_OPTS_LOG);
        
        try (PlainTextWriteLogReader logReader = WriteLogs.newPlainTextWriteLogReader(root, ext)) {
          
          logReader.listFrom(log).stream().limit(limit).
            forEach(
                le -> instance.printEntry(le.timestamp, hexPath.getEntry(le.hex), System.out) );
          
        }
        
      }
    
    } catch (IllegalArgumentException x) {
      String message = x.getMessage();
      message = x.getClass().getSimpleName() + (message == null ? "" : " "  + message);
      exitInputError(message);
    
    } catch (Exception e) {
      String message = e.getMessage();
      message = e.getClass().getSimpleName() + (message == null ? "" : " "  + message);
      printError(message);
      e.printStackTrace(System.err);
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
    out.println();
    table.printRow(DIR + "=*", "store root directory (must already exist)", REQ);
    out.println();
    table.printRow(HEX + "=*", "outputs a single entry using this unambiguous prefix", REQ_CH);
    table.printRow(null,       "of its hexadecimal ID", null);
    out.println();
    table.printRow(START + "=*", "lists entries in ascending order of hex IDs starting from", REQ_CH);
    table.printRow(null,         "given hex (prefix OK)", null);
    out.println();
    table.printRow(LOG + "=*", "lists entries in log timestamp order starting from the", REQ_CH);
    table.printRow(null,         "given date (prefix OK)", null);
    out.println();
    table.printRow(PRINT + "=*", "sets what's to be output. Valid values range in [1," + MAX_OPTS + "]", OPT);
    table.printRow(null,         "(defaults to " + DEFAULT_OPTS + " for '" + HEX + "'/'" + START + "'; " +
                                  DEFAULT_OPTS_LOG + " for '" + LOG + "'). Values are", null);
    table.printRow(null,         "interpreted as bit field combinations of the following:", null);
    out.println();
    {
      TablePrint subTable = new TablePrint(out, 5, 65);
      subTable.setIndentation(12);
      subTable.printRow(HEX_OPT, "hex ID");
      subTable.printRow(PATH_OPT, "file path");
      subTable.printRow(CONTENTS_OPT, "file contents (assuming it's text)");
      subTable.printRow(LOG_DATE_OPT, "write-log timestamp (noop if not used with '" + LOG + "')");
    }
    out.println();
    table.printRow(LIMIT + "=*", "max number of entries to output (default " + DEFAULT_LIMIT + ")", OPT);
    
    out.println();
  }
  
  private static void printLegend(PrintStream out) {
    out.println();
    TablePrint legend = new TablePrint(out, 5, 65);
    legend.setIndentation(11);
    legend.println("______");
    legend.println("Legend:");
    legend.println("------");
    out.println();
    legend.printRow("*", "denotes an arbitrary input value (not a wildcard)");
    legend.printRow(REQ, "denotes a required name={value} arg");
    legend.printRow(REQ_CH, "denotes a required one-of-many name={value} arg");
    legend.printRow(null, "(grouped together in adjacent rows)");
    out.println();
  }

  
  private final static String EXT = "ext";
  private final static String DIR = "dir";
  private final static String HEX = "hex";
  private final static String START = "start";
  private final static String LOG = "log";
  private final static String LIMIT = "limit";
  private final static String PRINT = "print";
  
  private final static String REQ = "R";
  private final static String REQ_CH = "R?";
  private final static String OPT = "";
  
  
  private final static int HEX_OPT = 1;
  private final static int PATH_OPT = 2;
  private final static int CONTENTS_OPT = 4;
  private final static int LOG_DATE_OPT = 8;
  private final static int MAX_OPTS = 2 * LOG_DATE_OPT - 1;
  private final static int DEFAULT_OPTS = HEX_OPT + PATH_OPT;
  private final static int DEFAULT_OPTS_LOG = DEFAULT_OPTS + LOG_DATE_OPT;
  private final static int DEFAULT_LIMIT = 10;

}
