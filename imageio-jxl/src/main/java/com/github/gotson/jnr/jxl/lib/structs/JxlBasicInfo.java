package com.github.gotson.jnr.jxl.lib.structs;

import com.github.gotson.jnr.jxl.lib.enums.JxlOrientation;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class JxlBasicInfo extends Struct {
    /**
     * Whether the codestream is embedded in the container format. If true,
     * metadata information and extensions may be available in addition to the
     * codestream.
     */
    public Struct.u_int32_t have_container = new u_int32_t();

    /**
     * Width of the image in pixels, before applying orientation.
     */
    public Struct.u_int32_t xsize = new u_int32_t();

    /**
     * Height of the image in pixels, before applying orientation.
     */
    public Struct.u_int32_t ysize = new u_int32_t();

    /**
     * Original image color channel bit depth.
     */
    public Struct.u_int32_t bits_per_sample = new u_int32_t();

    /**
     * Original image color channel floating point exponent bits, or 0 if they
     * are unsigned integer. For example, if the original data is half-precision
     * (binary16) floating point, bits_per_sample is 16 and
     * exponent_bits_per_sample is 5, and so on for other floating point
     * precisions.
     */
    public Struct.u_int32_t exponent_bits_per_sample = new u_int32_t();

    /**
     * Upper bound on the intensity level present in the image in nits. For
     * unsigned integer pixel encodings, this is the brightness of the largest
     * representable value. The image does not necessarily contain a pixel
     * actually this bright. An encoder is allowed to set 255 for SDR images
     * without computing a histogram.
     * Leaving this set to its default of 0 lets libjxl choose a sensible default
     * value based on the color encoding.
     */
    public Struct.Float intensity_target = new Float();

    /**
     * Lower bound on the intensity level present in the image. This may be
     * loose, i.e. lower than the actual darkest pixel. When tone mapping, a
     * decoder will map [min_nits, intensity_target] to the display range.
     */
    public Struct.Float min_nits = new Float();

    /**
     * See the description of @see linear_below.
     */
    public Struct.u_int32_t relative_to_max_display = new u_int32_t();

    /**
     * The tone mapping will leave unchanged (linear mapping) any pixels whose
     * brightness is strictly below this. The interpretation depends on
     * relative_to_max_display. If true, this is a ratio [0, 1] of the maximum
     * display brightness [nits], otherwise an absolute brightness [nits].
     */
    public Struct.Float  linear_below = new Float();

    /**
     * Whether the data in the codestream is encoded in the original color
     * profile that is attached to the codestream metadata header, or is
     * encoded in an internally supported absolute color space (which the decoder
     * can always convert to linear or non-linear sRGB or to XYB). If the original
     * profile is used, the decoder outputs pixel data in the color space matching
     * that profile, but doesn't convert it to any other color space. If the
     * original profile is not used, the decoder only outputs the data as sRGB
     * (linear if outputting to floating point, nonlinear with standard sRGB
     * transfer function if outputting to unsigned integers) but will not convert
     * it to to the original color profile. The decoder also does not convert to
     * the target display color profile, but instead will always indicate which
     * color profile the returned pixel data is encoded in when using @see
     * JXL_COLOR_PROFILE_TARGET_DATA so that a CMS can be used to convert the
     * data.
     */
    public Struct.u_int32_t uses_original_profile = new u_int32_t();

    /**
     * Indicates a preview image exists near the beginning of the codestream.
     * The preview itself or its dimensions are not included in the basic info.
     */
    public Struct.u_int32_t have_preview = new u_int32_t();

    /**
     * Indicates animation frames exist in the codestream. The animation
     * information is not included in the basic info.
     */
    public Struct.u_int32_t have_animation = new u_int32_t();

    /**
     * Image orientation, value 1-8 matching the values used by JEITA CP-3451C
     * (Exif version 2.3).
     */
    public Struct.Enum32<JxlOrientation> orientation = new Enum32<>(JxlOrientation.class);

    /**
     * Number of color channels encoded in the image, this is either 1 for
     * grayscale data, or 3 for colored data. This count does not include
     * the alpha channel or other extra channels. To check presence of an alpha
     * channel, such as in the case of RGBA color, check alpha_bits != 0.
     * If and only if this is 1, the JxlColorSpace in the JxlColorEncoding is
     * JXL_COLOR_SPACE_GRAY.
     */
    public Struct.u_int32_t num_color_channels = new u_int32_t();

    /**
     * Number of additional image channels. This includes the main alpha channel,
     * but can also include additional channels such as depth, additional alpha
     * channels, spot colors, and so on. Information about the extra channels
     * can be queried with JxlDecoderGetExtraChannelInfo. The main alpha channel,
     * if it exists, also has its information available in the alpha_bits,
     * alpha_exponent_bits and alpha_premultiplied fields in this JxlBasicInfo.
     */
    public Struct.u_int32_t num_extra_channels = new u_int32_t();

    /**
     * Bit depth of the encoded alpha channel, or 0 if there is no alpha channel.
     * If present, matches the alpha_bits value of the JxlExtraChannelInfo
     * associated with this alpha channel.
     */
    public Struct.u_int32_t alpha_bits = new u_int32_t();

    /**
     * Alpha channel floating point exponent bits, or 0 if they are unsigned. If
     * present, matches the alpha_bits value of the JxlExtraChannelInfo associated
     * with this alpha channel. integer.
     */
    public Struct.u_int32_t alpha_exponent_bits = new u_int32_t();

    /**
     * Whether the alpha channel is premultiplied. Only used if there is a main
     * alpha channel. Matches the alpha_premultiplied value of the
     * JxlExtraChannelInfo associated with this alpha channel.
     */
    public Struct.u_int32_t alpha_premultiplied = new u_int32_t();

    /**
     * Dimensions of encoded preview image, only used if have_preview is
     * JXL_TRUE.
     */
    public JxlPreviewHeader preview;

    /**
     * Animation header with global animation properties for all frames, only
     * used if have_animation is JXL_TRUE.
     */
    public JxlAnimationHeader animation;

    /**
     * Intrinsic width of the image.
     * The intrinsic size can be different from the actual size in pixels
     * (as given by xsize and ysize) and it denotes the recommended dimensions
     * for displaying the image, i.e. applications are advised to resample the
     * decoded image to the intrinsic dimensions.
     */
    public Struct.u_int32_t intrinsic_xsize = new u_int32_t();

    /**
     * Intrinsic heigth of the image.
     * The intrinsic size can be different from the actual size in pixels
     * (as given by xsize and ysize) and it denotes the recommended dimensions
     * for displaying the image, i.e. applications are advised to resample the
     * decoded image to the intrinsic dimensions.
     */
    public Struct.u_int32_t intrinsic_ysize = new u_int32_t();

    /**
     * Padding for forwards-compatibility, in case more fields are exposed
     * in a future version of the library.
     */
    public Struct.u_int8_t[] padding = new u_int8_t[100];

    public JxlBasicInfo(Runtime runtime) {
        super(runtime);
        preview = inner(new JxlPreviewHeader(runtime));
        animation = inner(new JxlAnimationHeader(runtime));
    }
}
