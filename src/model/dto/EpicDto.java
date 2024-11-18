package model.dto;

import com.google.gson.annotations.JsonAdapter;
import model.Epic;
import model.Status;
import model.SubTask;
import model.adapters.LocalDateTimeAdapter;
import service.task.TaskService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class EpicDto {
    private Integer id;
    private String title;
    private String description;
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime startTime;
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime endTime;
    private int durationInMinutes;
    private List<SubTask> subTasks;
    private Status status;

    private EpicDto() {

    }

    public static EpicDto createDTO(Epic epic, List<SubTask> subTasks) {
        EpicDto epicDto = new EpicDto();
        epicDto.id = epic.getId();
        epicDto.title = epic.getTitle();
        epicDto.description = epic.getDescription();
        epicDto.startTime = epic.getStartTime();
        epicDto.endTime = epic.getEndTime();
        epicDto.durationInMinutes = epic.getDurationInMinutes();
        epicDto.subTasks = subTasks;
        epicDto.status = epic.getStatus();
        return epicDto;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getTitle() {
        return title;
    }

    public Integer getId() {
        return id;
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }
}
