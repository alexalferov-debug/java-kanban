package service.task;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;

public interface TaskService {
    List<Task> getHistory();

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    SubTask createSubTask(SubTask subTask);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    SubTask updateSubTask(SubTask subTask);

    Task getTask(int id);

    List<SubTask> getSubTaskList();

    List<Task> getPrioritizedTasks();

    List<Epic> getEpicList();

    List<Task> getTaskList();

    List<SubTask> getSubTasksForEpic(Epic epic);

    boolean dropTask(int taskId);

    boolean dropSubTask(int subTaskId);

    boolean dropEpic(int epicId);

    SubTask getSubTask(int id);

    Epic getEpic(int id);

}
