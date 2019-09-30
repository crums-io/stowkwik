/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import com.gnahraf.io.CorruptionException;
import com.gnahraf.io.FilepathGenerator;
import com.gnahraf.xcept.NotFoundException;

/**
 * A file-per-object storage manager. The ID of each object is
 * determined by a cryptographic hash (MD5 suffices) of its contents.
 *
 * @param T the type of object managed.
 */
public abstract class HashedObjectManager<T> extends ObjectManager<T> {
  
  private final FilepathGenerator hashedPath;
  private final Encoder<T> encoder;

  /**
   * 
   * @param hashedPath  determines the file naming convention
   * @param encoder     binary encoder for computing the hash of the object's contents
   */
  protected HashedObjectManager(FilepathGenerator hashedPath, Encoder<T> encoder) {
    this.hashedPath = hashedPath;
    this.encoder = encoder;
    
    if (hashedPath == null)
      throw new IllegalArgumentException("null hashedPath");
    if (encoder == null)
      throw new IllegalArgumentException("null encoder");
  }
  
  
  @Override
  public String write(T object) throws UncheckedIOException {
    ByteBuffer buffer = allocateBuffer(encoder.maxBytes());
    encoder.write(object, buffer);
    buffer.flip();
    
    String hash = signature(buffer);
    File file = hashedPath.toFilepath(hash);
    
    if (file.exists())
      validateFile(file, object, buffer);
    else
      writeObjectFile(file, object, buffer);
    
    return hash;
  }
  
  
  
  @Override
  public String getId(T object) {
    ByteBuffer buffer = allocateBuffer(encoder.maxBytes());
    encoder.write(object, buffer);
    buffer.flip();
    
    return signature(buffer);
  }


  @Override
  public boolean containsId(String id) {
    return hashedPath.toFilepath(id).exists();
  }


  @Override
  public T read(String hash) throws UncheckedIOException {
    File file = hashedPath.toFilepath(hash);
    if (!file.exists())
      throw new NotFoundException(hash);
    
    return readObjectFile(file);
  }
  
  
  
  protected final File getFilepath(String hash) {
    return hashedPath.toFilepath(hash);
  }
  
  
  
  @Override
  public Stream<String> streamIds() {
    return hashedPath.streamIdentifiers();
  }
  
  
  
  
  /**
   * @param file      an existing file
   */
  protected abstract T readObjectFile(File file) throws UncheckedIOException;



  /**
   * @param file      (does not yet exist)
   * @param object    the thing to be written
   * @param buffer    the binary contents used to generate the object's signature
   *                  (may be suitable for writing to file?)
   * @throws UncheckedIOException
   */
  protected abstract void writeObjectFile(File file, T object, ByteBuffer buffer) throws UncheckedIOException;



  protected void validateFile(File file, T object, ByteBuffer buffer) throws CorruptionException {
  }



  protected ByteBuffer allocateBuffer(int bytes) {
    return ByteBuffer.allocate(bytes);
  }
  
  /**
   * Computes and returns the signature of the given <tt>buffer</tt>.
   * Excepting its mark, the state of the <tt>buffer</tt> is not modified.
   */
  protected String signature(ByteBuffer buffer) {
    MessageDigest digest = newDigest();
    buffer.mark();
    digest.update(buffer);
    buffer.reset();
    return DatatypeConverter.printHexBinary(digest.digest());
  }

  
  
  protected MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException nsax) {
      throw new RuntimeException(nsax);
    }
  }
}
