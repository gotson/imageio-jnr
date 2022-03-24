package com.github.gotson.jnr.jxl.lib.enums;

import jnr.ffi.util.EnumMapper;

/**
 * Data type for the sample values per channel per pixel.
 */
public enum JxlDataType implements EnumMapper.IntegerEnum {
    /**
     * Use 32-bit single-precision floating point values, with range 0.0-1.0
     * (within gamut, may go outside this range for wide color gamut). Floating
     * point output, either JXL_TYPE_FLOAT or JXL_TYPE_FLOAT16, is recommended
     * for HDR and wide gamut images when color profile conversion is required.
     */
    JXL_TYPE_FLOAT(0),

    /**
     * Use 1-bit packed in uint8_t, first pixel in LSB, padded to uint8_t per
     * row.
     */
    JXL_TYPE_BOOLEAN(1),

    /**
     * Use type uint8_t. May clip wide color gamut data.
     */
    JXL_TYPE_UINT8(2),

    /**
     * Use type uint16_t. May clip wide color gamut data.
     */
    JXL_TYPE_UINT16(3),

    /**
     * Use type uint32_t. May clip wide color gamut data.
     */
    JXL_TYPE_UINT32(4),

    /**
     * Use 16-bit IEEE 754 half-precision floating point values
     */
    JXL_TYPE_FLOAT16(5);

    private final int val;

    JxlDataType(int val) {
        this.val = val;
    }

    @Override
    public int intValue() {
        return val;
    }

    public int sizeBytes() {
        switch (this) {
            case JXL_TYPE_FLOAT:
            case JXL_TYPE_UINT32:
                return 4;
            case JXL_TYPE_BOOLEAN:
            case JXL_TYPE_UINT8:
                return 1;
            case JXL_TYPE_UINT16:
            case JXL_TYPE_FLOAT16:
                return 2;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
