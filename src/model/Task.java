package model;

import java.util.Objects;

public class Task {
    private int id;
    private String title;
    private String description;
    private Status status;

    public Task(Task task) {
        id = task.id;
        description = task.description;
        title = task.title;
        status = task.status;
    }

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        if (Objects.isNull(status)) {
            this.status = Status.NEW;
        } else {
            this.status = status;
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task task)) return false;
        return (id == task.id);
    }

    @Override
    public String toString() {
        return "Task{\n" +
                "id:" + id + ";\n" +
                "title: " + title + ";\n" +
                "description: " + description + ";\n" +
                "status: " + status +
                "\n}";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
