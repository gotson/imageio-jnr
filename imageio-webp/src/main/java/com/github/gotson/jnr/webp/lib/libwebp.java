package com.github.gotson.jnr.webp.lib;

import com.github.gotson.jnr.webp.lib.structs.WebPDecoderConfig;
import com.github.gotson.jnr.webp.lib.enums.VP8StatusCode;
import com.github.gotson.jnr.webp.lib.structs.WebPBitstreamFeatures;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.types.size_t;

import java.nio.Buffer;

public interface libwebp {
    boolean WebPGetInfo(
        @In Buffer data,
        @In @size_t long data_size,
        @Out IntByReference width,
        @Out IntByReference height);

    /**
     * Initialize the configuration as empty. This function must always be
     * called first, unless WebPGetFeatures() is to be called.
     * Returns false in case of mismatched version.
     */
    boolean WebPInitDecoderConfigInternal(
        @Out WebPDecoderConfig config,
        @In int version
    );

    /**
     * Retrieve features from the bitstream.
     * The *features structure is filled with information
     * gathered from the bitstream. Returns VP8_STATUS_OK when the features are successfully
     * retrieved. Returns VP8_STATUS_NOT_ENOUGH_DATA when more data is needed to retrieve the features
     * from headers. Returns error in other cases. Note: The following chunk sequences (before the raw
     * VP8/VP8L data) are considered valid by this function: RIFF + VP8(L) RIFF + VP8X + (optional
     * chunks) + VP8(L) ALPH + VP8 <-- Not a valid WebP format: only allowed for internal purpose.
     * VP8(L) <-- Not a valid WebP format: only allowed for internal purpose.
     */
    VP8StatusCode WebPGetFeaturesInternal(
        @In Buffer data, @In @size_t long data_size, @Out WebPBitstreamFeatures features, @In int version);

    VP8StatusCode WebPDecode(
        @In Buffer data, @In @size_t long data_size,
        WebPDecoderConfig config
    );

    Pointer WebPDecodeBGRInto(
        @In Buffer data, @In @size_t long data_size,
        @Out Buffer output_buffer, @In @size_t long output_buffer_size, @In int output_stride);

    Pointer WebPDecodeBGRAInto(
        @In Buffer data, @In @size_t long data_size,
        @Out Buffer output_buffer, @In @size_t long output_buffer_size, @In int output_stride);

//    Pointer WebPDecodeBGR(
//        @In Buffer webpBuf,
//        @size_t long webpSize,
//        @Out IntByReference width,
//        @Out IntByReference height);
}
