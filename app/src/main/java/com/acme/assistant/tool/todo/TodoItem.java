package com.acme.assistant.tool.implementation.todo;

public record TodoItem(int id, String title, boolean completed) {

    public TodoItem complete() {
        return new TodoItem(id, title, true);
    }

    @Override
    public String toString() {
        String status = completed ? "[x]" : "[ ]";
        return status + "#" + id + " " + title;
    }
}
