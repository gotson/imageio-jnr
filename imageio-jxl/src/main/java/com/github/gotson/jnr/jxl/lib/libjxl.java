package com.github.gotson.jnr.jxl.lib;

import com.github.gotson.jnr.jxl.lib.structs.JxlPixelFormat;
import com.github.gotson.jnr.jxl.lib.enums.JxlColorProfileTarget;
import com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus;
import com.github.gotson.jnr.jxl.lib.enums.JxlOrientation;
import com.github.gotson.jnr.jxl.lib.enums.JxlSignature;
import com.github.gotson.jnr.jxl.lib.structs.JxlBasicInfo;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.NativeLongByReference;
import jnr.ffi.types.size_t;

import java.nio.Buffer;
import java.util.EnumSet;

public interface libjxl {
    /**
     * Decoder library version.
     *
     * @return the decoder library version as an integer:
     * MAJOR_VERSION * 1000000 + MINOR_VERSION * 1000 + PATCH_VERSION. For example,
     * version 1.2.3 would return 1002003.
     */
    int JxlDecoderVersion();

    /**
     * JPEG XL signature identification.
     * <p>
     * Checks if the passed buffer contains a valid JPEG XL signature. The passed
     * buf of size len doesn't need to be a full image, only the beginning of the file.
     *
     * @return a flag indicating if a JPEG XL signature was found and what type.
     * - JXL_SIG_NOT_ENOUGH_BYTES not enough bytes were passed to determine
     * if a valid signature is there.
     * - JXL_SIG_INVALID: no valid signature found for JPEG XL decoding.
     * - JXL_SIG_CODESTREAM a valid JPEG XL codestream signature was found.
     * - JXL_SIG_CONTAINER a valid JPEG XL container signature was found.
     */
    JxlSignature JxlSignatureCheck(@In Buffer buf, @In @size_t long len);

    /**
     * Creates an instance of JxlDecoder and initializes it.
     * <p>
     * memory_manager will be used for all the library dynamic allocations made
     * from this instance. The parameter may be NULL, in which case the default
     * allocator will be used. See jpegxl/memory_manager.h for details.
     *
     * @param memory_manager custom allocator function. It may be NULL. The memory
     *                       manager will be copied internally.
     * @return NULL if the instance can not be allocated or initialized, pointer to initialized JxlDecoder otherwise
     */
    Pointer JxlDecoderCreate(@In Pointer memory_manager);

    /**
     * Deinitializes and frees JxlDecoder instance.
     *
     * @param dec instance to be cleaned up and deallocated.
     */
    void JxlDecoderDestroy(@In Pointer dec);

    /**
     * Enables or disables preserving of original orientation. Some images are
     * encoded with an orientation tag indicating the image is rotated and/or
     * mirrored (here called the original orientation).
     * <p>
     * *) If keep_orientation is JXL_FALSE (the default): the decoder will perform
     * work to undo the transformation. This ensures the decoded pixels will not
     * be rotated or mirrored. The decoder will always set the orientation field
     * of the JxlBasicInfo to JXL_ORIENT_IDENTITY to match the returned pixel data.
     * The decoder may also swap xsize and ysize in the JxlBasicInfo compared to the
     * values inside of the codestream, to correctly match the decoded pixel data,
     * e.g. when a 90 degree rotation was performed.
     * <p>
     * *) If this option is JXL_TRUE: then the image is returned as-is, which may be
     * rotated or mirrored, and the user must check the orientation field in
     * JxlBasicInfo after decoding to correctly interpret the decoded pixel data.
     * This may be faster to decode since the decoder doesn't have to apply the
     * transformation, but can cause wrong display of the image if the orientation
     * tag is not correctly taken into account by the user.
     * <p>
     * By default, this option is disabled, and the decoder automatically corrects
     * the orientation.
     * <p>
     * This function must be called at the beginning, before decoding is performed.
     *
     * @param dec              decoder object
     * @param keep_orientation JXL_TRUE to enable, JXL_FALSE to disable.
     * @return JXL_DEC_SUCCESS if no error, JXL_DEC_ERROR otherwise.
     * @see JxlBasicInfo for the orientation field
     * @see JxlOrientation for the possible values.
     */
    JxlDecoderStatus JxlDecoderSetKeepOrientation(@In Pointer dec, @In boolean keep_orientation);

    /**
     * Select for which informative events (JXL_DEC_BASIC_INFO, etc...) the
     * decoder should return with a status. It is not required to subscribe to any
     * events, data can still be requested from the decoder as soon as it available.
     * By default, the decoder is subscribed to no events (events_wanted == 0), and
     * the decoder will then only return when it cannot continue because it needs
     * more input data or more output buffer. This function may only be be called
     * before using JxlDecoderProcessInput
     *
     * @param dec           decoder object
     * @param events_wanted bitfield of desired events.
     * @return JXL_DEC_SUCCESS if no error, JXL_DEC_ERROR otherwise.
     */
    JxlDecoderStatus JxlDecoderSubscribeEvents(@In Pointer dec, EnumSet<JxlDecoderStatus> events_wanted);

