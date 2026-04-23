package com.acme.assistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentPartTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    void TextPart를_직렬화한다() throws Exception {
        ContentPart part = new ContentPart.TextPart("안녕하세요");

        String json = mapper.writeValueAsString(part);

        assertThat(json).contains("\"type\":\"text\"");
        assertThat(json).contains("\"text\":\"안녕하세요\"");
    }

    @Test
    void ImagePart를_URL로_직렬화한다() throws Exception {
        ContentPart part = ContentPart.ImagePart.ofUrl("http://example.com/photo.jpg");

        String json = mapper.writeValueAsString(part);

        assertThat(json).contains("\"type\":\"image_url\"");
        assertThat(json).contains("\"url\":\"http://example.com/photo.jpg\"");
        assertThat(json).doesNotContain("\"detail\"");
    }

    @Test
    void ImagePart를_detail과_함께_직렬화한다() throws Exception {
        ContentPart part = ContentPart.ImagePart.ofUrl("http://example.com/photo.jpg", "high");

        String json = mapper.writeValueAsString(part);

        assertThat(json).contains("\"type\":\"image_url\"");
        assertThat(json).contains("\"url\":\"http://example.com/photo.jpg\"");
        assertThat(json).contains("\"detail\":\"high\"");
    }
}
