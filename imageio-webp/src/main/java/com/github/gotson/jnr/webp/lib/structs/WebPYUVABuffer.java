package com.github.gotson.jnr.webp.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class WebPYUVABuffer extends Struct {
  /** pointer to luma */
  public Struct.Pointer y = new Pointer();

  /** pointer to chroma U */
  public Struct.Pointer u = new Pointer();

  /** pointer to chroma V */
  public Struct.Pointer v = new Pointer();

  /** pointer to alpha samples */
  public Struct.Pointer a = new Pointer();

  /** luma stride */
  public Struct.Signed32 y_stride = new Signed32();

  /** chroma strides */
  public Struct.Signed32 u_stride = new Signed32();

  /** chroma strides */
  public Struct.Signed32 v_stride = new Signed32();

  /** alpha stride */
  public Struct.Signed32 a_stride = new Signed32();

  /** luma plane size */
  public Struct.size_t y_size = new size_t();

  /** chroma planes size */
  public Struct.size_t u_size = new size_t();

  /** chroma planes size */
  public Struct.size_t v_size = new size_t();

  /** alpha-plane size */
  public Struct.size_t a_size = new size_t();

  protected WebPYUVABuffer(Runtime runtime) {
    super(runtime);
  }
}
