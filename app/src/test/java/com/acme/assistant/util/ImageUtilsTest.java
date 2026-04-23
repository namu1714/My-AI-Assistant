package com.acme.assistant.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ImageUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void PNG_파일을_Data_URI로_변환한다() throws Exception {
        byte[] pngBytes = { // 최소 유효 1x1 png 바이트 배열
                (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,
                (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1F, (byte)0x15, (byte)0xC4, (byte)0x89,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A, (byte)0x49, (byte)0x44, (byte)0x41, (byte)0x54,
                (byte)0x78, (byte)0x9C, (byte)0x63, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00, (byte)0x01,
                (byte)0xE2, (byte)0x27, (byte)0xD4, (byte)0xBF,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44,
                (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
        };

        Path pngFile = tempDir.resolve("test.png");
        Files.write(pngFile, pngBytes);

        String dataUri = ImageUtils.toDataUri(pngFile);

        assertThat(dataUri).startsWith("data:image/png;base64,");
        assertThat(dataUri).contains("iVBOR"); // PNG base64 시작 패턴
    }

    @Test
    void 존재하지_않는_파일에_예외를_던진다() {
        Path noFile = tempDir.resolve("없는파일.png");

        assertThatThrownBy(() -> ImageUtils.toDataUri(noFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("파일이 존재하지 않습니다");
    }

    @Test
    void 지원하지_않는_형식에_예외를_던진다() throws Exception {
        Path textFile = tempDir.resolve("document.txt");
        Files.writeString(textFile, "This is a text file.");

        assertThatThrownBy(() -> ImageUtils.toDataUri(textFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("지원하지 않는 이미지 형식");
    }
}
