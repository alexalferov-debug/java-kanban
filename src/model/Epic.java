package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subTaskIds = new ArrayList<>();

    public Epic(Epic epic) {
        super(epic);
        subTaskIds = epic.subTaskIds;
    }

    public Epic(String title, String description, Status status) {
        super(title, description, status);
    }

    public List<Integer> getSubTaskIds() {
        return new ArrayList<>(subTaskIds);
    }

    public void setSubTaskIds(List<Integer> subTaskIds) {
        this.subTaskIds = subTaskIds;
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void removeSubTask(int subTaskId) {
        subTaskIds.removeIf(id -> id == subTaskId);
    }

    @Override
    public String toString() {
        return "Epic{\n" +
                "id:" + super.getId() + ";\n" +
                "title: " + super.getTitle() + ";\n" +
                "description: " + super.getDescription() + ";\n" +
                "status: " + super.getStatus() + ";\n" +
                "subTaskIds: " + subTaskIds +
                "\n}";
    }
}
