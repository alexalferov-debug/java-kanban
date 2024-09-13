package service.history;

import model.Task;

import java.util.List;

public interface HistoryService {

    void add(Task task);
    List<Task> getHistory();
}
