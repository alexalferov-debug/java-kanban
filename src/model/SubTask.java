package model;

public class SubTask extends Task {
    int epicId;

    public SubTask(SubTask subTask) {
        super(subTask);
        epicId = subTask.epicId;
    }

    public SubTask(String title, String description, Status status, int epicId) {
        super(title, description, status);
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
                "status: " + super.getStatus() + ";\n" +
                "epic.id: " + epicId +
                "\n}";
    }
}
