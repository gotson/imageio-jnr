package com.github.gotson.jnr.webp.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** Main object storing the configuration for advanced decoding. */
public class WebPDecoderConfig extends Struct {
  /** Immutable bitstream features (optional) */
  public WebPBitstreamFeatures input;

  /** Output buffer (can point to external mem) */
  public WebPDecBuffer output;

  /** Decoding options */
  public WebPDecoderOptions options;

  public WebPDecoderConfig(Runtime runtime) {
    super(runtime);
    input = inner(new WebPBitstreamFeatures(runtime));
    output = inner(new WebPDecBuffer(runtime));
    options = inner(new WebPDecoderOptions(runtime));
  }
}
