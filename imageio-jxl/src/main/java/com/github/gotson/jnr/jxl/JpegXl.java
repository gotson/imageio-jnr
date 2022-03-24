package com.github.gotson.jnr.jxl;

import com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus;
import com.github.gotson.jnr.jxl.lib.enums.JxlSignature;
import com.github.gotson.jnr.jxl.lib.libjxl;
import com.github.gotson.jnr.jxl.lib.structs.JxlBasicInfo;
import com.github.gotson.jnr.jxl.lib.structs.JxlPixelFormat;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.byref.NativeLongByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.EnumSet;

import static com.github.gotson.jnr.jxl.lib.enums.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_ORIGINAL;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDataType.JXL_TYPE_UINT8;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus.JXL_DEC_BASIC_INFO;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus.JXL_DEC_COLOR_ENCODING;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus.JXL_DEC_ERROR;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus.JXL_DEC_FULL_IMAGE;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus.JXL_DEC_NEED_IMAGE_OUT_BUFFER;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus.JXL_DEC_NEED_MORE_INPUT;
import static com.github.gotson.jnr.jxl.lib.enums.JxlDecoderStatus.JXL_DEC_SUCCESS;
import static com.github.gotson.jnr.jxl.lib.enums.JxlEndianness.JXL_NATIVE_ENDIAN;

/**
 * Java bindings for libjxl via JFFI *
 */
public class JpegXl {

    private static final Logger LOG = LoggerFactory.getLogger(JpegXl.class);
    public libjxl lib;
    public Runtime runtime;
    private final int minDecoderAbi = Integer.parseInt("0200", 16);

    public JpegXl() {
        lib = LibraryLoader.create(libjxl.class).load("jxl");
        runtime = Runtime.getRuntime(lib);
        if (LOG.isInfoEnabled()) {
            LOG.info("Loaded version {}", getLibVersion());
        }
    }

    public boolean canDecode(byte[] data) {
        JxlSignature signature = lib.JxlSignatureCheck(ByteBuffer.wrap(data), data.length);
        return signature == JxlSignature.JXL_SIG_CODESTREAM || signature == JxlSignature.JXL_SIG_CONTAINER;
    }

    public String getLibVersion() {
        int versionInt = lib.JxlDecoderVersion();
        int major = versionInt / 1000000;
        int minor = (versionInt - major * 1000000) / 1000;
        int patch = versionInt - major * 1000000 - minor * 1000;
        return String.format("%d.%d.%d", major, minor, patch);
    }

    public JxlBasicInfo getBasicInfo(byte[] jxlData) throws JxlException {
        Pointer dec = lib.JxlDecoderCreate(null);

        try {
            if (dec == null) {
                throw new JxlException("JxlDecoderCreate failed");
            }

            lib.JxlDecoderSetKeepOrientation(dec, true);

            if (JXL_DEC_SUCCESS != lib.JxlDecoderSubscribeEvents(dec, EnumSet.of(JXL_DEC_BASIC_INFO))) {
                throw new JxlException("JxlDecoderSubscribeEvents failed");
            }

            JxlBasicInfo info = new JxlBasicInfo(runtime);

            lib.JxlDecoderSetInput(dec, ByteBuffer.wrap(jxlData), jxlData.length);
//            lib.JxlDecoderCloseInput(dec);
            JxlDecoderStatus status = lib.JxlDecoderProcessInput(dec);

            if (status == JXL_DEC_ERROR) {
                throw new JxlException("Decoder error");
            }
            if (status == JXL_DEC_NEED_MORE_INPUT) {
                throw new JxlException("Error, already provided all input");
            }
            if (status == JXL_DEC_BASIC_INFO) {
                if (JXL_DEC_SUCCESS != lib.JxlDecoderGetBasicInfo(dec, info)) {
                    throw new JxlException("JxlDecoderGetBasicInfo failed");
                }
                return info;
            }

            throw new JxlException("No basic info found");
        } finally {
            lib.JxlDecoderDestroy(dec);
        }
    }

