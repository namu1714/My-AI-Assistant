package com.acme.assistant.model;

public record ResponseFormat(String type) {

    public static ResponseFormat jsonObject() {
        return new ResponseFormat("json_object");
    }

    public static ResponseFormat text() {
        return new ResponseFormat("text");
    }
}
