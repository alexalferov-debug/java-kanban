package service.history;

import model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryService implements HistoryService {
    List<Task> history = new LinkedList<>();
    private static final int MAX_SIZE = 10;

    @Override
    public void add(Task task) {
        if (history.size() == MAX_SIZE){
            history.removeFirst();
        }
        history.add(new Task(task));
    }

    @Override
    public List<Task> getHistory() {
        return this.history;
    }
}
