/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.io;


import static com.gnahraf.util.IntegralStrings.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;

import com.gnahraf.util.EasyList;

/**
 * A more capable <tt>HexPath</tt>. This builds on the base class (which was factored out
 * in order to lessen programmer cognitive load).
 */
public class HexPathTree extends HexPath {

  
  private final static String[] FULL_DIR_SET = new String[256];
  static {
    for (int i = 0; i < 16; ++i)
      FULL_DIR_SET[i] = "0" + Integer.toHexString(i);
    for (int i = 16; i < 256; ++i)
      FULL_DIR_SET[i] = Integer.toHexString(i);
  }
  
  /**
   * Stateless hex directory filter.
   */
  private final static FileFilter HEX_DIRECTORY_FILTER = new FileFilter() {
    
    @Override
    public boolean accept(File pathname) {
      String name = pathname.getName();
      return name.length() == 2 && isLowercaseHex(name) && pathname.isDirectory();
    }
  };
  
  private final FilenameFilter entryFilter;

  public HexPathTree(File dir, String ext) {
    this(dir, ext, 256);
  }

  
  public HexPathTree(File dir, String ext, int maxFilesPerDir) {
    super(dir, ext, maxFilesPerDir);
    this.entryFilter = convention.getFilenameFilter();
  }
  
  
  
  
  
  public static class Entry implements Comparable<Entry> {
    
    public final String hex;
    public final File file;
    
    private Entry(String hex, File file) {
      this.hex = hex;
      this.file = file;
    }

    @Override
    public int compareTo(Entry o) {
      return hex.compareTo(o.hex);
    }
    
    @Override
    public boolean equals(Object o) {
      return this == o || o instanceof Entry && ((Entry) o).hex.equals(hex);
    }
    
    @Override
    public int hashCode() {
      return hex.hashCode();
    }
  }
  
  
  
  class Cursor implements Spliterator<Entry> {
    
    private final EasyList<HexDirectoryPosition> pathPositions;
    
    private HexDirectoryPosition[] entryRankedPositions;
    
    public Cursor() {
      pathPositions = new EasyList<>();
      pathPositions.add(new HexDirectoryPosition(new HexDirectory()));
      init();
    }
    
    
    
    private Cursor(EasyList<HexDirectoryPosition> pathPositions) {
      this.pathPositions = pathPositions;
      init();
    }
    
    
    private void init() {
      pushDown();
      popConsumed();
      entryRankedPositions = rankPositions();
    }
    
    
    public String getHeadValue() {
      return entryRankedPositions[0].firstEntry();
    }
    
    
    public File getHeadFile() {
      HexDirectoryPosition p = entryRankedPositions[0];
      String hexTail = p.firstEntry().substring(p.hexDirectory().getInheritedValue().length());
      return new File(p.hexDirectory().dir, convention.toFilename(hexTail));
    }
    
    
    public Entry getHeadEntry() {
      return new Entry(getHeadValue(), getHeadFile());
    }
    
    
    /**
     * Consumes the head entry and advances the state to next entry. 
     * @return {@linkplain #hasRemaining()}
     */
    public boolean consumeNext() {
      entryRankedPositions[0].consumeNextEntry();
      popConsumed();
      entryRankedPositions = rankPositions();
      return hasRemaining();
    }
    
    
    public boolean hasRemaining() {
      return entryRankedPositions[0].hasRemaining();
    }
    
    



    @Override
    public boolean tryAdvance(Consumer<? super Entry> action) {
      if (hasRemaining()) {
        action.accept(getHeadEntry());
        consumeNext();
        return true;
      } else
        return false;
    }


    @Override
    public Spliterator<Entry> trySplit() {
      HexDirectoryPosition splitDirectory = pathPositions.first();
      int splitDepth;
      {
        int depth = 0;
        int maxIndex = pathPositions.size() - 1;
        while (!splitDirectory.isSplittable()) {
          if (++depth > maxIndex)
            return null;
          splitDirectory = pathPositions.get(depth);
        }
        splitDepth = depth;
      }
      // Note: the splitDepth > 0 when we keep splitting a split instance
      //(typically 8 times to advance to the next depth)
      
      EasyList<HexDirectoryPosition> pathPositionsCopy = new EasyList<>(pathPositions.size());
      for (int depth = 0; depth < splitDepth; ++depth)
        pathPositionsCopy.add(new HexDirectoryPosition(pathPositions.get(depth)));
      
      pathPositionsCopy.add(splitDirectory.split());
      return new Cursor(pathPositionsCopy);
    }


