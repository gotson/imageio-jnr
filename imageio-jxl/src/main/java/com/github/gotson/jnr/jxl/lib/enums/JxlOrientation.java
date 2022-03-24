package com.github.gotson.jnr.jxl.lib.enums;

import jnr.ffi.util.EnumMapper;

/**
 * Image orientation metadata.
 * Values 1..8 match the EXIF definitions.
 * The name indicates the operation to perform to transform from the encoded
 * image to the display image.
 */
public enum JxlOrientation implements EnumMapper.IntegerEnum {
    JXL_ORIENT_IDENTITY(1),
    JXL_ORIENT_FLIP_HORIZONTAL(2),
    JXL_ORIENT_ROTATE_180(3),
    JXL_ORIENT_FLIP_VERTICAL(4),
    JXL_ORIENT_TRANSPOSE(5),
    JXL_ORIENT_ROTATE_90_CW(6),
    JXL_ORIENT_ANTI_TRANSPOSE(7),
    JXL_ORIENT_ROTATE_90_CCW(8);

    private final int val;

    JxlOrientation(int val) {
        this.val = val;
    }

    @Override
    public int intValue() {
        return val;
    }
}
