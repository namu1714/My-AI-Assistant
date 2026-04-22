package com.acme.assistant.prompt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PromptManager {

    private final String basePath;
    private final Map<String, PromptTemplate> cache = new ConcurrentHashMap<>();

    public PromptManager() {
        this("prompts/");
    }

    public PromptManager(String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
    }

    public PromptTemplate getTemplate(String name) {
        return cache.computeIfAbsent(name, this::loadTemplate);
    }

    public String render(String name, Map<String, Object> variables) {
        return getTemplate(name).render(variables);
    }

    private PromptTemplate loadTemplate(String name) {
        String resourcePath = basePath + name + ".mustache";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + resourcePath);
            }
            String source = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new PromptTemplate(source);
        } catch (IOException e) {
            throw new RuntimeException("템플릿 로드 실패: " + resourcePath, e);
        }
    }
}
