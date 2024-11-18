package model;

import com.google.gson.annotations.JsonAdapter;
import model.adapters.LocalDateTimeAdapter;
import service.task.exceptions.ValidationException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Cloneable, Serializable {
    private Integer id;
    private String title;
    private String description;
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime startTime;
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime endTime;
    private int durationInMinutes;
    private Status status;
    private static final int defaultDuration = 15;

    public Task(Task task) {
        id = task.id;
        description = task.description;
        title = task.title;
        status = returnStatus(task.status);
        this.startTime = Objects.isNull(task.startTime) ? LocalDateTime.now() : task.startTime;
        this.durationInMinutes = (task.durationInMinutes <= 0) ? defaultDuration : task.durationInMinutes;
        this.endTime = returnEndTime(startTime, this.durationInMinutes);
        this.endTime = returnEndTime(this.startTime, this.durationInMinutes);
    }

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.startTime = LocalDateTime.now();
        this.durationInMinutes = defaultDuration;
        this.endTime = returnEndTime(startTime, this.durationInMinutes);
        this.status = returnStatus(status);
    }

    public Task(String title, String description, Status status, LocalDateTime startTime, int durationInMinutes) {
        this.title = title;
        this.description = description;
        this.startTime = Objects.isNull(startTime) ? LocalDateTime.now() : startTime;
        this.durationInMinutes = (durationInMinutes <= 0) ? defaultDuration : durationInMinutes;
        this.endTime = returnEndTime(startTime, this.durationInMinutes);
        this.status = returnStatus(status);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task task)) return false;
        return (Objects.equals(id, task.id));
    }

    public void check() {
        if (Objects.isNull(title) || title.isEmpty() || Objects.isNull(description) || description.isEmpty()) {
            throw new ValidationException("Не удалось распарсить объект из переданного запроса");
        }
    }

    @Override
    public String toString() {
        return "Task{\n" +
                "id:" + id + ";\n" +
                "title: " + title + ";\n" +
                "description: " + description + ";\n" +
                "startTime: " + startTime + ";\n" +
                "endTime: " + endTime + ";\n" +
                "duration: " + durationInMinutes + ";\n" +
                "status: " + status +
                "\n}";
    }

    public Integer getId() {
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

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;

    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    @Override
    public Task clone() {
        try {
            return (Task) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private LocalDateTime returnEndTime(LocalDateTime startTime, int durationInMinutes) {
        return startTime.plusMinutes(durationInMinutes);
    }

    private Status returnStatus(Status status) {
        return Objects.isNull(status) ? Status.NEW : status;
    }
}
