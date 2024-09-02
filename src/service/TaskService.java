package service;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.*;

public class TaskService {
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, SubTask> subTasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    int id;

    public Task createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epic.setSubTaskIds(new ArrayList<>());
        epic.setStatus(Status.NEW);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask createSubTask(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (Objects.isNull(epic)) {
            return null;
        }
        subTask.setId(generateId());
        epic.addSubTaskId(subTask.getId());
        subTasks.put(subTask.getId(), subTask);
        recalcEpicStatus(epic.getId());
        return new SubTask(subTask);
    }

    public Task updateTask(Task task) {
        Task saved = getTask(task.getId());
        if (Objects.isNull(saved)) {
            return null;
        }
        saved.setDescription(task.getDescription());
        saved.setStatus(task.getStatus());
        saved.setTitle(task.getTitle());
        tasks.put(saved.getId(), saved);
        return saved;
    }

    public Epic updateEpic(Epic epic) {
        Epic saved = getEpic(epic.getId());
        if (Objects.isNull(saved)) {
            return saved;
        }
        saved.setTitle(epic.getTitle());
        saved.setDescription(epic.getDescription());
        epics.put(saved.getId(), saved);
        return saved;
    }

    public SubTask updateSubTask(SubTask subTask) {
        SubTask saved = getSubTask(subTask.getId());
        if (Objects.isNull(saved)) {
            return null;
        }
        saved.setDescription(subTask.getDescription());
        saved.setTitle(subTask.getTitle());
        if (saved.getStatus().equals(subTask.getStatus())) {
            if (saved.getEpicId() != subTask.getEpicId()) {
                int oldEpicId = saved.getEpicId();
                saved.setEpicId(subTask.getEpicId());
                subTasks.put(saved.getId(), saved);
                recalcEpicStatus(oldEpicId);
                recalcEpicStatus(saved.getEpicId());
            }
        } else {
            saved.setStatus(subTask.getStatus());
            if (saved.getEpicId() != subTask.getEpicId()) {
                int oldEpicId = saved.getEpicId();
                saved.setEpicId(subTask.getEpicId());
                getEpic(oldEpicId).getSubTaskIds().remove(saved.getId());
                getEpic(saved.getEpicId()).getSubTaskIds().add(saved.getId());
                recalcEpicStatus(oldEpicId);
                recalcEpicStatus(saved.getEpicId());
            }
            subTasks.put(saved.getId(), saved);
            recalcEpicStatus(saved.getEpicId());
        }
        return saved;
    }

    public Task getTask(int id) {
        Task immutableTask = tasks.get(id);
        if (Objects.isNull(immutableTask)) return null;
        return new Task(immutableTask);
    }

    public List <SubTask> getSubTaskList() {
        return subTasks.values().stream().toList();
    }

    public List<Epic> getEpicList() {
        return epics.values().stream().toList();
    }

    public List<Task> getTaskList() {
        return tasks.values().stream().toList();
    }

    public List<SubTask> getSubTasksForEpic(Epic epic) {
        if (Objects.isNull(epic)) return null;
        if (epic.getSubTaskIds().isEmpty()) return Collections.emptyList();
        return getSubTasksByIds(epic.getSubTaskIds());
    }

    public boolean dropTask(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            return true;
        } else {
            return false;
        }
    }

    public boolean dropSubTask(int subTaskId) {
        if (subTasks.containsKey(subTaskId)) {
            SubTask subTask = getSubTask(subTaskId);
            Epic epic = getEpic(subTask.getEpicId());
            epic.removeSubTask(subTask.getId());
            recalcEpicStatus(epic.getId());
            subTasks.remove(subTaskId);
            return true;
        } else {
            return false;
        }
    }

    public boolean dropEpic(int epicId) {
        if (epics.containsKey(epicId)) {
            Epic epic = getEpic(epicId);
            if (!epic.getSubTaskIds().isEmpty()) {
                for (int subTaskId : epic.getSubTaskIds()) {
                    dropSubTask(subTaskId);
                }
            }
            epics.remove(epicId);
        }
        return false;
    }

    public SubTask getSubTask(int id) {
        SubTask immutableSubTask = subTasks.get(id);
        if (Objects.isNull(immutableSubTask)) return null;
        return new SubTask(immutableSubTask);
    }

    public Epic getEpic(int id) {
        Epic immutableEpic = epics.get(id);
        if (Objects.isNull(immutableEpic)) return null;
        return new Epic(immutableEpic);
    }

    private int generateId() {
        return ++id;
    }

    private void recalcEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (Objects.isNull(epic)) {
            return;
        }
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        List<SubTask> subTaskList = getSubTasksByIds(epic.getSubTaskIds());
        if (subTaskList.size() == getSubTaskCountByStatus(subTaskList, Status.NEW)) {
            epic.setStatus(Status.NEW);
            return;
        }
        if (subTaskList.size() == getSubTaskCountByStatus(subTaskList, Status.DONE)) {
            epic.setStatus(Status.DONE);
            return;
        }
        epic.setStatus(Status.IN_WORK);
    }

    private List<SubTask> getSubTasksByIds(List<Integer> subTaskIdsList) {
        List<SubTask> subTaskList = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            if (subTaskIdsList.contains(subTask.getId())) {
                subTaskList.add(subTask);
            }
        }
        return subTaskList;
    }

    private long getSubTaskCountByStatus(List<SubTask> subTaskList, Status status) {
        return subTaskList.stream().filter(subTask -> subTask.getStatus().equals(status)).count();
    }
}