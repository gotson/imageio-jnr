package com.github.gotson.jnr.webp.lib.structs;

import com.github.gotson.jnr.webp.lib.enums.WEBP_CSP_MODE;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** Output buffer */
public class WebPDecBuffer extends Struct {
  public WEBP_CSP_MODE colorspace;
  public Signed32 width = new Signed32();
  public Signed32 height = new Signed32();

  /**
   * If non-zero, 'internal_memory' pointer is not used. If value is '2' or more, the external
   * memory is considered 'slow' and multiple read/write will be avoided.
   */
  public Signed32 is_external_memory = new Signed32();

  /** Nameless union of buffer parameters. */
  public WebpBufferUnion u;

  /** padding for later use */
  public Struct.u_int32_t[] pad = new u_int32_t[4];

  /**
   * Internally allocated memory (only when is_external_memory is 0). Should not be used externally,
   * but accessed via the buffer union.
   */
  public Struct.Pointer private_memory = new Pointer();

  public WebPDecBuffer(Runtime runtime) {
    super(runtime);
    u = inner(new WebpBufferUnion(runtime, true));
  }
}