    public BufferedImage decode(byte[] jxlData) throws JxlException {
        Pointer dec = lib.JxlDecoderCreate(null);

        try {
            if (dec == null) {
                throw new JxlException("JxlDecoderCreate failed");
            }

            if (JXL_DEC_SUCCESS !=
                lib.JxlDecoderSubscribeEvents(dec, EnumSet.of(JXL_DEC_BASIC_INFO, JXL_DEC_COLOR_ENCODING, JXL_DEC_FULL_IMAGE))) {
                throw new JxlException("JxlDecoderSubscribeEvents failed");
            }

            lib.JxlDecoderSetInput(dec, ByteBuffer.wrap(jxlData), jxlData.length);

            JxlBasicInfo info = new JxlBasicInfo(runtime);
            JxlPixelFormat format = new JxlPixelFormat(runtime);
            ByteBuffer iccProfile = ByteBuffer.allocate(0);
            ByteBuffer pixels = ByteBuffer.allocate(0);

            while (true) {
                JxlDecoderStatus status = lib.JxlDecoderProcessInput(dec);

                if (status == JXL_DEC_ERROR) {
                    throw new JxlException("Decoder error");
                }
                if (status == JXL_DEC_NEED_MORE_INPUT) {
                    throw new JxlException("Error, already provided all input");
                }

                if (status == JXL_DEC_BASIC_INFO) {
                    if (JXL_DEC_SUCCESS != lib.JxlDecoderGetBasicInfo(dec, info)) {
                        throw new JxlException("JxlDecoderGetBasicInfo failed");
                    }
                    format = new JxlPixelFormat(runtime,
                        info.num_color_channels.intValue() + (info.alpha_bits.get() > 0 ? 1 : 0),
                        JXL_TYPE_UINT8,
                        JXL_NATIVE_ENDIAN,
                        0);
                } else if (status == JXL_DEC_COLOR_ENCODING) {
                    // Get the ICC color profile of the pixel data
                    NativeLongByReference iccSize = new NativeLongByReference();
                    if (JXL_DEC_SUCCESS != lib.JxlDecoderGetICCProfileSize(dec, format, JXL_COLOR_PROFILE_TARGET_ORIGINAL, iccSize)) {
                        throw new JxlException("JxlDecoderGetICCProfileSize failed");
                    }

                    iccProfile = ByteBuffer.allocate(iccSize.intValue());
                    if (JXL_DEC_SUCCESS != lib.JxlDecoderGetColorAsICCProfile(dec, format, JXL_COLOR_PROFILE_TARGET_ORIGINAL, iccProfile, iccSize.longValue())) {
                        throw new JxlException("JxlDecoderGetColorAsICCProfile failed");
                    }
                } else if (status == JXL_DEC_NEED_IMAGE_OUT_BUFFER) {
                    NativeLongByReference bufferSizeBytes = new NativeLongByReference();
                    if (JXL_DEC_SUCCESS != lib.JxlDecoderImageOutBufferSize(dec, format, bufferSizeBytes)) {
                        throw new JxlException("JxlDecoderImageOutBufferSize failed");
                    }
                    if (bufferSizeBytes.intValue() != info.xsize.get() * info.ysize.get() * format.num_channels.get() * format.data_type.get().sizeBytes()) {
                        throw new JxlException("Invalid out buffer size");
                    }

                    pixels = ByteBuffer.allocate(bufferSizeBytes.intValue());
                    if (JXL_DEC_SUCCESS != lib.JxlDecoderSetImageOutBuffer(dec, format, pixels, bufferSizeBytes.longValue())) {
                        throw new JxlException("JxlDecoderSetImageOutBuffer failed");
                    }
                } else if (status == JXL_DEC_FULL_IMAGE) {
                    // Nothing to do. Do not yet return. If the image is an animation, more
                    // full frames may be decoded. This example only keeps the last one.
                } else if (status == JXL_DEC_SUCCESS) {
                    // All decoding successfully finished.
                    // It's not required to call JxlDecoderReleaseInput(dec.get()) here since
                    // the decoder will be destroyed.
                    break;
                } else {
                    throw new JxlException("Unknown decoder status: " + status);
                }
            }

            return null;

        } finally {
            lib.JxlDecoderDestroy(dec);
        }
    }


//    /**
//     * Encode an image to JPEG
//     *
//     * @param img     image as rectangle of pixels
//     * @param quality compression quality
//     * @return jpeg image
//     * @throws de.digitalcollections.turbojpeg.TurboJpegException if compression with library fails
//     */
    //  public ByteBuffer encode(Raster img, int quality) throws WebpException {
    //    Pointer codec = null;
    //    PointerByReference bufPtrRef = null;
    //    try {
    //      TJPF pixelFmt;
    //      switch (img.getNumBands()) {
    //        case 4:
    //          pixelFmt = TJPF.TJPF_BGRX; // 4BYTE_BGRA
    //          break;
    //        case 3:
    //          pixelFmt = TJPF.TJPF_BGR; // 3BYTE_BGR
    //          break;
    //        case 1:
    //          pixelFmt = TJPF.TJPF_GRAY; // 1BYTE_GRAY
    //          break;
    //        default:
    //          throw new IllegalArgumentException("Illegal sample format");
    //      }
    //      // TODO: Make sampling format configurable
    //      TJSAMP sampling = pixelFmt == TJPF.TJPF_GRAY ? TJSAMP.TJSAMP_GRAY : TJSAMP.TJSAMP_420;
    //      codec = lib.tjInitCompress();
    //
    //      // Allocate JPEG target buffer
    //      int bufSize = (int) lib.tjBufSize(img.getWidth(), img.getHeight(), sampling);
    //      Pointer bufPtr = lib.tjAlloc(bufSize);
    //      bufPtrRef = new PointerByReference(bufPtr);
    //      NativeLongByReference lenPtr = new NativeLongByReference(bufSize);
    //
    //      // Wrap source image data buffer with ByteBuffer to pass it over the ABI
    //      ByteBuffer inBuf;
    //      if (img.getNumBands() == 1 && img.getSampleModel().getSampleSize(0) == 1) {
    //        // For binary images, we need to convert our (0, 1) binary values into (0, 255)
    // greyscale
    //        // values
    //        int[] buf = new int[img.getWidth() * img.getHeight()];
    //        img.getPixels(0, 0, img.getWidth(), img.getHeight(), buf);
    //        byte[] byteBuf = new byte[buf.length];
    //        for (int i = 0; i < buf.length; i++) {
    //          byteBuf[i] = (byte) (buf[i] == 0 ? 0x00 : 0xFF);
    //        }
    //        inBuf = ByteBuffer.wrap(byteBuf).order(runtime.byteOrder());
    //      } else {
    //        inBuf = asByteBuffer(img.getDataBuffer());
    //      }
    //      int rv =
    //          lib.tjCompress2(
    //              codec,
    //              inBuf,
    //              img.getWidth(),
    //              0,
    //              img.getHeight(),
    //              pixelFmt,
    //              bufPtrRef,
    //              lenPtr,
    //              sampling,
    //              quality,
    //              0);
    //      if (rv != 0) {
    //        LOG.error(
    //            "Could not compress image (dimensions: {}x{}, format: {}, sampling: {}, quality:
    // {}",
    //            img.getWidth(),
    //            img.getHeight(),
    //            pixelFmt,
    //            sampling,
    //            quality);
    //        throw new WebpException(lib.tjGetErrorStr());
    //      }
    //      ByteBuffer outBuf =
    //          ByteBuffer.allocate(lenPtr.getValue().intValue()).order(runtime.byteOrder());
    //      bufPtrRef.getValue().get(0, outBuf.array(), 0, lenPtr.getValue().intValue());
    //      ((Buffer) outBuf).rewind();
    //      return outBuf;
    //    } finally {
    //      if (codec != null && codec.address() != 0) {
    //        lib.tjDestroy(codec);
    //      }
    //      if (bufPtrRef != null
    //          && bufPtrRef.getValue() != null
    //          && bufPtrRef.getValue().address() != 0) {
    //        lib.tjFree(bufPtrRef.getValue());
    //      }
    //    }
    //  }

//    /**
//     * Transform a JPEG image without decoding it fully
//     *
//     * @param jpegData JPEG input buffer
//     * @param info     Information about the JPEG (from {@link #getInfo(byte[])}
//     * @param region   Source region to crop out of JPEG
//     * @param rotation Degrees to rotate the JPEG, must be 90, 180 or 270
//     * @return The transformed JPEG data
//     * @throws JxlException if image transformation fails
//     */
    //  public ByteBuffer transform(byte[] jpegData, Info info, Rectangle region, int rotation)
    //      throws WebpException {
    //    Pointer codec = null;
    //    PointerByReference bufPtrRef = null;
    //    try {
    //      codec = lib.tjInitTransform();
    //      tjtransform transform = new tjtransform(runtime);
    //
    //      int width = info.getWidth();
    //      int height = info.getHeight();
    //      boolean flipCoords = rotation == 90 || rotation == 270;
    //      if (region != null) {
    //        Dimension mcuSize = new Dimension(info.getWidth(), info.getHeight()); //
    // info.getMCUSize();
    //        if (((region.x + region.width) != width && region.width % mcuSize.width != 0)
    //            || ((region.y + region.height) != height && region.height % mcuSize.height != 0)) {
    //          throw new IllegalArgumentException(
    //              String.format(
    //                  "Invalid cropping region %d×%d, width must be divisible by %d, height by %d",
    //                  region.width, region.height, mcuSize.width, mcuSize.height));
    //        }
    //        transform.options.set(TJXOPT.TJXOPT_CROP | TJXOPT.TJXOPT_TRIM);
    //        transform.r.x.set(region.x);
    //        transform.r.y.set(region.y);
    //        // If any cropping dimension equals the original dimension, libturbojpeg requires it to
    // be
    //        // set to 0
    //        if ((region.x + region.width) >= (flipCoords ? info.getHeight() : info.getWidth())) {
    //          transform.r.w.set(0);
    //        } else {
    //          transform.r.w.set(region.width);
    //        }
    //        if ((region.y + region.height) >= (flipCoords ? info.getWidth() : info.getHeight())) {
    //          transform.r.h.set(0);
    //        } else {
    //          transform.r.h.set(region.height);
    //        }
    //      }
    //      if (rotation != 0) {
    //        TJXOP op;
    //        switch (rotation) {
    //          case 90:
    //            op = TJXOP.TJXOP_ROT90;
    //            break;
    //          case 180:
    //            op = TJXOP.TJXOP_ROT180;
    //            break;
    //          case 270:
    //            op = TJXOP.TJXOP_ROT270;
    //            break;
    //          default:
    //            throw new IllegalArgumentException("Invalid rotation, must be 90, 180 or 270");
    //        }
    //        transform.op.set(op.intValue());
    //      }
    //      Buffer inBuf = ByteBuffer.wrap(jpegData).order(runtime.byteOrder());
    //      NativeLongByReference lenRef = new NativeLongByReference();
    //      bufPtrRef = new PointerByReference();
    //      int rv = lib.tjTransform(codec, inBuf, jpegData.length, 1, bufPtrRef, lenRef, transform,
    // 0);
    //      if (rv != 0) {
    //        LOG.error(
    //            "Could not compress image (crop: {},{},{},{}, rotate: {})",
    //            transform.r.x,
    //            transform.r.y,
    //            transform.r.w,
    //            transform.r.h,
    //            rotation);
    //        throw new WebpException(lib.tjGetErrorStr());
    //      }
    //      ByteBuffer outBuf =
    //          ByteBuffer.allocate(lenRef.getValue().intValue()).order(runtime.byteOrder());
    //      bufPtrRef.getValue().get(0, outBuf.array(), 0, lenRef.getValue().intValue());
    //      ((Buffer) outBuf).rewind();
    //      return outBuf;
    //    } finally {
    //      if (codec != null && codec.address() != 0) {
    //        lib.tjDestroy(codec);
    //      }
    //      if (bufPtrRef != null
    //          && bufPtrRef.getValue() != null
    //          && bufPtrRef.getValue().address() != 0) {
    //        lib.tjFree(bufPtrRef.getValue());
    //      }
    //    }
    //  }
    private ByteBuffer asByteBuffer(DataBuffer dataBuffer) {
        ByteBuffer byteBuffer;
        if (dataBuffer instanceof DataBufferByte) {
            byte[] pixelData = ((DataBufferByte) dataBuffer).getData();
            byteBuffer = ByteBuffer.wrap(pixelData).order(runtime.byteOrder());
        } else if (dataBuffer instanceof DataBufferUShort) {
            short[] pixelData = ((DataBufferUShort) dataBuffer).getData();
            byteBuffer = ByteBuffer.allocate(pixelData.length * 2).order(runtime.byteOrder());
            byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
        } else if (dataBuffer instanceof DataBufferShort) {
            short[] pixelData = ((DataBufferShort) dataBuffer).getData();
            byteBuffer = ByteBuffer.allocate(pixelData.length * 2).order(runtime.byteOrder());
            byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
        } else if (dataBuffer instanceof DataBufferInt) {
            int[] pixelData = ((DataBufferInt) dataBuffer).getData();
            byteBuffer = ByteBuffer.allocate(pixelData.length * 4).order(runtime.byteOrder());
            byteBuffer.asIntBuffer().put(IntBuffer.wrap(pixelData));
        } else {
            throw new IllegalArgumentException("Unsupported DataBuffer type: " + dataBuffer.getClass());
        }
        return byteBuffer;
    }

    private IntBuffer asIntBuffer(DataBuffer dataBuffer) {
        IntBuffer intBuffer;
        if (dataBuffer instanceof DataBufferInt) {
            int[] pixelData = ((DataBufferInt) dataBuffer).getData();
            intBuffer = IntBuffer.wrap(pixelData);
        } else {
            throw new IllegalArgumentException("Unsupported DataBuffer type: " + dataBuffer.getClass());
        }
        return intBuffer;
    }
}
