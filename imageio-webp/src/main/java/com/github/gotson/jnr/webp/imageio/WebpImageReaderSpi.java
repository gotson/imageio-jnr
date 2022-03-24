package com.github.gotson.jnr.webp.imageio;

import com.github.gotson.jnr.webp.Webp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

@SuppressWarnings("checkstyle:constantname")
public class WebpImageReaderSpi extends ImageReaderSpi {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebpImageReaderSpi.class);
    private static final byte[] RIFF = new byte[] {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = new byte[] {'W', 'E', 'B', 'P'};
    private static final byte[] VP8_ = new byte[] {'V', 'P', '8', ' '};
    private static final byte[] VP8L = new byte[] {'V', 'P', '8', 'L'};
    private static final byte[] VP8X = new byte[] {'V', 'P', '8', 'X'};
    private static final String vendorName = "NightMonkeys";
    private static final String version = "0.1.0";
    private static final String readerClassName = "com.github.gotson.jnr.webp.imageio.WebpImageReader";
    private static final String[] names = {"Webp", "webp"};
    private static final String[] suffixes = {"webp"};
    private static final String[] MIMETypes = {"image/webp"};
    private static final String[] writerSpiNames = null;

    private Webp lib;

    /**
     * Construct the SPI. Boilerplate.
     */
    public WebpImageReaderSpi() {
        super(
            vendorName,
            version,
            names,
            suffixes,
            MIMETypes,
            readerClassName,
            new Class[] {ImageInputStream.class},
            writerSpiNames,
            false,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            null);
    }

    private void loadLibrary() throws IOException {
        if (this.lib == null) {
            try {
                this.lib = new Webp();
            } catch (UnsatisfiedLinkError e) {
                LOGGER.warn("Could not load libwebp, plugin will be disabled");
                throw new IOException(e);
            }
        }
    }

    /**
     * Instruct registry to prioritize this ReaderSpi over other WebP readers. *
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onRegistration(final ServiceRegistry registry, final Class<?> category) {
        Stream.of("com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi")
            .forEach(
                (clsName) -> {
                    try {
                        ImageReaderSpi defaultProvider =
                            (ImageReaderSpi) registry.getServiceProviderByClass(Class.forName(clsName));
                        if (defaultProvider != null) {
                            registry.setOrdering((Class<ImageReaderSpi>) category, this, defaultProvider);
                        }
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
        ImageInputStream stream = (ImageInputStream) input;
        byte[] b = new byte[4];
        ByteOrder oldByteOrder = stream.getByteOrder();
        stream.mark();
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        try {
            stream.readFully(b);
            if (!Arrays.equals(b, RIFF)) {
                return false;
            }
            long chunkLength = stream.readUnsignedInt();
            long streamLength = stream.length();
            if (streamLength != -1 && streamLength != chunkLength + 8) {
                return false;
            }
            stream.readFully(b);
            if (!Arrays.equals(b, WEBP)) {
                return false;
            }

            stream.readFully(b);
            if (!Arrays.equals(b, VP8_) && !Arrays.equals(b, VP8L) && !Arrays.equals(b, VP8X)) {
                return false;
            }
        } finally {
            stream.setByteOrder(oldByteOrder);
            stream.reset();
        }

        return true;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        loadLibrary();
        return new WebpImageReader(this, lib);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Webp reader plugin based on libwebp.";
    }
}
