package com.acme.assistant.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileReadTool extends AbstractTool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public FileReadTool() {
        super(new ToolDefinition(
                "file_read",
                "지정한 경로의 파일 내용을 읽어 반환한다",
                JsonSchemaBuilder.objectSchema()
                        .property("path", "string", "읽을 파일의 절대 경로")
                        .required("path")
                        .build()
        ));
    }

    @Override
    public String execute(String arguments) throws Exception {
        JsonNode args = MAPPER.readTree(arguments);
        String path = args.get("path").asText();
        return Files.readString(Path.of(path));
    }
}
