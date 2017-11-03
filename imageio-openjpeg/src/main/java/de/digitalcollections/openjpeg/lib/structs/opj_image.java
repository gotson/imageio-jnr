package de.digitalcollections.openjpeg.lib.structs;

import de.digitalcollections.openjpeg.OpenJpeg;
import de.digitalcollections.openjpeg.lib.enums.COLOR_SPACE;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_image extends Struct implements AutoCloseable {
  /** XOsiz: horizontal offset from the origin of the reference grid to the left side of the image area */
  public u_int32_t x0 = new u_int32_t();
  /** YOsiz: vertical offset from the origin of the reference grid to the top side of the image area */
  public Unsigned32 y0 = new Unsigned32();
  /** Xsiz: width of the reference grid */
  public Unsigned32 x1 = new Unsigned32();
  /** Ysiz: height of the reference grid */
  public Unsigned32 y1 = new Unsigned32();
  /** number of components in the image */
  public Unsigned32 numcomps = new Unsigned32();
  /** color space: sRGB, Greyscale or YUV */
  Enum32<COLOR_SPACE> color_space = new Enum32<COLOR_SPACE>(COLOR_SPACE.class);
  /** image components */
  public StructRef<opj_image_comp> comps = new StructRef<>(opj_image_comp.class);
  /** 'restricted' ICC profile */
  Pointer icc_profile_buf = new Pointer();
  /** size of ICC profile */
  Unsigned32 icc_profile_len = new Unsigned32();

  public opj_image(Runtime runtime) {
    super(runtime);
  }

  @Override
  public void close() {
    if (this.x0.getMemory().address() != 0) {
      OpenJpeg.LIB.opj_image_destroy(this.x0.getMemory());
    }
  }
}
