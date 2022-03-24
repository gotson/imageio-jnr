package com.github.gotson.jnr.jxl;

import com.github.gotson.jnr.jxl.lib.libjxl;
import com.github.gotson.jnr.jxl.lib.structs.JxlBasicInfo;
import com.github.gotson.jnr.jxl.lib.enums.JxlSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpegXlTest {
    private final JpegXl jpegXl = new JpegXl();

    @ParameterizedTest
    @MethodSource("testVersionParsingProvider")
    public void testVersionParsing(int version, String expected) {
        jpegXl.lib = mock(libjxl.class);

        when(jpegXl.lib.JxlDecoderVersion()).thenReturn(version);

        String actual = jpegXl.getLibVersion();

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> testVersionParsingProvider() {
        return Stream.of(
            Arguments.of(1002003, "1.2.3"),
            Arguments.of(6001, "0.6.1"),
            Arguments.of(5, "0.0.5")
        );
    }

    @ParameterizedTest
    @MethodSource("testSignatureCheckProvider")
    public void testSignatureCheck(String fixtureFile, JxlSignature expected) throws Exception {
        byte[] buf = TestUtils.getRessourceAsByteArray(fixtureFile);
        JxlSignature signature = jpegXl.lib.JxlSignatureCheck(ByteBuffer.wrap(buf), buf.length);

        assertThat(signature).isEqualTo(expected);
    }

    private static Stream<Arguments> testSignatureCheckProvider() {
        return Stream.of(
            Arguments.of("animation.jxl", JxlSignature.JXL_SIG_CODESTREAM),
            Arguments.of("container.jxl", JxlSignature.JXL_SIG_CONTAINER),
            Arguments.of("hills.jxl", JxlSignature.JXL_SIG_CODESTREAM),
            Arguments.of("island.jxl", JxlSignature.JXL_SIG_CODESTREAM)
        );
    }

    @Test
    public void testInfoIsCorrect() throws Exception {
        JxlBasicInfo features = jpegXl.getBasicInfo(TestUtils.getRessourceAsByteArray("animation.jxl"));
        System.out.println("");
//        assertThat(features.getWidth()).isEqualTo(400);
//        assertThat(features.getHeight()).isEqualTo(301);
//        assertThat(features.hasAlpha()).isTrue();
//        assertThat(features.hasAnimation()).isFalse();
//        assertThat(features.getFormat()).isEqualTo(Info.Format.LOSSY);
    }
}