    /**
     * Sets input data for JxlDecoderProcessInput. The data is owned by the caller
     * and may be used by the decoder until JxlDecoderReleaseInput is called or
     * the decoder is destroyed or reset so must be kept alive until then.
     *
     * @param dec  decoder object
     * @param data pointer to next bytes to read from
     * @param size amount of bytes available starting from data
     * @return JXL_DEC_ERROR if input was already set without releasing,
     * JXL_DEC_SUCCESS otherwise
     */
    JxlDecoderStatus JxlDecoderSetInput(@In Pointer dec, @In Buffer data, @size_t long size);

    /**
     * Releases input which was provided with JxlDecoderSetInput. Between
     * JxlDecoderProcessInput and JxlDecoderReleaseInput, the user may not alter
     * the data in the buffer. Calling JxlDecoderReleaseInput is required whenever
     * any input is already set and new input needs to be added with
     * JxlDecoderSetInput, but is not required before JxlDecoderDestroy or
     * JxlDecoderReset. Calling JxlDecoderReleaseInput when no input is set is
     * not an error and returns 0.
     *
     * @param dec decoder object
     * @return the amount of bytes the decoder has not yet processed that are
     * still remaining in the data set by JxlDecoderSetInput, or 0 if no input is
     * set or JxlDecoderReleaseInput was already called. For a next call to
     * JxlDecoderProcessInput, the buffer must start with these unprocessed bytes.
     * This value doesn't provide information about how many bytes the decoder
     * truly processed internally or how large the original JPEG XL codestream or
     * file are.
     */
    @size_t
    long JxlDecoderReleaseInput(@In Pointer dec);

    /**
     * Marks the input as finished, indicates that no more JxlDecoderSetInput will
     * be called. This function allows the decoder to determine correctly if it
     * should return success, need more input or error in certain cases. For
     * backwards compatibility with a previous version of the API, using this
     * function is optional when not using the JXL_DEC_BOX event (the decoder is
     * able to determine the end of the image frames without marking the end), but
     * using this function is required when using JXL_DEC_BOX for getting metadata
     * box contents. This function does not replace JxlDecoderReleaseInput, that
     * function should still be called if its return value is needed.
     * JxlDecoderCloseInput should be called as soon as all known input bytes are
     * set (e.g. at the beginning when not streaming but setting all input at once),
     * before the final JxlDecoderProcessInput calls.
     * @param dec decoder object
     */
    // Unreleased as of 0.6.1
//    void JxlDecoderCloseInput(@In Pointer dec);

    /**
     * Decodes JPEG XL file using the available bytes. Requires input has been
     * set with JxlDecoderSetInput. After JxlDecoderProcessInput, input can
     * optionally be released with JxlDecoderReleaseInput and then set again to
     * next bytes in the stream. JxlDecoderReleaseInput returns how many bytes are
     * not yet processed, before a next call to JxlDecoderProcessInput all
     * unprocessed bytes must be provided again (the address need not match, but the
     * contents must), and more bytes may be concatenated after the unprocessed
     * bytes.
     * <p>
     * The returned status indicates whether the decoder needs more input bytes, or
     * more output buffer for a certain type of output data. No matter what the
     * returned status is (other than JXL_DEC_ERROR), new information, such as
     * JxlDecoderGetBasicInfo, may have become available after this call. When
     * the return value is not JXL_DEC_ERROR or JXL_DEC_SUCCESS, the decoding
     * requires more JxlDecoderProcessInput calls to continue.
     *
     * @param dec decoder object
     * @return JXL_DEC_SUCCESS when decoding finished and all events handled.
     * JXL_DEC_ERROR when decoding failed, e.g. invalid codestream.
     * JXL_DEC_NEED_MORE_INPUT more input data is necessary.
     * JXL_DEC_BASIC_INFO when basic info such as image dimensions is available and this informative event is subscribed to.
     * JXL_DEC_EXTENSIONS when JPEG XL codestream user extensions are available and this informative event is subscribed to.
     * JXL_DEC_COLOR_ENCODING when color profile information is available and this informative event is subscribed to.
     * JXL_DEC_PREVIEW_IMAGE when preview pixel information is available and output in the preview buffer.
     * JXL_DEC_DC_IMAGE when DC pixel information (8x8 downscaled version of the image) is available and output in the DC buffer.
     * JXL_DEC_FULL_IMAGE when all pixel information at highest detail is available and has been output in the pixel buffer.
     */
    JxlDecoderStatus JxlDecoderProcessInput(@In Pointer dec);

