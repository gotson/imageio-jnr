package com.github.gotson.jnr.jxl.imageio;

import com.github.gotson.jnr.jxl.Info;
import com.github.gotson.jnr.jxl.JpegXl;
import com.github.gotson.jnr.jxl.JxlException;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JxlImageReader extends ImageReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(JxlImageReader.class);

  private final JpegXl lib;
  private ByteBuffer data;
  private Info info;

  protected JxlImageReader(ImageReaderSpi originatingProvider, JpegXl lib) {
    super(originatingProvider);
    this.lib = lib;
  }

  @Override
  public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
    super.setInput(input, seekForwardOnly, ignoreMetadata);
    if (input == null) {
      return;
    }
    if (input instanceof ImageInputStream) {
      try {
        data = bufferFromStream((ImageInputStream) input);
//        info = lib.getFeatures(data.array());
      } catch (IOException e) {
        LOGGER.error(e.getMessage());
        throw new IllegalArgumentException("Failed to read input.");
      }
    } else {
      throw new IllegalArgumentException("Bad input.");
    }
  }

  private void checkIndex(int imageIndex) {
    //    if (imageIndex >= info.getAvailableSizes().size()) {
    //      throw new IndexOutOfBoundsException("bad index");
    //    }
  }

  static ByteBuffer bufferFromStream(ImageInputStream stream) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final byte[] buf = new byte[8192];
    int n;
    while (0 < (n = stream.read(buf))) {
      bos.write(buf, 0, n);
    }
    return ByteBuffer.wrap(bos.toByteArray());
  }

  @Override
  public ImageReadParam getDefaultReadParam() {
    return new JxlImageReadParam();
  }

  /**
   * The number of images corresponds to the number of different resolutions that can be directly
   * decoded.
   */
  @Override
  public int getNumImages(boolean allowSearch) {
    //    return info.getAvailableSizes().size();
    return 1;
  }

  //  private Dimension getDimension(int imageIndex) {
  //    checkIndex(imageIndex);
  //    return info.getAvailableSizes().get(imageIndex);
  //  }

  @Override
  public int getWidth(int imageIndex) {
    checkIndex(imageIndex);
    return info.getWidth();
  }

  @Override
  public int getHeight(int imageIndex) {
    checkIndex(imageIndex);
    return info.getHeight();
  }

  @Override
  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) {
    //    return Stream.of(
    //            ImageTypeSpecifier.createFromBufferedImageType(
    //                info.getColorspace() == TJCS.TJCS_GRAY ? TYPE_BYTE_GRAY : TYPE_3BYTE_BGR))
    //        .iterator();
    return Collections.singletonList(
            ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB))
        .iterator();
  }

  /**
   * Since TurboJPEG can only crop to values divisible by the MCU size, we may need to expand the
   * cropping area to get a suitable rectangle. Thus, cropping becomes a two-stage process: 1. Crop
   * to to nearest MCU boundaries (TurboJPEG) 2. Crop to the actual region (Java). <strong>This
   * method <em>mutates</em> the region!</strong>
   *
   * <p>Additionally, since TurboJPEG applies rotation **before** cropping, but the ImageIO API is
   * based on the assumption that rotation occurs **after** cropping, we have to transform the
   * cropping region accordingly.
   *
   * @param mcuSize The size of the MCUs
   * @param region The source region to be cropped
   * @param rotation Degrees the image is supposed to be rotated.
   * @param imageSize Dimensions of the image the cropping region targets
   * @return The region that needs to be cropped from the image cropped to the expanded rectangle
   */
  Rectangle adjustRegion(Dimension mcuSize, Rectangle region, int rotation, Dimension imageSize) {
    if (region == null) {
      return null;
    }
    final int originalWidth = imageSize.width;
    final int originalHeight = imageSize.height;

    // Recalculate the cropping region based on the desired rotation.
    final Rectangle originalRegion = (Rectangle) region.clone();
    if (rotation == 90) {
      int x = region.x;
      region.x = originalHeight - region.height - region.y;
      region.y = x;
    }
    if (rotation == 180) {
      region.x = originalWidth - region.width - region.x;
      region.y = originalHeight - region.height - region.y;
    }
    if (rotation == 270) {
      int x = region.x;
      region.x = region.y;
      region.y = originalWidth - region.width - x;
    }
    if (rotation == 90 || rotation == 270) {
      int w = region.width;
      region.width = region.height;
      region.height = w;
    }

    // Calculate how much of the region returned from libjpeg has to be cropped on the JVM-side
    Rectangle extraCrop =
        new Rectangle(
            0,
            0,
            region.width == 0 ? originalWidth - region.x : region.width,
            region.height == 0 ? originalHeight - region.y : region.height);
    // X-Offset + Width
    if (region.x % mcuSize.width != 0) {
      extraCrop.x = region.x % mcuSize.width;
      region.x -= extraCrop.x;
      if (region.width > 0) {
        region.width = Math.min(region.width + extraCrop.x, originalWidth - region.x);
      }
    }
    // Y-Offset + Height
    if (region.y % mcuSize.height != 0) {
      extraCrop.y = region.y % mcuSize.height;
      region.y -= extraCrop.y;
      if (region.height > 0) {
        region.height = Math.min(region.height + extraCrop.y, originalHeight - region.y);
      }
    }

    if ((region.x + region.width) != originalWidth && region.width % mcuSize.width != 0) {
      region.width =
          Math.min(
              (int) (mcuSize.width * (Math.ceil(region.getWidth() / mcuSize.width))),
              imageSize.width - region.x);
    }

    if ((region.y + region.height) != originalHeight && region.height % mcuSize.height != 0) {
      region.height =
          Math.min(
              (int) (mcuSize.height * (Math.ceil(region.getHeight() / mcuSize.height))),
              imageSize.height - region.y);
    }

    boolean modified =
        originalRegion.x != region.x
            || originalRegion.y != region.y
            || originalRegion.width != region.width
            || originalRegion.height != region.height;
    if (modified) {
      return extraCrop;
    } else {
      return null;
    }
  }

  /**
   * While the regular cropping parameters are applied to the unscaled source image, the additional
   * extra cropping on the Java side of things is applied to the decoded and possibly scaled image.
   * Thus, we need to scale down the extra cropping rectangle.
   */
  private void adjustExtraCrop(int imageIndex, Info croppedInfo, Rectangle rectangle) {
    //    double factor =
    //        croppedInfo.getAvailableSizes().get(imageIndex).getWidth()
    //            / croppedInfo.getAvailableSizes().get(0).getWidth();
    //    if (factor < 1) {
    //      rectangle.x = (int) Math.round(factor * rectangle.x);
    //      rectangle.y = (int) Math.round(factor * rectangle.y);
    //      rectangle.width = (int) Math.round(factor * rectangle.width);
    //      rectangle.height = (int) Math.round(factor * rectangle.height);
    //    }
    //    int maxWidth = croppedInfo.getAvailableSizes().get(imageIndex).width;
    //    int maxHeight = croppedInfo.getAvailableSizes().get(imageIndex).height;
    //    if (rectangle.x + rectangle.width > maxWidth) {
    //      rectangle.width = maxWidth - rectangle.x;
    //    }
    //    if (rectangle.y + rectangle.height > maxHeight) {
    //      rectangle.height = maxHeight - rectangle.y;
    //    }
  }

  /**
   * The incoming cropping request always targets a specific resolution (i.e. downscaled if
   * targetIndex > 0). However, TurobJPEG requires the cropping region to target the source
   * resolution. Thus, we need to upscale the region passed by the user if the index != 0
   *
   * @param targetIndex Index of the targeted image resolution
   * @param sourceRegion Region relative to the targeted image resolution, will be modified
   */
  private void scaleRegion(int targetIndex, Rectangle sourceRegion) {
    if (targetIndex == 0) {
      return;
    }
    int nativeWidth = getWidth(0);
    int nativeHeight = getHeight(0);
    double scaleFactor = (double) nativeWidth / (double) getWidth(targetIndex);
    sourceRegion.x = (int) Math.ceil(scaleFactor * sourceRegion.x);
    sourceRegion.y = (int) Math.ceil(scaleFactor * sourceRegion.y);
    sourceRegion.width =
        Math.min((int) Math.ceil(scaleFactor * sourceRegion.width), nativeWidth - sourceRegion.x);
    sourceRegion.height =
        Math.min((int) Math.ceil(scaleFactor * sourceRegion.height), nativeHeight - sourceRegion.y);
  }

  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    checkIndex(imageIndex);
    ByteBuffer data = this.data;
    try {
      int rotation = 0;
      Rectangle region = null;
      Rectangle extraCrop = null;
      if (param instanceof JxlImageReadParam) {
        rotation = ((JxlImageReadParam) param).getRotationDegree();
      }
      if (param != null && param.getSourceRegion() != null) {
        region = param.getSourceRegion();
        if (!isRegionFullImage(imageIndex, region)) {
          scaleRegion(imageIndex, region);
          // adjustments need native image size → imageIndex == 0
          //          extraCrop = adjustRegion(info.getMCUSize(), region, rotation,
          // getDimension(0));
        } else {
          region = null;
        }
      }
      if (region != null
          && (region.x + region.width > getWidth(0) || region.y + region.height > getHeight(0))) {
        throw new IllegalArgumentException(
            String.format(
                "Selected region (%dx%d+%d+%d) exceeds the image boundaries (%dx%d).",
                region.width,
                region.height,
                region.x,
                region.y,
                getWidth(imageIndex),
                getHeight(imageIndex)));
      }
      //      if (region != null || rotation != 0) {
      //        data = lib.transform(data.array(), info, region, rotation);
      //      }
      //      Info transformedInfo = lib.getInfo(data.array());
      BufferedImage img = lib.decode(data.array());
      //      if (extraCrop != null) {
      //        adjustExtraCrop(imageIndex, transformedInfo, extraCrop);
      //        img = img.getSubimage(extraCrop.x, extraCrop.y, extraCrop.width, extraCrop.height);
      //      }
      return img;
    } catch (JxlException e) {
      throw new IOException(e);
    }
  }

  private boolean isRegionFullImage(int imageIndex, Rectangle region) {
    int nativeWidth = getWidth(imageIndex);
    int nativeHeight = getHeight(imageIndex);
    return region.x == 0
        && region.y == 0
        && (region.width == 0 || region.width == nativeWidth)
        && (region.height == 0 || region.height == nativeHeight);
  }

  @Override
  public IIOMetadata getStreamMetadata() {
    return null;
  }

  @Override
  public IIOMetadata getImageMetadata(int imageIndex) {
    return null;
  }
}
