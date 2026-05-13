package com.acme.assistant.agent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AgentRegistry {

    private final Map<String, Agent> agents = new ConcurrentHashMap<>();

    public void register(Agent agent) {
        String name = agent.metadata().name();
        if (agents.containsKey(name)) {
            throw new IllegalArgumentException("Agent already registered: " + name);
        }
        agents.put(name, agent);
    }

    public Optional<Agent> getAgent(String name) {
        return Optional.ofNullable(agents.get(name));
    }

    public List<Agent> listAgents() {
        return List.copyOf(agents.values());
    }

    public int size() {
        return agents.size();
    }
}
