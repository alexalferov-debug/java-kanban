package service.history;

import helpers.AssertHelpers;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        historyService.add(task);
        historyService.add(epic);
        historyService.add(subTask);
        Assertions.assertEquals(3,historyService.getHistory().size());
    }

    @Test
    public void shouldSaveCurrentStateOfTask(){
        historyService.add(task);
        task.setStatus(Status.DONE);
        Assertions.assertNotEquals(historyService.getHistory().getFirst().getStatus(),task.getStatus());
    }

    @Test
    public void shouldClearOldElementsIfListSizeAbove9Elements(){
        SubTask anotherSubtask = new SubTask("Проверим, что этот затёрли","потому что переполнение",Status.IN_WORK,23);
        anotherSubtask.setId(11);
        historyService.add(anotherSubtask);
        for (int i = 0; i <= 9; i = i+2){
            historyService.add(task);
            historyService.add(epic);
        }
        historyService.add(task);
        Assertions.assertNotEquals(anotherSubtask.getId(),historyService.getHistory().getFirst().getId());
        Assertions.assertNotEquals(anotherSubtask.getTitle(),historyService.getHistory().getFirst().getTitle());
        Assertions.assertNotEquals(anotherSubtask.getDescription(),historyService.getHistory().getFirst().getDescription());
        Assertions.assertNotEquals(anotherSubtask.getStatus(),historyService.getHistory().getFirst().getStatus());
    }

    @Test
    public void shouldAddNewElementsInEndOfList(){
        SubTask anotherSubtask = new SubTask("Проверим, что этот затёрли","потому что переполнение",Status.IN_WORK,23);
        anotherSubtask.setId(11);
        for (int i = 0; i <= 9; i = i+2){
            historyService.add(task);
            historyService.add(epic);
        }
        System.out.println(historyService.getHistory().size());
        historyService.add(task);
        historyService.add(anotherSubtask);
        AssertHelpers.equalsForTasks(anotherSubtask,historyService.getHistory().get(9));
    }
}