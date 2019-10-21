/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.ByteBuffer;

import javax.xml.bind.JAXB;

import com.gnahraf.io.CorruptionException;
import com.gnahraf.xcept.NotFoundException;

/**
 * Stores object state in XML format. Note the hashing scheme is independent of
 * of formatting and such. (So it's OK, for example, to edit one of these XML files,
 * as in adding comments, as long as you don't break the read-codepath.)
 * 
 * @param T a mutable struct type with public members suitable for <tt>JAXB</tt>. Note you
 *        have to override {@linkplain Object#equals(Object)} (and by extension {@linkplain Object#hashCode()})
 *        for this to properly work. To
 *        bridge this mutable type to an immutable one, consider
 *        {@linkplain ObjectManager#map(ObjectManager, java.util.function.Function, java.util.function.Function) ObjectManager.map}
 *
 */
public class XmlObjectManager<T> extends HashedObjectManager<T> {
  
  private final Class<T> clazz;

  public XmlObjectManager(File dir, String ext, Encoder<T> encoder, Class<T> clazz) {
    this(dir, ext, encoder, clazz, DEFAULT_HASH_ALGO);
  }
  
  /**
   * @param encoder  used to compute a hash of the object. Note this is a lot easier than it sounds:
   *                 you just write the state (members) of object <tt>T</tt> to a buffer in whatever order
   *                 (just don't change it). It's not a full codec: you don't have to read it back.
   */
  public XmlObjectManager(File dir, String ext, Encoder<T> encoder, Class<T> clazz, String hashAlgo) {
    super(dir, ext, encoder, hashAlgo);
    this.clazz = clazz;
    
    if (clazz == null)
      throw new IllegalArgumentException("null clazz");
  }

  
  
  
  
  
  @Override
  protected T readObjectFile(File file) {
    return JAXB.unmarshal(file, clazz);
  }

  
  @Override
  protected void writeObjectFile(File file, T object, ByteBuffer buffer) {
    JAXB.marshal(object, file);
  }


  /**
   * This validation depends on {@linkplain Object#equals(Object)} for type <tt>T</tt>
   * being properly implemented.
   */
  @Override
  protected void validateFile(File file, T object, ByteBuffer buffer) throws CorruptionException {
    
    boolean fail = !object.equals(readObjectFile(file));
    if (fail)
      throw new CorruptionException(file.toString());
  }
  
  
  
  @Override
  public boolean hasReader() {
    return true;
  }
  
  
  /**
   * Returns the XML representation of the thing.
   */
  @Override
  public Reader getReader(String hash) throws NotFoundException {
    try {
      return new FileReader(getFilepath(hash));
    } catch (FileNotFoundException fnfx) {
      throw new NotFoundException(hash, fnfx);
    }
  }

}
