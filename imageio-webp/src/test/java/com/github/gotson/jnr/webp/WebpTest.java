package com.github.gotson.jnr.webp;

import com.github.gotson.jnr.webp.Info;
import com.github.gotson.jnr.webp.Webp;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class WebpTest {
    private final Webp webp = new Webp();

    byte[] getRessourceAsByteArray(String fixtureFile) throws IOException {
        return FileUtils.readFileToByteArray(new File (ClassLoader.getSystemResource(fixtureFile).getFile()));
    }

    @Test
    public void testInfoIsCorrectForLossy() throws Exception {
        Info features = webp.getFeatures(getRessourceAsByteArray("lossy.webp"));

        assertThat(features.getWidth()).isEqualTo(1024);
        assertThat(features.getHeight()).isEqualTo(752);
        assertThat(features.hasAlpha()).isFalse();
        assertThat(features.hasAnimation()).isFalse();
        assertThat(features.getFormat()).isEqualTo(Info.Format.LOSSY);
    }

    @Test
    public void testInfoIsCorrectForLossyAlpha() throws Exception {
        Info features = webp.getFeatures(getRessourceAsByteArray("lossy_alpha.webp"));

        assertThat(features.getWidth()).isEqualTo(400);
        assertThat(features.getHeight()).isEqualTo(301);
        assertThat(features.hasAlpha()).isTrue();
        assertThat(features.hasAnimation()).isFalse();
        assertThat(features.getFormat()).isEqualTo(Info.Format.LOSSY);
    }

    @Test
    public void testInfoIsCorrectForLossless() throws Exception {
        Info features = webp.getFeatures(getRessourceAsByteArray("lossless.webp"));

        assertThat(features.getWidth()).isEqualTo(400);
        assertThat(features.getHeight()).isEqualTo(301);
        assertThat(features.hasAlpha()).isTrue();
        assertThat(features.hasAnimation()).isFalse();
        assertThat(features.getFormat()).isEqualTo(Info.Format.LOSSLESS);
    }

    @Test
    public void testInfoIsCorrectForAnimated() throws Exception {
        Info features = webp.getFeatures(getRessourceAsByteArray("animated.webp"));

        assertThat(features.getWidth()).isEqualTo(400);
        assertThat(features.getHeight()).isEqualTo(400);
        assertThat(features.hasAlpha()).isTrue();
        assertThat(features.hasAnimation()).isTrue();
        assertThat(features.getFormat()).isEqualTo(Info.Format.UNDEFINED_OR_MIXED);
    }

    @Test
    public void testInfoIsCorrectForAnimatedLossless() throws Exception {
        Info features = webp.getFeatures(getRessourceAsByteArray("banana.webp"));

        assertThat(features.getWidth()).isEqualTo(990);
        assertThat(features.getHeight()).isEqualTo(1050);
        assertThat(features.hasAlpha()).isTrue();
        assertThat(features.hasAnimation()).isTrue();
        assertThat(features.getFormat()).isEqualTo(Info.Format.UNDEFINED_OR_MIXED);
    }
}