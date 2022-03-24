package com.github.gotson.jnr.jxl.imageio;

import static com.github.gotson.jnr.jxl.imageio.CustomAssertions.assertThat;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

class JpegXlImageReaderTest {

  @Test
  public void testReaderIsRegistered() {
    Supplier<List<ImageReader>> getReaderIter =
        () -> Lists.newArrayList(ImageIO.getImageReadersBySuffix("webp"));
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(JxlImageReader.class);
    getReaderIter = () -> Lists.newArrayList(ImageIO.getImageReadersByMIMEType("image/webp"));
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(JxlImageReader.class);
    getReaderIter = () -> Lists.newArrayList(ImageIO.getImageReadersByFormatName("webp"));
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(JxlImageReader.class);
  }

  private JxlImageReader getReader(String fixtureFile) throws IOException {
    File inFile = new File(ClassLoader.getSystemResource(fixtureFile).getFile());
    ImageInputStream is = ImageIO.createImageInputStream(inFile);
    ImageReader reader = ImageIO.getImageReaders(is).next();
    assertThat(reader).isInstanceOf(JxlImageReader.class);
    reader.setInput(is);
    return (JxlImageReader) reader;
  }

  @Test
  public void testRead() throws IOException {
    BufferedImage img = ((ImageReader) getReader("hills.jxl")).read(0, null);
    ImageIO.write(img, "png", new File("/Users/groebroeck/Downloads/out.png"));
    assertThat(img).hasDimensions(1024, 752);
  }

  @Test
  public void testReadScaled() throws IOException {
    BufferedImage img = getReader("lossless.webp").read(0, null);
    ImageIO.write(img, "png", new File("/Users/groebroeck/Downloads/out.png"));
    assertThat(img).hasDimensions(400, 301);
  }

  @Test
  public void testReadRegionAligned() throws IOException {
    ImageReader reader = getReader("crop_aligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(32, 32, 96, 96));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(96, 96).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRegionUnaligned() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(116, 148, 204, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(204, 172).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadUnalignedScaled() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(87, 111, 152, 129));
    BufferedImage img = reader.read(2, param);
    assertThat(img).hasDimensions(152, 129).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadUnalignedRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned_rot90.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(16, 16, 339, 319));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(319, 339).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(339, 319).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(319, 339).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    img = img.getSubimage(192, 116, 172, 204);

    // Need to copy the image so we can check the image data
    BufferedImage copy =
        new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics g = copy.createGraphics();
    g.drawImage(img, 0, 0, null);
    assertThat(img).hasNoPixelsOfColor(-1);
  }

  @Test
  public void testReadRegionRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(116, 148, 204, 172));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(172, 204).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(204, 172).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(172, 204).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRegionRotatedFullWidth() throws IOException {
    ImageReader reader = getReader("rotated_fullwidth.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, 384, 368));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(1, param);
    assertThat(img).hasDimensions(368, 384).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(1, param);
    assertThat(img).hasDimensions(384, 368).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(1, param);
    assertThat(img).hasDimensions(368, 384).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testCanReuseReader() throws IOException {
    ImageReader reader = getReader("rgb.jpg");
    BufferedImage rgbImg = reader.read(1, null);

    reader.setInput(
        ImageIO.createImageInputStream(
            new File(ClassLoader.getSystemResource("crop_unaligned.jpg").getFile())));
    BufferedImage bwImg = reader.read(1, null);

    assertThat(rgbImg.getRGB(256, 256)).isNotEqualTo(bwImg.getRGB(256, 256));
  }

  @Test
  public void testCropFullWidth() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 192, 521, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(521, 172);
  }

  @Test
  public void testCropFullWidthOffset() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(21, 192, 500, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(500, 172);
  }

  @Test
  public void testCropFullHeight() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(192, 0, 172, 509));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(172, 509);
  }

  @Test
  public void testCropFullHeightOffset() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(192, 9, 172, 500));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(172, 500);
  }

  @Test
  public void testUnalignedCropOnPrimeShaped() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(131, 57, 239, 397));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(239, 397).hasNoPixelsOfColor(-1);
  }

  @Test
  public void testCropFullImageScaled() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, reader.getWidth(2), reader.getHeight(2)));
    BufferedImage img = reader.read(2, param);
    assertThat(img).hasDimensions(reader.getWidth(2), reader.getHeight(2));
  }

  @Test
  public void testReadTinyImage() throws IOException {
    ImageReader reader = getReader("tiny.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, reader.getWidth(0), reader.getHeight(0)));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(1);
    assertThat(img.getHeight()).isEqualTo(1);
  }

  @Test
  public void testReadCMYKDelegatesToDefault() throws IOException {
    File inFile = new File(ClassLoader.getSystemResource("cmyk.jpg").getFile());
    ImageInputStream is = ImageIO.createImageInputStream(inFile);
    ImageReader reader = ImageIO.getImageReaders(is).next();
    assertThat(reader).isNotInstanceOf(JxlImageReader.class);
  }

  @Test
  public void testDoubleFreeCrash() throws IOException {
    ImageReader reader = getReader("thumbnail.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, reader.getWidth(4), reader.getHeight(4)));
    BufferedImage img = reader.read(4, param);
    assertThat(img.getWidth()).isEqualTo(180);
    assertThat(img.getHeight()).isEqualTo(136);
  }

  @Test
  public void testCroppingRequiresReallocation() throws IOException {
    ImageReader reader = getReader("needs_realloc.jpg");
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(1281, 1281, 365, 10));
    BufferedImage img = reader.read(3, param);
    assertThat(img.getWidth()).isEqualTo(365);
    assertThat(img.getHeight()).isEqualTo(10);
  }

  @Test
  void testAdjustMCURegion() {
    JxlImageReader reader = new JxlImageReader(null, null);

    Dimension mcuSize = new Dimension(16, 16);
    Rectangle region = new Rectangle(1185, 327, 309, 36);
    int rotation = 0;
    Dimension imageSize = new Dimension(1500, 2260);

    Rectangle extraCrop = reader.adjustRegion(mcuSize, region, rotation, imageSize);
    Rectangle regionExpected = new Rectangle(1184, 320, 316, 48);
    Rectangle extraCropExpected = new Rectangle(1, 7, 309, 36);

    assertThat(region).isEqualTo(regionExpected);
    assertThat(extraCrop).isEqualTo(extraCropExpected);
  }

  @Test
  public void testReadGrayscale() throws IOException {
    ImageReader reader = getReader("grayscale.jpg");
    assertThat(reader.getRawImageType(0).getNumComponents()).isEqualTo(1);
    JxlImageReadParam param = (JxlImageReadParam) reader.getDefaultReadParam();
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(1955, 524);
    assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);
    InputStream input = ClassLoader.getSystemResourceAsStream("grayscale_control.png");
    assertThat(input).isNotNull();
    BufferedImage controlImg = ImageIO.read(input);
    assertThat(img).isEqualTo(controlImg);
  }
}
