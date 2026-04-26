package com.acme.assistant.tool.implementation;

import com.acme.assistant.tool.ToolContext;
import com.acme.assistant.tool.ToolInput;
import com.acme.assistant.tool.ToolResult;
import com.acme.assistant.tool.web.MockSearchProvider;
import com.acme.assistant.tool.web.WebSearchTool;
import com.acme.assistant.tool.web.UrlValidator;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WebSearchToolTest {

    @Test
    void 유효한_HTTPS_URL을_허용한다() {
        UrlValidator validator = new UrlValidator();

        URI result = validator.validate("https://example.com/page");

        assertThat(result.getHost()).isEqualTo("example.com");
    }

    @Test
    void localhost를_차단한다() {
        UrlValidator validator = new UrlValidator();

        assertThatThrownBy(() ->
                validator.validate("http://localhost:8080/api"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("사설 네트워크 접근 차단");
    }

    @Test
    void FTP_프로토콜을_차단하다() {
        UrlValidator validator = new UrlValidator();

        assertThatThrownBy(() ->
                validator.validate("ftp://files.example.com/data"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("허용되지 않는 프로토콜");
    }

    @Test
    void 검색_결과를_반환한다() {
        WebSearchTool tool = new WebSearchTool(new MockSearchProvider());
        ToolInput input = ToolInput.parse("{\"query\":\"Java\"}");

        ToolResult result = tool.execute(input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("Java 21");
    }
}
