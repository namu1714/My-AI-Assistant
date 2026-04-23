package com.acme.assistant.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public final class ImageUtils {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    private static final Map<String, String> EXTENSION_TO_MIME = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "webp", "image/webp"
    );

    private ImageUtils() {
        // Utility class, prevent instantiation
    }

    public static String toDataUri(Path imagePath) throws IOException {
        if (!Files.exists(imagePath)) {
            throw new IOException("파일이 존재하지 않습니다: " + imagePath);
        }

        long fileSize = Files.size(imagePath);
        if (fileSize > MAX_FILE_SIZE) {
            throw new IOException("파일 크기가 20MB를 초과합니다: " + (fileSize / 1024 / 1024) + "MB");
        };

        String mimeType = detectMimeType(imagePath);
        byte[] data = Files.readAllBytes(imagePath);
        String base64 = Base64.getEncoder().encodeToString(data);

        return "data:" + mimeType + ";base64," + base64;
    }

    static String detectMimeType(Path imagePath) throws IOException {
        // Files.probeContentType() 시도
        String mimeType = Files.probeContentType(imagePath);
        if (mimeType != null && mimeType.startsWith("image/")) {
            return mimeType;
        }

        // 확장자 기반 fallback
        String fileName = imagePath.getFileName().toString().toLowerCase();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String ext = fileName.substring(dotIndex + 1);
            String extMime = EXTENSION_TO_MIME.get(ext);
            if (extMime != null) {
                return extMime;
            }
        }
        throw new IOException("지원하지 않는 이미지 형식입니다: " + imagePath);
    }
}
