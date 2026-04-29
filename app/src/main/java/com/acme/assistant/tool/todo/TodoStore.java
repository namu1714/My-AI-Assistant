package com.acme.assistant.tool.todo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TodoStore {

    private final Map<Integer, TodoItem> items = new LinkedHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public TodoItem add(String title) {
        int id = idGenerator.getAndIncrement();
        TodoItem item = new TodoItem(id, title, false);
        items.put(id, item);
        return item;
    }

    public List<TodoItem> list() {
        return List.copyOf(items.values());
    }

    public Optional<TodoItem> complete(int id) {
        var item = items.get(id);
        if (item == null) {
            return Optional.empty();
        }
        var completed = item.complete();
        items.put(id, completed);
        return Optional.of(completed);
    }

    public boolean delete(int id) {
        return items.remove(id) != null;
    }

    public int size() {
        return items.size();
    }
}
