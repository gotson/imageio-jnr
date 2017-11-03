package de.digitalcollections.openjpeg.imageio;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class OpenJp2ImageReaderSpi extends ImageReaderSpi {
  private static byte[] HEADER_MAGIC = new byte[]{0x00, 0x00, 0x00, 0x0c, 0x6a, 0x50,
                                                  0x20, 0x20, 0x0d, 0x0a, (byte) 0x87, 0x0a};
  private static final String vendorName = "Münchener Digitalisierungszentrum/Digitale Bibliothek, Bayerische Staatsbibliothek";
  private static final String version = "0.1.0";
  private static final String readerClassName = "de.digitalcollections.openjpeg.imageio.OpenJp2ImageReader";
  private static final String[] names = { "jpeg2000" };
  private static final String[] suffixes = { "jp2" };
  private static final String[] MIMETypes = { "image/jp2" };
  private static final String[] writerSpiNames = { "de.digitalcollections.openjpeg.imageio.OpenJp2ImageWriterSpi" };
  private static final Class[] inputTypes = { ImageInputStream.class };

  public OpenJp2ImageReaderSpi() {
    super(vendorName, version, names, suffixes, MIMETypes, readerClassName, inputTypes, writerSpiNames,
        false, null, null,
        null, null, false,
        null, null, null,
        null);
  }



  @Override
  public boolean canDecodeInput(Object input) throws IOException {
    if (!(input instanceof ImageInputStream)) {
      input = ImageIO.createImageInputStream(input);
    }
    if (input == null) {
      return false;
    }
    ImageInputStream stream = (ImageInputStream)input;
    byte[] b = new byte[12];
    try {
      stream.mark();
      stream.readFully(b);
    } catch (IOException e) {
      return false;
    }
    return Arrays.equals(b, HEADER_MAGIC);
  }

  @Override
  public ImageReader createReaderInstance(Object extension) throws IOException {
    return new OpenJp2ImageReader(this);
  }

  @Override
  public String getDescription(Locale locale) {
    return "JPEG2000 reader plugin based on the OpenJp2 library from the OpenJPEG project.";
  }
}
