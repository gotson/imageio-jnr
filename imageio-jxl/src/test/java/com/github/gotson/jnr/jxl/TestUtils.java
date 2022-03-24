package com.github.gotson.jnr.jxl;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TestUtils {
    public static byte[] getRessourceAsByteArray(String fixtureFile) throws IOException {
        return FileUtils.readFileToByteArray(new File(ClassLoader.getSystemResource(fixtureFile).getFile()));
    }

    public static InputStream getResourceAsInputStream(String fixtureFile) {
        return ClassLoader.getSystemResourceAsStream(fixtureFile);
    }

    public static Stream<Arguments> provideAllFixtureFiles() throws IOException {
        Path resources = Paths.get("src", "test", "resources");
        return Files.list(resources).map(x -> Arguments.of(x.getFileName().toString()));
    }
}
