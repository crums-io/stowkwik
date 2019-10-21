/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import static com.gnahraf.util.IntegralStrings.toHex;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.gnahraf.io.CorruptionException;
import com.gnahraf.io.HexPathTree;
import com.gnahraf.xcept.NotFoundException;

/**
 * A file-per-object storage manager. The ID of each object is
 * determined by a cryptographic hash (MD5 suffices) of its contents.
 *
 * @param T the type of object managed.
 */
public abstract class HashedObjectManager<T> extends ObjectManager<T> {
  
  public final static String DEFAULT_HASH_ALGO = "MD5";
  
  private final HexPathTree hashedPath;
  private final Encoder<T> encoder;
  private final String hashAlgo;
  
  protected HashedObjectManager(File dir, String ext, Encoder<T> encoder) {
    this(dir, ext, encoder, DEFAULT_HASH_ALGO);
  }

  /**
   * @param dir         the root directory
   * @param ext         filename extension used by entries
   * @param encoder     binary encoder for computing the hash of the object's contents
   */
  protected HashedObjectManager(File dir, String ext, Encoder<T> encoder, String hashAlgo) {
    this.hashedPath = new HexPathTree(dir, ext);
    this.encoder = encoder;
    this.hashAlgo = hashAlgo;
    
    if (hashedPath == null)
      throw new IllegalArgumentException("null hashedPath");
    if (encoder == null)
      throw new IllegalArgumentException("null encoder");
    if (hashAlgo == null)
      throw new IllegalArgumentException("null hashAlgo");
    // make sure the digest algo is supported
    newDigest();
  }
  
  
  @Override
  public String write(T object) throws UncheckedIOException {
    ByteBuffer buffer = allocateBuffer(encoder.maxBytes());
    encoder.write(object, buffer);
    buffer.flip();
    
    String hash = signature(buffer);
    File file = hashedPath.suggest(hash, true);
    
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
    return hashedPath.find(id) != null;
  }


  @Override
  public T read(String hash) throws UncheckedIOException {
    File file = hashedPath.find(hash);
    if (file == null)
      throw new NotFoundException(hash);
    
    return readObjectFile(file);
  }
  
  
  
  protected final File getFilepath(String hash) {
    return hashedPath.find(hash);
  }
  
  
  
  @Override
  public Stream<String> streamIds() {
    return hashedPath.stream().map(e -> e.hex);
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
    MessageDigest digest = threadLocalDigest();
    buffer.mark();
    digest.update(buffer);
    buffer.reset();
    return toHex(digest.digest());
  }
  
  /**
   * Testing shows about a 10% improvement in speed when reusing digests (SSD
   */
  protected MessageDigest threadLocalDigest() {
    Map<String, MessageDigest> map = digestMap.get();
    MessageDigest digest = map.get(hashAlgo);
    if (digest == null) {
      digest = newDigest();
      map.put(hashAlgo, digest);
    } else
      digest.reset();
    return digest;
  }
  
  protected final ThreadLocal<Map<String, MessageDigest>> digestMap = new ThreadLocal<>() {
    @Override
    protected Map<String, MessageDigest> initialValue() {
      return new HashMap<>(2);
    }
  };

  
  
  protected MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance(hashAlgo);
    } catch (NoSuchAlgorithmException nsax) {
      IllegalArgumentException iax = new IllegalArgumentException("hash algo: " + hashAlgo);
      iax.initCause(nsax);
      throw iax;
    }
  }
}
