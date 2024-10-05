package service.history;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryService implements HistoryService {
    Map<Integer, Node> history = new HashMap<>();
    Node first;
    Node last;

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    @Override
    public void add(Task task) {
        if (history.containsKey(task.getId())) {
            removeNode(history.get(task.getId()));
        }
        linkLast(new Task(task));
        history.put(task.getId(), last);
    }

    @Override
    public void remove(int id) {
        if (history.containsKey(id)) {
            removeNode(history.get(id));
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node current = first;
        while (current != null) {
            historyList.add(current.task);
            current = current.next;
        }
        return historyList;
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            first = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            last = node.prev;
        }
        history.remove(node.task.getId());
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(last, task, null);
        if (last != null) {
            last.next = newNode;
        }
        last = newNode;
        if (first == null) {
            first = newNode;
        }
    }
}
