package model.dto;

import com.google.gson.annotations.JsonAdapter;
import model.Epic;
import model.Status;
import model.SubTask;
import model.adapters.LocalDateTimeAdapter;

import java.time.LocalDateTime;

public class SubTaskWithoutEpicDTO {
    private Integer id;
    private String title;
    private String description;
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime startTime;
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime endTime;
    private int durationInMinutes;
    private Status status;
    Epic epic;

    private SubTaskWithoutEpicDTO() {
    }

    public static SubTaskWithoutEpicDTO returnSubTaskDto(SubTask subTask) {
        SubTaskWithoutEpicDTO subTaskDTO = new SubTaskWithoutEpicDTO();
        subTaskDTO.id = subTask.getId();
        subTaskDTO.title = subTask.getTitle();
        subTaskDTO.description = subTask.getDescription();
        subTaskDTO.status = subTask.getStatus();
        subTaskDTO.startTime = subTask.getStartTime();
        subTaskDTO.endTime = subTask.getEndTime();
        subTaskDTO.durationInMinutes = subTask.getDurationInMinutes();
        return subTaskDTO;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public Epic getEpic() {
        return epic;
    }

    public Integer getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }
}
