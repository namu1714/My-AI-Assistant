package com.acme.assistant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ContentPart.TextPart.class, name = "text"),
        @JsonSubTypes.Type(value = ContentPart.ImagePart.class, name = "image_url")
})
public sealed interface ContentPart permits ContentPart.TextPart, ContentPart.ImagePart {

    @JsonProperty("type")
    String type();

    record TextPart(String text) implements ContentPart {
        @Override
        public String type() {
            return "text";
        }
    }

    record ImageUrl(String url, String detail) {
        public ImageUrl(String url) {
            this(url, null);
        }
    }

    record ImagePart(ImageUrl imageUrl) implements ContentPart {
        @Override
        public String type() {
            return "image_url";
        }

        public static ImagePart ofUrl(String url) {
            return new ImagePart(new ImageUrl(url));
        }

        public static ImagePart ofUrl(String url, String detail) {
            return new ImagePart(new ImageUrl(url, detail));
        }
    }
}
