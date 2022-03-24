package com.github.gotson.jnr.jxl.lib.enums;

import jnr.ffi.util.EnumMapper;

/** The result of JxlSignatureCheck.
 */
public enum JxlSignature implements EnumMapper.IntegerEnum {
  /** Not enough bytes were passed to determine if a valid signature was found.
   */
  JXL_SIG_NOT_ENOUGH_BYTES(0),
  /** No valid JPEGXL header was found. */
  JXL_SIG_INVALID(1),
  /** A valid JPEG XL codestream signature was found, that is a JPEG XL image
   * without container.
   */
  JXL_SIG_CODESTREAM(2),
  /** A valid container signature was found, that is a JPEG XL image embedded
   * in a box format container.
   */
  JXL_SIG_CONTAINER(3);

  private final int val;

  JxlSignature(int val) {
    this.val = val;
  }

  @Override
  public int intValue() {
    return val;
  }
}
