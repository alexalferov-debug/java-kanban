package service.history;

import helpers.AssertHelpers;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryServiceTest {
    HistoryService historyService;
    Task task;
    Epic epic;
    SubTask subTask;

    @BeforeEach
    public void setUpTestData() {
        historyService = new InMemoryHistoryService();
        task = new Task("Таск для добавления", "Просто добавим его в разные списки", Status.NEW);
        epic = new Epic("Таск для добавления", "Просто добавим его в разные списки", Status.NEW);
        subTask = new SubTask("Лорем ипсум","и выпей чаю",Status.NEW,1);
    }

    @Test
    public void shouldAddAllTypesExtendsTasks(){
        task.setId(2);
        epic.setId(1);
        subTask.setId(3);
        historyService.add(task);
        historyService.add(epic);
        historyService.add(subTask);
        assertEquals(3,historyService.getHistory().size());
    }

    @Test
    public void shouldSaveCurrentStateOfTask(){
        historyService.add(task);
        task.setStatus(Status.DONE);
        Assertions.assertNotEquals(historyService.getHistory().getFirst().getStatus(),task.getStatus());
    }

    @Test
    public void testReplaceTaskInHistory() {
        task.setId(1);
        Task task2 = task;
        historyService.add(task);
        historyService.add(task2);

        List<Task> history = historyService.getHistory();
        assertEquals(1, history.size());
        AssertHelpers.equalsForTasks(task2, history.getFirst());
    }

    @Test
    public void testRemoveTask() {
        task.setId(1);
        Task task2 = task;
        task2.setId(3);
        historyService.add(task);
        historyService.add(task2);
        historyService.remove(1);
        List<Task> history = historyService.getHistory();
        assertEquals(1, history.size());
        AssertHelpers.equalsForTasks(task2, history.getFirst());
    }

    @Test
    public void testRemoveNonExistentTask() {
        historyService.add(task);

        historyService.remove(2);
        List<Task> history = historyService.getHistory();
        assertEquals(1, history.size());
        AssertHelpers.equalsForTasks(task, history.getFirst());
    }

    @Test
    public void testGetEmptyHistory() {
        List<Task> history = historyService.getHistory();
        assertTrue(history.isEmpty());
    }
}