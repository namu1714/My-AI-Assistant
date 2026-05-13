package com.acme.assistant.agent;

import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.client.LlmClient;
import com.acme.assistant.memory.ConversationMemory;
import com.acme.assistant.memory.MessageWindowMemory;
import com.acme.assistant.tool.ToolRegistry;

import java.util.Objects;

public class DefaultAgent implements Agent {

    private final AgentMetadata metadata;
    private final AgentContent content;

    private DefaultAgent(AgentMetadata metadata, AgentContent content) {
        this.metadata = metadata;
        this.content = content;
    }

    @Override
    public AgentMetadata metadata() {
        return metadata;
    }

    @Override
    public AgentContent content() {
        return content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private String version = "1.0.0";
        private LlmClient llmClient;
        private LlmModel llmModel;
        private ToolRegistry toolRegistry;
        private ConversationMemory memory;
        private String systemPrompt;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder llmClient(LlmClient llmClient) {
            this.llmClient = llmClient;
            return this;
        }

        public Builder llmModel(LlmModel llmModel) {
            this.llmModel = llmModel;
            return this;
        }

        public Builder toolRegistry(ToolRegistry toolRegistry) {
            this.toolRegistry = toolRegistry;
            return this;
        }

        public Builder memory(ConversationMemory memory) {
            this.memory = memory;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public DefaultAgent build() {
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(llmClient, "llmClient is required");
            Objects.requireNonNull(llmModel, "llmModel is required");

            if (toolRegistry == null) {
                toolRegistry = new ToolRegistry();
            }
            if (memory == null) {
                memory = new MessageWindowMemory(100);
            }

            var metadata = new AgentMetadata(name, description, version);
            var content = new AgentContent(llmClient, llmModel, toolRegistry, memory, systemPrompt);

            return new DefaultAgent(metadata, content);
        }
    }
}
