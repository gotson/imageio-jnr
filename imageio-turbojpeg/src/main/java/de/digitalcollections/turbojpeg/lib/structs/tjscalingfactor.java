package de.digitalcollections.turbojpeg.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class tjscalingfactor extends Struct {
  public Signed32 num = new Signed32();
  public Signed32 denom = new Signed32();

  public tjscalingfactor(Runtime runtime) {
    super(runtime);
  }
}
