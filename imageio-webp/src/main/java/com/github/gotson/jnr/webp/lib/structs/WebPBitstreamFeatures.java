package com.github.gotson.jnr.webp.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/**
 * Features gathered from the bitstream
 */
public class WebPBitstreamFeatures extends Struct {
    /**
     * Width in pixels, as read from the bitstream.
     */
    public Signed32 width = new Signed32();

    /**
     * Height in pixels, as read from the bitstream.
     */
    public Signed32 height = new Signed32();

    /**
     * True if the bitstream contains an alpha channel.
     */
    public Signed32 has_alpha = new Signed32();

    /**
     * True if the bitstream is an animation.
     */
    public Signed32 has_animation = new Signed32();

    /**
     * 0 = undefined (/mixed), 1 = lossy, 2 = lossless
     */
    public Signed32 format = new Signed32();

    /**
     * padding for later use
     */
    public Struct.u_int32_t[] pad = new u_int32_t[5];

    public WebPBitstreamFeatures(Runtime runtime) {
        super(runtime);
    }
}
