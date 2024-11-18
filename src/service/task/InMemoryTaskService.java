package service.task;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import service.history.HistoryService;
import service.task.exceptions.NotFoundException;
import service.task.exceptions.ValidationException;

import java.time.Duration;
import java.util.*;

public class InMemoryTaskService implements TaskService {
    Map<Integer, Task> tasks = new HashMap<>();
    Map<Integer, SubTask> subTasks = new HashMap<>();
    Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryService historyService;
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    int id;

    public InMemoryTaskService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public List<Task> getHistory() {
        return historyService.getHistory();
    }

    @Override
    public Task createTask(Task task) {
        if (doIntervalsOverlap(task)) {
            throw new ValidationException("На временной интервал с " + task.getStartTime() + " до " + task.getStartTime().plusMinutes(task.getDurationInMinutes()) + "уже создана задача");
        }
        task.setId(generateId());
        Task task1 = new Task(task);
        tasks.put(task.getId(), task1);
        prioritizedTasks.add(task1);
        return task1.clone();
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epic.setSubTaskIds(new ArrayList<>());
        epic.setStatus(Status.NEW);
        Epic epic1 = new Epic(epic);
        epics.put(epic.getId(), epic1);
        return epic1.clone();
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (Objects.isNull(epic)) {
            throw new NotFoundException("Невозможно привязать сабтаск к несуществующему эпику с id = " + subTask.getEpicId());
        }
        if (doIntervalsOverlap(subTask)) {
            throw new ValidationException("На временной интервал с " + subTask.getStartTime() + " до " + subTask.getStartTime().plusMinutes(subTask.getDurationInMinutes()) + "уже создана задача");
        }
        subTask.setId(generateId());
        SubTask subTask1 = new SubTask(subTask);
        epic.addSubTaskId(subTask1.getId());
        subTasks.put(subTask.getId(), subTask1);
        prioritizedTasks.add(subTask1);
        recalculateEpicFields(epic.getId());
        return subTask1.clone();
    }

    @Override
    public Task updateTask(Task task) {
        Task saved = getTask(task.getId());
        if (Objects.isNull(saved)) {
            throw new NotFoundException("Не найден таск с id = " + task.getId());
        }
        if (doIntervalsOverlap(task)) {
            throw new ValidationException("На временной интервал с " + task.getStartTime() + " до " + task.getStartTime().plusMinutes(task.getDurationInMinutes()) + "уже создана задача");
        }
        saved.setDescription(task.getDescription());
        saved.setStatus(task.getStatus());
        saved.setTitle(task.getTitle());
        tasks.put(saved.getId(), saved);
        prioritizedTasks.removeIf(t -> Objects.equals(t.getId(), saved.getId()));
        prioritizedTasks.add(saved);
        return saved.clone();
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic saved = getEpic(epic.getId());
        if (Objects.isNull(saved)) {
            throw new NotFoundException("Не найден эпик с id = " + epic.getId());
        }
        saved.setTitle(epic.getTitle());
        saved.setDescription(epic.getDescription());
        epics.put(saved.getId(), saved);
        return saved.clone();
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        SubTask saved = getSubTask(subTask.getId());
        if (Objects.isNull(saved)) {
            throw new NotFoundException("Не найден сабтаск с id = " + subTask.getId());
        }
        Epic epic = getEpic(subTask.getEpicId());
        if (Objects.isNull(epic)) {
            throw new NotFoundException("Невозможно привязать сабтаск к несуществующему эпику с id = " + subTask.getEpicId());
        }
        if (doIntervalsOverlap(subTask)) {
            throw new ValidationException("На временной интервал с " + subTask.getStartTime() + " до " + subTask.getStartTime().plusMinutes(subTask.getDurationInMinutes()) + "уже создана задача");
        }
        saved.setDescription(subTask.getDescription());
        saved.setTitle(subTask.getTitle());
        if (saved.getStatus().equals(subTask.getStatus())) {
            if (saved.getEpicId() != subTask.getEpicId()) {
                int oldEpicId = saved.getEpicId();
                saved.setEpicId(subTask.getEpicId());
                subTasks.put(saved.getId(), saved);
                recalculateEpicFields(oldEpicId);
                recalculateEpicFields(saved.getEpicId());
            }
        } else {
            saved.setStatus(subTask.getStatus());
            if (saved.getEpicId() != subTask.getEpicId()) {
                int oldEpicId = saved.getEpicId();
                saved.setEpicId(subTask.getEpicId());
                getEpic(oldEpicId).getSubTaskIds().remove(saved.getId());
                getEpic(saved.getEpicId()).getSubTaskIds().add(saved.getId());
                recalculateEpicFields(oldEpicId);
                recalculateEpicFields(saved.getEpicId());
            }
            subTasks.put(saved.getId(), saved);
            recalculateEpicFields(saved.getEpicId());
        }
        return saved.clone();
    }

