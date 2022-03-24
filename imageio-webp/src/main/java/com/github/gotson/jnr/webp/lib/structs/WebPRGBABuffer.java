package com.github.gotson.jnr.webp.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class WebPRGBABuffer extends Struct {
  /** pointer to RGBA samples */
  public Struct.Pointer rgba = new Pointer();

  /** stride in bytes from one scanline to the next. */
  public Struct.Signed32 stride = new Signed32();

  /** total size of the *rgba buffer. */
  public Struct.size_t size = new size_t();

  protected WebPRGBABuffer(Runtime runtime) {
    super(runtime);
  }
}
