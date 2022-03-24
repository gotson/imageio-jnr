package com.github.gotson.jnr.webp.lib.enums;

import jnr.ffi.util.EnumMapper;

public enum VP8StatusCode implements EnumMapper.IntegerEnum {
  VP8_STATUS_OK(0),
  VP8_STATUS_OUT_OF_MEMORY(1),
  VP8_STATUS_INVALID_PARAM(2),
  VP8_STATUS_BITSTREAM_ERROR(3),
  VP8_STATUS_UNSUPPORTED_FEATURE(4),
  VP8_STATUS_SUSPENDED(5),
  VP8_STATUS_USER_ABORT(6),
  VP8_STATUS_NOT_ENOUGH_DATA(7);

  private final int val;

  VP8StatusCode(int val) {
    this.val = val;
  }

  @Override
  public int intValue() {
    return val;
  }
}
