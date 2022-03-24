package com.github.gotson.jnr.jxl.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** The codestream animation header, optionally present in the beginning of
 * the codestream, and if it is it applies to all animation frames, unlike
 * JxlFrameHeader which applies to an individual frame.
 */
public class JxlAnimationHeader extends Struct {
    /** Numerator of ticks per second of a single animation frame time unit */
    public Struct.u_int32_t tps_numerator = new u_int32_t();

    /** Denominator of ticks per second of a single animation frame time unit */
    public Struct.u_int32_t tps_denominator = new u_int32_t();

    /** Amount of animation loops, or 0 to repeat infinitely */
    public Struct.u_int32_t num_loops = new u_int32_t();

    /** Whether animation time codes are present at animation frames in the
     * codestream */
    public Struct.u_int32_t have_timecodes= new u_int32_t();

    protected JxlAnimationHeader(Runtime runtime) {
        super(runtime);
    }
}
