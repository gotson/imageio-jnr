package com.github.gotson.jnr.jxl.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** The codestream preview header */
public class JxlPreviewHeader extends Struct {
    /** Preview width in pixels */
    public Struct.u_int32_t xsize = new u_int32_t();

    /** Preview height in pixels */
    public Struct.u_int32_t ysize = new u_int32_t();

    protected JxlPreviewHeader(Runtime runtime) {
        super(runtime);
    }
}
