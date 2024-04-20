/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import io.crums.stowkwik.io.Channels;

/**
 * Move-on-write test.
 */
public class FileManagerTest extends NoBiggiesObjectManagerTest {
  
  private final static String STAGING_DIR = "staging";
  
  
  private final boolean moveOnWrite;

  /**
   * Move-on-write test.
   */
  public FileManagerTest() {
    this(true);
  }
  
  protected FileManagerTest(boolean moveOnWrite) {
    super(".mif");
    this.moveOnWrite = moveOnWrite;
  }

  
  @SuppressWarnings("resource")
  @Override
  protected ObjectManager<Mock> makeStore(File dir) {
    
    File staging = new File(dir, STAGING_DIR);
    if (!staging.mkdirs() && !staging.isDirectory())
      throw new IllegalStateException(staging.toString());
    
    FileManager manager = new FileManager(dir, ext, moveOnWrite);
    MockCodec codec = new MockCodec();
    AtomicInteger count = new AtomicInteger();
    
    return ObjectManager.map(
        manager,
        
        file -> {
          ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
          Channels.readFully(file, buffer);
          buffer.flip();
          buffer.limit(buffer.limit() - 1); // remove padding
          return codec.read(buffer);
        },
        
        mock -> {
          ByteBuffer buffer = ByteBuffer.allocate(codec.maxBytes() + 1);
          codec.write(mock, buffer);
          buffer.put((byte) 77);  // (pad to indicate we're not cheating)
          buffer.flip();
          File file = new File(staging, "S" + count.incrementAndGet());
          try (FileChannel channel = new FileOutputStream(file).getChannel()) {
            Channels.writeRemaining(channel, buffer);
          } catch (IOException iox) {
            throw new UncheckedIOException(iox);
          }
          return file;
        });
  }
  
  
  

}