    /**
     * Outputs the basic image information, such as image dimensions, bit depth and
     * all other JxlBasicInfo fields, if available.
     *
     * @param dec  decoder object
     * @param info struct to copy the information into, or NULL to only check
     *             whether the information is available through the return value.
     * @return JXL_DEC_SUCCESS if the value is available,
     * JXL_DEC_NEED_MORE_INPUT if not yet available, JXL_DEC_ERROR in case
     * of other error conditions.
     */
    JxlDecoderStatus JxlDecoderGetBasicInfo(@In Pointer dec, JxlBasicInfo info);

    /**
     * Outputs the size in bytes of the ICC profile returned by
     * JxlDecoderGetColorAsICCProfile, if available, or indicates there is none
     * available. In most cases, the image will have an ICC profile available, but
     * if it does not, JxlDecoderGetColorAsEncodedProfile must be used instead.
     *
     * @param dec    decoder object
     * @param format pixel format to output the data to. Only used for
     *               JXL_COLOR_PROFILE_TARGET_DATA, may be nullptr otherwise.
     * @param target whether to get the original color profile from the metadata
     *               or the color profile of the decoded pixels.
     * @param size   variable to output the size into, or NULL to only check the
     *               return status.
     * @return JXL_DEC_SUCCESS if the ICC profile is available,
     * JXL_DEC_NEED_MORE_INPUT if the decoder has not yet received enough
     * input data to determine whether an ICC profile is available or what its
     * size is, JXL_DEC_ERROR in case the ICC profile is not available and
     * cannot be generated.
     * @see JxlDecoderGetColorAsEncodedProfile for more information. The ICC
     * profile is either the exact ICC profile attached to the codestream metadata,
     * or a close approximation generated from JPEG XL encoded structured data,
     * depending of what is encoded in the codestream.
     */
    JxlDecoderStatus JxlDecoderGetICCProfileSize(@In Pointer dec, @In JxlPixelFormat format, @In JxlColorProfileTarget target, @Out @size_t NativeLongByReference size);

    /**
     * Outputs ICC profile if available. The profile is only available if
     * JxlDecoderGetICCProfileSize returns success. The output buffer must have
     * at least as many bytes as given by JxlDecoderGetICCProfileSize.
     *
     * @param dec         decoder object
     * @param format      pixel format to output the data to. Only used for
     *                    JXL_COLOR_PROFILE_TARGET_DATA, may be nullptr otherwise.
     * @param target      whether to get the original color profile from the metadata
     *                    or the color profile of the decoded pixels.
     * @param icc_profile buffer to copy the ICC profile into
     * @param size        size of the icc_profile buffer in bytes
     * @return JXL_DEC_SUCCESS if the profile was successfully returned is
     * available, JXL_DEC_NEED_MORE_INPUT if not yet available,
     * JXL_DEC_ERROR if the profile doesn't exist or the output size is not
     * large enough.
     */
    JxlDecoderStatus JxlDecoderGetColorAsICCProfile(@In Pointer dec, @In JxlPixelFormat format, @In JxlColorProfileTarget target, @Out Buffer icc_profile,
                                                    @In @size_t long size);

    /**
     * Returns the minimum size in bytes of the image output pixel buffer for the
     * given format. This is the buffer for JxlDecoderSetImageOutBuffer. Requires
     * the basic image information is available in the decoder.
     *
     * @param dec    decoder object
     * @param format format of the pixels.
     * @param size   output value, buffer size in bytes
     * @return JXL_DEC_SUCCESS on success, JXL_DEC_ERROR on error, such as
     * information not available yet.
     */
    JxlDecoderStatus JxlDecoderImageOutBufferSize(@In Pointer dec, @In JxlPixelFormat format, @Out @size_t NativeLongByReference size);

    /**
     * Sets the buffer to write the full resolution image to. This can be set when
     * the JXL_DEC_FRAME event occurs, must be set when the
     * JXL_DEC_NEED_IMAGE_OUT_BUFFER event occurs, and applies only for the current
     * frame. The size of the buffer must be at least as large as given by
     * JxlDecoderImageOutBufferSize. The buffer follows the format described by
     * JxlPixelFormat. The buffer is owned by the caller.
     *
     * @param dec    decoder object
     * @param format format of the pixels. Object owned by user and its contents
     *               are copied internally.
     * @param buffer buffer type to output the pixel data to
     * @param size   size of buffer in bytes
     * @return JXL_DEC_SUCCESS on success, JXL_DEC_ERROR on error, such as
     * size too small.
     */
    JxlDecoderStatus JxlDecoderSetImageOutBuffer(@In Pointer dec, @In JxlPixelFormat format, @Out Buffer buffer, @In @size_t long size);
}
