package com.github.gotson.jnr.jxl.imageio;

import com.github.gotson.jnr.jxl.JpegXl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

@SuppressWarnings("checkstyle:constantname")
public class JxlImageReaderSpi extends ImageReaderSpi {

    private static final Logger LOGGER = LoggerFactory.getLogger(JxlImageReaderSpi.class);
    private static final String vendorName = "NightMonkeys";
    private static final String version = "0.1.0";
    private static final String readerClassName = "com.github.gotson.jnr.jxl.imageio.JxlImageReader";
    private static final String[] names = {"Jpeg XL", "jxl"};
    private static final String[] suffixes = {"jxl"};
    private static final String[] MIMETypes = {"image/jxl"};
    private static final String[] writerSpiNames = null;

    private JpegXl lib;

    /**
     * Construct the SPI. Boilerplate.
     */
    public JxlImageReaderSpi() {
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
                this.lib = new JpegXl();
            } catch (UnsatisfiedLinkError e) {
                LOGGER.warn("Could not load libjxl, plugin will be disabled");
                throw new IOException(e);
            }
        }
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
        byte[] b = new byte[12];
        stream.mark();

        try {
            stream.readFully(b);
            return lib.canDecode(b);
        } finally {
            stream.reset();
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        loadLibrary();
        return new JxlImageReader(this, lib);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Jpeg XL reader plugin based on libjxl.";
    }
}
