package com.acme.assistant.tool;

import java.util.Map;

public interface Tool {

    String name();

    String description();

    Map<String, Object> parameterSchema();

    String execute(String arguments) throws Exception;
}
