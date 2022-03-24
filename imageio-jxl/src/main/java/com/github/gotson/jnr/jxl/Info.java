package com.github.gotson.jnr.jxl;

public class Info {
    private final int width;
    private final int height;
    private final boolean hasAlpha;
    private final boolean hasAnimation;
    private final Format format;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasAlpha() {
        return hasAlpha;
    }

    public boolean hasAnimation() {
        return hasAnimation;
    }

    public Format getFormat() {
        return format;
    }

    private static int getScaled(int dim, int num, int denom) {
        return (dim * num + denom - 1) / denom;
    }

    /**
     * Create a new instance with the information parsed from the JPEG image.
     */
    public Info(int width, int height, boolean hasAlpha, boolean hasAnimation, Format format) {
        this.width = width;
        this.height = height;
        this.hasAlpha = hasAlpha;
        this.hasAnimation = hasAnimation;
        this.format = format;
    }

    enum Format {
        UNDEFINED_OR_MIXED,
        LOSSY,
        LOSSLESS
    }
}
