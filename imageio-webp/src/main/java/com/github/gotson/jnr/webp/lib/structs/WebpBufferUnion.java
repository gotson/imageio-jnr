package com.github.gotson.jnr.webp.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Union;

public class WebpBufferUnion extends Union {
    public WebPRGBABuffer RGBA;
    public WebPYUVABuffer YUVA;

    protected WebpBufferUnion(Runtime runtime, boolean rgba) {
        super(runtime);
        if (rgba) {
            RGBA = new WebPRGBABuffer(runtime);
        } else {
            YUVA = new WebPYUVABuffer(runtime);
        }
    }
}