    @Override
    public Task getTask(int id) {
        Task immutableTask = tasks.get(id);
        if (Objects.isNull(immutableTask)) {
            throw new NotFoundException("Таск с id = " + id + " не найден");
        }
        historyService.add(immutableTask.clone());
        return new Task(immutableTask.clone());
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask immutableSubTask = subTasks.get(id);
        if (Objects.isNull(immutableSubTask)) {
            throw new NotFoundException("Сабтаск с id = " + id + " не найден");
        }
        historyService.add(immutableSubTask.clone());
        return new SubTask(immutableSubTask.clone());
    }

    @Override
    public Epic getEpic(int id) {
        Epic immutableEpic = epics.get(id);
        if (Objects.isNull(immutableEpic)) {
            throw new NotFoundException("Эпик с id = " + id + " не найден");
        }
        historyService.add(immutableEpic.clone());
        return new Epic(immutableEpic.clone());
    }

    @Override
    public List<SubTask> getSubTaskList() {
        return subTasks.values().stream().toList();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>((prioritizedTasks));
    }

    @Override
    public List<Epic> getEpicList() {
        return epics.values().stream().toList();
    }

    @Override
    public List<Task> getTaskList() {
        return tasks.values().stream().toList();
    }

    @Override
    public List<SubTask> getSubTasksForEpic(Epic epic) {
        if (Objects.isNull(epic)) return null;
        if (epic.getSubTaskIds().isEmpty()) return Collections.emptyList();
        return getSubTasksByIds(epic.getSubTaskIds());
    }

    @Override
    public boolean dropTask(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            prioritizedTasks.removeIf(task -> task.getId() == taskId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean dropSubTask(int subTaskId) {
        if (subTasks.containsKey(subTaskId)) {
            SubTask subTask = getSubTask(subTaskId);
            Epic epic = getEpic(subTask.getEpicId());
            epic.removeSubTask(subTask.getId());
            prioritizedTasks.removeIf(task -> Objects.equals(task.getId(), subTask.getId()));
            recalculateEpicFields(epic.getId());
            subTasks.remove(subTaskId);
            return true;
        } else {
            return false;
        }
    }

    @Override
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

    private int generateId() {
        return ++id;
    }

    private void recalculateEpicFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        List<SubTask> subTaskList = getSubTasksByIds(epic.getSubTaskIds());
        Optional<SubTask> minStartTimeSubTask = subTaskList
                .stream()
                .min(Comparator.comparing(SubTask::getStartTime));
        Optional<SubTask> maxEndTime = subTaskList
                .stream()
                .max(Comparator.comparing(SubTask::getStartTime));
        epic.setStartTime(minStartTimeSubTask.orElseThrow().getStartTime());
        epic.setEndTime(maxEndTime.orElseThrow().getEndTime());
        epic.setDurationInMinutes((int) Duration.between(minStartTimeSubTask.get().getStartTime(), maxEndTime.get().getEndTime()).toMinutes());
        if (subTaskList.size() == getSubTaskCountByStatus(subTaskList, Status.NEW)) {
            epic.setStatus(Status.NEW);
            epics.replace(epic.getId(), epic);
            return;
        }
        if (subTaskList.size() == getSubTaskCountByStatus(subTaskList, Status.DONE)) {
            epic.setStatus(Status.DONE);
            epics.replace(epic.getId(), epic);
            return;
        }
        epic.setStatus(Status.IN_WORK);
        epics.replace(epic.getId(), epic);
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
        return subTaskList
                .stream()
                .filter(subTask -> subTask.getStatus().equals(status))
                .count();
    }

    private boolean doIntervalsOverlap(Task task) {
        for (Task t : prioritizedTasks) {
            if (Objects.equals(t.getId(), task.getId())) {
                continue;
            }
            if (t.getStartTime().isBefore(task.getStartTime().plusMinutes(task.getDurationInMinutes())) && task.getStartTime().isBefore(t.getEndTime())) {
                return true;
            }
        }
        return false;
    }
}
