package com.github.gotson.jnr.webp;

import com.github.gotson.jnr.webp.lib.enums.VP8StatusCode;
import com.github.gotson.jnr.webp.lib.enums.WEBP_CSP_MODE;
import com.github.gotson.jnr.webp.lib.libwebp;
import com.github.gotson.jnr.webp.lib.structs.WebPBitstreamFeatures;
import com.github.gotson.jnr.webp.lib.structs.WebPDecoderConfig;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Hashtable;

import static com.github.gotson.jnr.webp.lib.enums.VP8StatusCode.VP8_STATUS_OK;

/**
 * Java bindings for libwebp via JFFI *
 */
public class Webp {

    private static final Logger LOG = LoggerFactory.getLogger(Webp.class);
    public libwebp lib;
    public Runtime runtime;
    private final int minDecoderAbi = Integer.parseInt("0200", 16);

    public Webp() {
        lib = LibraryLoader.create(libwebp.class).load("webp");
        runtime = Runtime.getRuntime(lib);
    }

    /**
     * Return information about the WebP image in the input buffer
     *
     * @param webpData webp image data
     * @return information about the webp image
     * @throws WebpException if decompressing header with library fails
     */
    public Info getFeatures(byte[] webpData) throws WebpException {
        WebPBitstreamFeatures features = new WebPBitstreamFeatures(runtime);
        VP8StatusCode rv = WebPGetFeatures(ByteBuffer.wrap(webpData), webpData.length, features);
        if (rv != VP8_STATUS_OK) {
            throw new WebpException("Could not decode headers");
        }

        Info.Format format;
        switch (features.format.get()) {
            case 2:
                format = Info.Format.LOSSLESS;
                break;
            case 1:
                format = Info.Format.LOSSY;
                break;
            default:
                format = Info.Format.UNDEFINED_OR_MIXED;
        }
        return new Info(features.width.get(), features.height.get(), features.has_alpha.get() > 0, features.has_animation.get() > 0, format);
    }

    private VP8StatusCode WebPGetFeatures(Buffer data, long data_size, WebPBitstreamFeatures features) {
        return lib.WebPGetFeaturesInternal(data, data_size, features, minDecoderAbi);
    }

    private boolean WebPInitDecoderConfig(WebPDecoderConfig config) {
        return lib.WebPInitDecoderConfigInternal(config, minDecoderAbi);
    }

    /**
     * Decode the JPEG image in the input buffer into a BufferedImage.
     *
     * @param webpData JPEG data input buffer
     * @param info     Information about the JPEG image in the buffer
     * @return The decoded image
     * @throws WebpException if decompression with library fails
     */
    public BufferedImage decodeSimple(byte[] webpData, Info info) throws WebpException {
        int width = info.getWidth();
        int height = info.getHeight();
        boolean hasAlpha = info.hasAlpha();
        int imgType;
        int b;

        if (hasAlpha) {
            imgType = BufferedImage.TYPE_INT_ARGB;
            b = 4;
        } else {
            imgType = BufferedImage.TYPE_3BYTE_BGR;
            b = 3;
        }

        BufferedImage img = new BufferedImage(width, height, imgType);
        ByteBuffer byteBuffer = ByteBuffer.wrap(webpData);

        if (hasAlpha) {
            IntBuffer outBuf = asIntBuffer(img.getRaster().getDataBuffer());
            lib.WebPDecodeBGRAInto(byteBuffer, webpData.length, outBuf, (long) width * height * b, width * b);
        } else {
            ByteBuffer outBuf = asByteBuffer(img.getRaster().getDataBuffer());
            lib.WebPDecodeBGRInto(byteBuffer, webpData.length, outBuf, (long) width * height * b, width * b);
        }
        return img;
    }

    public BufferedImage decodeComplex(byte[] webpData, Info info, Dimension size) throws WebpException {
        int width = info.getWidth();
        int height = info.getHeight();
        //      if (size != null) {
        //        if (!info.getAvailableSizes().contains(size)) {
        //          throw new IllegalArgumentException(
        //              String.format("Invalid size, must be one of %s", info.getAvailableSizes()));
        //        } else {
        //          width = size.width;
        //          height = size.height;
        //        }
        //      }
        boolean hasAlpha = info.hasAlpha();
        int b;
        ColorModel colorModel;
        if (hasAlpha) {
            colorModel = new DirectColorModel( 32, 0x0000ff00, 0x00ff0000, 0xff000000, 0x000000ff );
            b = 4;
        } else {
            colorModel = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
            b = 3;
        }

        WebPDecoderConfig config = new WebPDecoderConfig(runtime);
        boolean rv = WebPInitDecoderConfig(config);
        if (!rv) {
            throw new WebpException("Could not init decoder config");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(webpData);
//        VP8StatusCode code = WebPGetFeatures(byteBuffer, webpData.length, config.input);

//        if (code != VP8_STATUS_OK) {
//            throw new WebpException("Could not get features");
//        }

        config.output.colorspace = hasAlpha ? WEBP_CSP_MODE.MODE_RGBA : WEBP_CSP_MODE.MODE_BGR;

        config.output.u.RGBA.stride.set(width * b);
        config.output.u.RGBA.size.set((long) width * height * b);
        config.output.is_external_memory.set(1);
        ByteBuffer outBuf = ByteBuffer.allocate(width * height * b);
        config.output.u.RGBA.rgba.set(Pointer.wrap(runtime, outBuf));

        VP8StatusCode statusCode = lib.WebPDecode(byteBuffer, webpData.length, config);

        if (statusCode != VP8_STATUS_OK) {
            LOG.error("Could not decompress WEBP (dimensions: {}x{}, alpha: {})", width, height, hasAlpha);
            throw new WebpException("");
        }

        SampleModel sampleModel = colorModel.createCompatibleSampleModel( width, height );
        DataBufferInt db = new DataBufferInt(outBuf.asIntBuffer().array(), width * height);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, db, null);

        return new BufferedImage( colorModel, raster, true, new Hashtable<>() );
    }

//    /**
//     * Encode an image to JPEG
//     *
//     * @param img image as rectangle of pixels
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
//     * @throws WebpException if image transformation fails
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
