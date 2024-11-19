package service.task;

import model.Epic;
import model.SubTask;
import model.Task;
import service.Managers;
import service.task.exceptions.ServiceSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileBackedTaskService extends InMemoryTaskService {
    private final Path pathToFile;

    public FileBackedTaskService(Path pathToFile) {
        super(Managers.getDefaultHistoryManager());
        this.pathToFile = pathToFile;
    }

    @Override
    public Task createTask(Task task) {
        Task task1 = super.createTask(task);
        save();
        return task1;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        SubTask subTask1 = super.createSubTask(subTask);
        save();
        return subTask1;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic epic1 = super.createEpic(epic);
        save();
        return epic1;
    }

    @Override
    public Task updateTask(Task task) {
        Task task1 = super.updateTask(task);
        save();
        return task1;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic epic1 = super.updateEpic(epic);
        save();
        return epic1;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        SubTask subTask1 = super.updateSubTask(subTask);
        save();
        return subTask1;
    }

    @Override
    public boolean dropTask(int taskId) {
        boolean result = super.dropTask(taskId);
        save();
        return result;
    }

    @Override
    public boolean dropSubTask(int subTaskId) {
        boolean result = super.dropSubTask(subTaskId);
        save();
        return result;
    }

    @Override
    public boolean dropEpic(int epicId) {
        boolean result = super.dropEpic(epicId);
        save();
        return result;
    }

    public static FileBackedTaskService loadFromFile(Path pathToFile) {
        FileBackedTaskService service = new FileBackedTaskService(pathToFile);
        service.loadFromFile();
        return service;
    }

    private void save() {
        try {
            Files.createDirectories(pathToFile.getParent());
        } catch (IOException e) {
            throw new ServiceSaveException("Не удалось создать директорию для файла " + pathToFile, e);
        }
        if (Files.notExists(pathToFile)) {
            try {
                Files.createFile(pathToFile);
            } catch (IOException e) {
                throw new ServiceSaveException("Не удалось создать файл " + pathToFile + " для сохранения тасков", e);
            }
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(pathToFile.toFile()))) {
            for (Map.Entry<Integer, Task> entry : tasks.entrySet()) {
                out.writeObject(entry.getValue());
            }
            for (Map.Entry<Integer, Epic> entry : epics.entrySet()) {
                out.writeObject(entry.getValue());
            }
            for (Map.Entry<Integer, SubTask> entry : subTasks.entrySet()) {
                out.writeObject(entry.getValue());
            }
        } catch (IOException e) {
            throw new ServiceSaveException("Не удалось записать данные в файл " + pathToFile, e);
        }
    }

    void loadFromFile() {
        int maxId = 0;
        if (Files.notExists(pathToFile)) {
            throw new ServiceSaveException("Файл " + pathToFile + " не существует");
        }
        try {
            if (Files.size(pathToFile) == 0) {
                return;
            }
        } catch (IOException e) {
            throw new ServiceSaveException("Не удалось прочитать содержимое файла " + pathToFile);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(pathToFile.toFile()))) {
            while (true) {
                try {
                    Object loadedObject = in.readObject();
                    if (loadedObject instanceof Epic curEpic) {
                        if (curEpic.getId() > maxId) {
                            maxId = curEpic.getId();
                        }
                        epics.put(curEpic.getId(), curEpic);
                    } else if (loadedObject instanceof SubTask curSubtask) {
                        if (curSubtask.getId() > maxId) {
                            maxId = curSubtask.getId();
                        }
                        subTasks.put(curSubtask.getId(), curSubtask);
                    } else {
                        Task curTask = (Task) loadedObject;
                        if (curTask.getId() > maxId) {
                            maxId = curTask.getId();
                        }
                        tasks.put(curTask.getId(), curTask);
                    }
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ServiceSaveException("не удалось загрузить список задач", e);
        }
        id = maxId;
    }

}
