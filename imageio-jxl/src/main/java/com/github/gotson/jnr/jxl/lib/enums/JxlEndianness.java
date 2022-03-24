package com.github.gotson.jnr.jxl.lib.enums;

import jnr.ffi.util.EnumMapper;

/**
 * Ordering of multi-byte data.
 */
public enum JxlEndianness implements EnumMapper.IntegerEnum {
    /**
     * Use the endianness of the system, either little endian or big endian,
     * without forcing either specific endianness. Do not use if pixel data
     * should be exported to a well defined format.
     */
    JXL_NATIVE_ENDIAN(0),

    /**
     * Force little endian
     */
    JXL_LITTLE_ENDIAN(1),

    /**
     * Force big endian
     */
    JXL_BIG_ENDIAN(2);

    private final int val;

    JxlEndianness(int val) {
        this.val = val;
    }

    @Override
    public int intValue() {
        return val;
    }
}
