package com.github.gotson.jnr.webp.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** Decoding options */
public class WebPDecoderOptions extends Struct {
  /** if true, skip the in-loop filtering */
  public Struct.Signed32 bypass_filtering = new Signed32();

  /** if true, use faster pointwise upsampler */
  public Struct.Signed32 no_fancy_upsampling = new Signed32();

  /** if true, cropping is applied _first_ */
  public Struct.Signed32 use_cropping = new Signed32();

  /** top-left position for cropping. */
  public Struct.Signed32 crop_left = new Signed32();

  /** top-left position for cropping. */
  public Struct.Signed32 crop_top = new Signed32();

  /** dimension of the cropping area Will be snapped to even values. */
  public Struct.Signed32 crop_width = new Signed32();

  /** dimension of the cropping area Will be snapped to even values. */
  public Struct.Signed32 crop_height = new Signed32();

  /** if true, scaling is applied _afterward_ */
  public Struct.Signed32 use_scaling = new Signed32();

  /** final resolution */
  public Struct.Signed32 scaled_width = new Signed32();

  /** final resolution */
  public Struct.Signed32 scaled_height = new Signed32();

  /** if true, use multi-threaded decoding */
  public Struct.Signed32 use_threads = new Signed32();

  /** dithering strength (0=Off, 100=full) */
  public Struct.Signed32 dithering_strength = new Signed32();

  /** if true, flip output vertically */
  public Struct.Signed32 flip = new Signed32();

  /** alpha dithering strength in [0..100] */
  public Struct.Signed32 alpha_dithering_strength = new Signed32();

  /** padding for later use */
  public Struct.u_int32_t[] pad = new u_int32_t[5];

  public WebPDecoderOptions(Runtime runtime) {
    super(runtime);
  }
}
