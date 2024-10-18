package service;

import service.history.HistoryService;
import service.history.InMemoryHistoryService;
import service.task.FileBackedTaskService;
import service.task.InMemoryTaskService;
import service.task.TaskService;

import java.nio.file.Path;

public class Managers {
    public static TaskService getDefaults() {
        return new InMemoryTaskService(new InMemoryHistoryService());
    }

    public static TaskService getFileService(Path pathToFile, boolean needToReadDataFromFile) {
        if (needToReadDataFromFile) {
            return FileBackedTaskService.loadFromFile(pathToFile);
        } else {
            return new FileBackedTaskService(pathToFile);
        }
    }

    public static HistoryService getDefaultHistoryManager() {
        return new InMemoryHistoryService();
    }
}
