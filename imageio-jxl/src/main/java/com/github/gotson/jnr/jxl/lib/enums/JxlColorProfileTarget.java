package com.github.gotson.jnr.jxl.lib.enums;

import jnr.ffi.util.EnumMapper;

/**
 * Defines which color profile to get: the profile from the codestream
 * metadata header, which represents the color profile of the original image,
 * or the color profile from the pixel data received by the decoder. Both are
 * the same if the basic has uses_original_profile set.
 */
public enum JxlColorProfileTarget implements EnumMapper.IntegerEnum {
    /**
     * Get the color profile of the original image from the metadata.
     */
    JXL_COLOR_PROFILE_TARGET_ORIGINAL(0),

    /**
     * Get the color profile of the pixel data the decoder outputs.
     */
    JXL_COLOR_PROFILE_TARGET_DATA(1);

    private final int val;

    JxlColorProfileTarget(int val) {
        this.val = val;
    }

    @Override
    public int intValue() {
        return val;
    }
}
