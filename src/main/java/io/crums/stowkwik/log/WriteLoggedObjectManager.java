/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik.log;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.Channel;
import java.util.Objects;

import io.crums.stowkwik.ObjectManager;
import io.crums.stowkwik.WrappedObjectManager;

/**
 * 
 */
public class WriteLoggedObjectManager<T> extends WrappedObjectManager<T> implements Channel {
  
  protected final WriteLog log;
  private boolean closed;
  private final Object closeLock = new Object();

  /**
   * @param base
   */
  public WriteLoggedObjectManager(ObjectManager<T> base, WriteLog log) {
    super(base);
    this.log = Objects.requireNonNull(log, "log");
  }

  
  @Override
  public String write(T object) throws UncheckedIOException {
    String id = base.write(object);
    log.objectWritten(id);
    return id;
  }


  @Override
  public void close() throws UncheckedIOException {
    synchronized (closeLock) {
      closed = true;
      try {
        log.close();
      } catch (IOException iox) {
        throw new UncheckedIOException(iox);
      }
    }
  }


  @Override
  public boolean isOpen() {
    synchronized (closeLock) {
      return !closed;
    }
  }
  
  
  

}
