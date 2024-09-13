package service.history;

import model.Task;

import java.util.List;

public interface HistoryService {

    public void add(Task task);
    public List<Task> getHistory();
}
