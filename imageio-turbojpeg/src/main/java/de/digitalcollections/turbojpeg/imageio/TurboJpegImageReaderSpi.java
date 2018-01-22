package de.digitalcollections.turbojpeg.imageio;

import de.digitalcollections.turbojpeg.TurboJpeg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public class TurboJpegImageReaderSpi extends ImageReaderSpi {
  private static final Logger LOGGER = LoggerFactory.getLogger(TurboJpegImageReaderSpi.class);
  private static byte[] HEADER_MAGIC = new byte[]{(byte) 0xff, (byte) 0xd8};
  private static final String vendorName = "Münchener Digitalisierungszentrum/Digitale Bibliothek, Bayerische Staatsbibliothek";
  private static final String version = "0.1.0";
  private static final String readerClassName = "de.digitalcollections.openjpeg.turbojpeg.TurboJpegImageReader";
  private static final String[] names = { "JPEG", "jpeg", "JPG", "jpg" };
  private static final String[] suffixes = { "jpg", "jpeg" };
  private static final String[] MIMETypes = { "image/jpeg" };
  private static final String[] writerSpiNames = { "de.digitalcollections.turbojpeg.imageio.TurboJpegImageWriterSpi" };

  private TurboJpeg lib;

  public TurboJpegImageReaderSpi() {
    super(vendorName, version, names, suffixes, MIMETypes, readerClassName,
          new Class[] { ImageInputStream.class }, writerSpiNames,
        false, null, null,
        null, null,  false,
        null, null, null,
        null);
  }

  private void loadLibrary() throws IOException {
    if (this.lib == null) {
      try {
        this.lib = new TurboJpeg();
      } catch (UnsatisfiedLinkError e) {
        LOGGER.error("Could not load libturbojpeg", e);
        throw new IOException(e);
      }
    }
  }

  /** Instruct registry to prioritize this ReaderSpi over other JPEG readers. **/
  @SuppressWarnings("unchecked")
  @Override
  public void onRegistration(final ServiceRegistry registry, final Class<?> category) {
    Stream.of(
        "com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi",
        "com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi").forEach((clsName) -> {
      try {
        ImageReaderSpi defaultProvider = (ImageReaderSpi) registry.getServiceProviderByClass(Class.forName(clsName));
        registry.setOrdering((Class<ImageReaderSpi>) category, this, defaultProvider);
      } catch (ClassNotFoundException e) {
        // NOP
      }
    });
  }


  @Override
  public boolean canDecodeInput(Object input) throws IOException {
    loadLibrary();
    if (!(input instanceof ImageInputStream)) {
      input = ImageIO.createImageInputStream(input);
    }
    if (input == null) {
      return false;
    }
    ImageInputStream stream = (ImageInputStream)input;
    byte[] b = new byte[2];
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
    loadLibrary();
    return new TurboJpegImageReader(this, lib);
  }

  @Override
  public String getDescription(Locale locale) {
    return "JPEG reader plugin based on libjpeg-turbo/turbojpeg.";
  }
}
