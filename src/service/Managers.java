package service;

import service.history.HistoryService;
import service.history.InMemoryHistoryService;
import service.task.InMemoryTaskService;
import service.task.TaskService;

public class Managers {
        public static TaskService getDefaults(){
            return new InMemoryTaskService(new InMemoryHistoryService());
        }

        public static HistoryService getDefaultHistoryManager(){
            return new InMemoryHistoryService();
        }
}
