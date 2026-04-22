package com.acme.assistant.prompt;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.util.Map;

public class PromptTemplate {

    private final Template template;
    private final String source;

    public PromptTemplate(String templateSource) {
        this.source = templateSource;
        this.template = Mustache.compiler().compile(templateSource);
    }

    public String render(Map<String, Object> variables) {
        return template.execute(variables);
    }

    public String source() {
        return source;
    }
}