    @Override
    public long estimateSize() {
      long estimate = 0;
      for (int depth = pathPositions.size(); depth-- > 0; ) {
        HexDirectoryPosition dirPosition = pathPositions.get(depth);
        int entries = dirPosition.entries().size();
        int subdirs = dirPosition.subdirs().size();
        estimate *= (1 + subdirs);
        estimate += entries;
      }
      return estimate;
    }


    @Override
    public int characteristics() {
      return ORDERED | SORTED | IMMUTABLE | NONNULL;
    }
    
    
    
    
    
    
    
    
    
    
    private HexDirectoryPosition[] rankPositions() {
      HexDirectoryPosition[] positions = new HexDirectoryPosition[pathPositions.size()];
      switch (positions.length) {
      case 0: throw new AssertionError();
      case 1:
        positions[0] = pathPositions.first();
        break;
      default:
        positions = pathPositions.toArray(positions);
        Arrays.sort(positions, DIRPOS_ENTRY_RANK);
      }
      return positions;
    }
    
    private void pushDown() {
      HexDirectoryPosition position = pathPositions.last();
      while (position.hasSubdirs()) {
        position = new HexDirectoryPosition(position.firstSubdir());
        pathPositions.add(position);
      }
    }
    
    
    private void popConsumed() {
      
      while (pathPositions.size() > 1 && pathPositions.last().isConsumed()) {
        pathPositions.removeLast();
        pathPositions.last().consumeNextSubdir();
        if (pathPositions.last().hasSubdirs())
          pushDown();
      }
    }
    
  }

  
  /**
   * Ranks <tt>HexDirectoryPosition</tt>s by their first remaining entry (file) and then by
   * their depth (with deeper ones coming first).
   */
  final static Comparator<HexDirectoryPosition> DIRPOS_ENTRY_RANK = new Comparator<>() {

    @Override
    public int compare(HexDirectoryPosition a, HexDirectoryPosition b) {
      if (a.hasEntries() && b.hasEntries()) {
        int comp = a.firstEntry().compareTo(b.firstEntry());
        if (comp == 0 && a != b)
          comp = compDepth(a, b);
        return comp;
      }
      else if (a.hasEntries())
        return -1;
      else if (b.hasEntries())
        return 1;
      else
        return compDepth(a, b);
    }
    
    
    // The deeper path comes first; i.e. the deeper path is less than the shallower one
    private int compDepth(HexDirectoryPosition a, HexDirectoryPosition b) {
      return b.hexDirectory().getDepth() - a.hexDirectory().getDepth();
    }
    
  };
  
  
  static class HexDirectoryPosition {
    
    private final HexDirectory hdir;
    
    private List<String> entries;
    
    private List<HexDirectory> subdirs;
    
    
    
    
    HexDirectoryPosition(HexDirectory hdir) {
      this.hdir = hdir;
      entries = hdir.listEntries();
      subdirs = hdir.listBranches();
    }
    
    
    private HexDirectoryPosition(HexDirectoryPosition copy) {
      this.hdir = copy.hdir;
      this.entries = Collections.emptyList();
      this.subdirs = copy.subdirs;
    }
    
    
    public HexDirectoryPosition split() {
      if (!isSplittable())
        throw new IllegalStateException("not splittable");
      
      HexDirectoryPosition split = new HexDirectoryPosition(this);
      int dirSplitIndex = 1 + subdirs.size() / 2;
      
      split.subdirs = subdirs.subList(dirSplitIndex, subdirs.size());
      subdirs = subdirs.subList(0, dirSplitIndex);
      
      return split;
    }
    
    public boolean isSplittable() {
      return subdirs.size() > 1;
    }
    
    public HexDirectory hexDirectory() {
      return hdir;
    }
    
    public boolean hasEntries() {
      return !entries.isEmpty();
    }
    
    public int countEntries() {
      return entries.size();
    }
    
    public String firstEntry() {
      return entries.get(0);
    }
    
    public boolean hasSubdirs() {
      return !subdirs.isEmpty();
    }
    
    public int countSubdirs() {
      return subdirs.size();
    }
    
