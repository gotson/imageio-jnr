package com.github.gotson.jnr.jxl.lib.structs;

import com.github.gotson.jnr.jxl.lib.enums.JxlDataType;
import com.github.gotson.jnr.jxl.lib.enums.JxlEndianness;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** Data type for the sample values per channel per pixel for the output buffer
 * for pixels. This is not necessarily the same as the data type encoded in the
 * codestream. The channels are interleaved per pixel. The pixels are
 * organized row by row, left to right, top to bottom.
 */
public class JxlPixelFormat extends Struct {
    /** Amount of channels available in a pixel buffer.
     * 1: single-channel data, e.g. grayscale or a single extra channel
     * 2: single-channel + alpha
     * 3: trichromatic, e.g. RGB
     * 4: trichromatic + alpha
     */
    public Struct.u_int32_t num_channels = new u_int32_t();

    /** Data type of each channel.
     */
    public Struct.Enum32<JxlDataType> data_type = new Enum32<>(JxlDataType.class);

    /** Whether multi-byte data types are represented in big endian or little
     * endian format. This applies to JXL_TYPE_UINT16, JXL_TYPE_UINT32
     * and JXL_TYPE_FLOAT.
     */
    public Struct.Enum32<JxlEndianness> endianness = new Enum32<>(JxlEndianness.class);

    /** Align scanlines to a multiple of align bytes, or 0 to require no
     * alignment at all (which has the same effect as value 1)
     */
    public Struct.size_t align = new size_t();

    public JxlPixelFormat(Runtime runtime) {
        super(runtime);
    }

    public JxlPixelFormat(Runtime runtime, int channels, JxlDataType dataType, JxlEndianness endianness, int align) {
        this(runtime);
        num_channels.set(channels);
        data_type.set(dataType);
        this.endianness.set(endianness);
        this.align.set(align);

    }
}
