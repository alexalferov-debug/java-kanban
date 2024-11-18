package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SubTask extends Task implements Cloneable, Serializable {
    Integer epicId;

    public SubTask(SubTask subTask) {
        super(subTask);
        epicId = subTask.epicId;
    }

    public SubTask(String title, String description, Status status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public SubTask(String title, String description, Status status, int epicId, LocalDateTime startTime, int durationInMinutes) {
        super(title, description, status, startTime, durationInMinutes);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "SubTask{\n" +
                "id:" + super.getId() + ";\n" +
                "title: " + super.getTitle() + ";\n" +
                "description: " + super.getDescription() + ";\n" +
                "startTime: " + super.getStartTime() + ";\n" +
                "endTime: " + super.getEndTime() + ";\n" +
                "duration: " + super.getDurationInMinutes() + ";\n" +
                "status: " + super.getStatus() + ";\n" +
                "epic.id: " + epicId +
                "\n}";
    }

    @Override
    public SubTask clone() {
        SubTask subTask = (SubTask) super.clone();
        subTask.setStartTime(this.getStartTime());
        subTask.setEndTime(this.getEndTime());
        return subTask;
    }
}