    public HexDirectory firstSubdir() {
      return subdirs.get(0);
    }
    
    public boolean consumeNextEntry() {
      
      switch (entries.size()) {
      case 0:
        return false;
      case 1:
        entries = Collections.emptyList();
        return true;
      default: 
        entries = entries.subList(1, entries.size());
        return true;
      }
    }
    
    public boolean consumeNextSubdir() {
      
      switch (subdirs.size()) {
      case 0:
        return false;
      case 1:
        subdirs = Collections.emptyList();
        return true;
      default: 
        subdirs = subdirs.subList(1, subdirs.size());
        return true;
      }
    }
    
    
    public boolean hasRemaining() {
      return !isConsumed();
    }
    
    public boolean isConsumed() {
      return entries.isEmpty() && subdirs.isEmpty();
    }
    
    
    public List<String> entries() {
      return entries;
    }
    
    public List<HexDirectory> subdirs() {
      return subdirs;
    }
    
    
    
  }
  
  
  
  /**
   * An immutable representation of a hex directory.
   * <p/>
   * OK it's not properly immutable since arrays aren't and inner classes are promiscuous.
   * But we're careful here not to muck around with state once an instance is created.
   */
  class HexDirectory {
    
    final File dir;
    private final HexDirectory parent;
    private final String[] hexEntries;
    private final String[] hexDirs;
    private final String hexPrefix;
    
    
    public HexDirectory() {
      this(HexPathTree.this.root, null);
    }
    
    private HexDirectory(File dir, HexDirectory parent) {
      this.dir = dir;
      this.parent = parent;
      
      // list the hex files in this directory
      String[] entries = dir.list(entryFilter);
      
      // we assume the best, that these are ordinary files
      // (i.e. an adversary didn't create directories by these names)
      
      // convert these to hexadecimal values and sort them
      for (int i = entries.length; i-- > 0; )
        entries[i] = convention.toIdentifierUnchecked(entries[i]);
      
      Arrays.sort(entries);
      hexEntries = entries;
      
      File[] subdirs = dir.listFiles(HEX_DIRECTORY_FILTER);
      if (subdirs.length == 256)
        hexDirs = FULL_DIR_SET;
      else {
        hexDirs = new String[subdirs.length];
        for (int i = hexDirs.length; i-- > 0; )
          hexDirs[i] = subdirs[i].getName();
        
        Arrays.sort(hexDirs);
      }
      
      hexPrefix = parent == null ? "" : parent.hexPrefix + dir.getName();
    }

    
    public final boolean isRoot() {
      return parent == null;
    }
    
    public HexDirectory getParent() {
      return parent;
    }
    
    
    public String getValue() {
      return isRoot() ? "" : dir.getName();
    }
    
    
    public String getInheritedValue() {
      return hexPrefix;
    }
    
    
    public int getDepth() {
      return hexPrefix.length() / 2;
    }
    
    
    
    /**
     * Returns a sorted, read-only list of hex entries in this directory. The
     * entries are completed with the hex value encoded in this directory's
     * pathname.
     */
    public List<String> listEntries() {
      if (hexEntries.length == 0)
        return Collections.emptyList();
      
      return
          new AbstractList<>() {

            @Override
            public String get(int index) {
              return hexPrefix + hexEntries[index];
            }
    
            @Override
            public int size() {
              return hexEntries.length;
            }
          };
    }
    
    
    
    /**
     * Returns a sorted, read-only list of subdirectories (branches) in this
     * directory.
     * @return
     */
    public List<HexDirectory> listBranches() {
      if (hexDirs.length == 0)
        return Collections.emptyList();
      
      return
          new AbstractList<>() {

            @Override
            public HexDirectory get(int index) {
              return new HexDirectory(new File(dir, hexDirs[index]), HexDirectory.this);
            }

            @Override
            public int size() {
              return hexDirs.length;
            }
        
          };
    }
    
    
    
    
    
    
    
    
    
    
    
//    public int indexInParent() {
//      checkNotRoot();
//      int index = Arrays.binarySearch(parent.hexDirs, getValue());
//      assert index >= 0;
//      return index;
//    }
    
    
//    private void checkNotRoot() {
//      if (isRoot())
//        throw new IllegalStateException("cannot invoke on root instance");
//    }
    
  }
  
  
  
  

}
