package service.history;

import model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryService implements HistoryService {
    List<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (history.size() == 10){
            history.removeFirst();
        }
        history.add(new Task(task));
    }

    @Override
    public List<Task> getHistory() {
        return this.history;
    }
}
