package service.task;

import helpers.AssertHelpers;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.history.InMemoryHistoryService;
import service.task.exceptions.NotFoundException;
import service.task.exceptions.ValidationException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskServiceTest {
    TaskService service;
    Task task;
    Epic epic;

    @BeforeEach
    public void setUpTestData() {
        service = new InMemoryTaskService(new InMemoryHistoryService());
        task = new Task("Таск для добавления", "Просто добавим его в разные списки", Status.NEW);
        epic = new Epic("Таск для добавления", "Просто добавим его в разные списки", Status.NEW);
    }

    @Test
    public void shouldTasksEqualsIfIdsEquals() {
        Task task1 = new Task("Заголовок", "Описание", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Заголовок1", "Описание2", Status.NEW);
        task2.setId(1);
        Assertions.assertEquals(task1, task2);
    }

    @Test
    public void shouldTasksNonEqualsIfIdsEquals() {
        Task task1 = new Task("Заголовок", "Описание", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Заголовок", "Описание", Status.NEW);
        task2.setId(2);
        Assertions.assertNotEquals(task1, task2);
    }

    @Test
    public void shouldSubTasksEqualsIfIdsEquals() {
        SubTask task1 = new SubTask("Заголовок", "Описание", Status.NEW, 3);
        task1.setId(1);
        SubTask task2 = new SubTask("Заголовок1", "Описание2", Status.NEW, 4);
        task2.setId(1);
        Assertions.assertEquals(task1, task2);
    }

    @Test
    public void shouldSubTasksNonEqualsIfIdsEquals() {
        SubTask subTask = new SubTask("Заголовок", "Описание", Status.NEW, 3);
        subTask.setId(1);
        SubTask subTask1 = new SubTask("Заголовок", "Описание", Status.NEW, 3);
        subTask1.setId(2);
        Assertions.assertNotEquals(subTask, subTask1);
    }

    @Test
    public void shouldEpicsEqualsIfIdsEquals() {
        Epic epic1 = new Epic("Заголовок", "Описание", Status.NEW);
        epic1.setId(1);
        Epic epic2 = new Epic("Заголовок1", "Описание2", Status.NEW);
        epic2.setId(1);
        Assertions.assertEquals(epic1, epic2);
    }

    @Test
    public void shouldEpicsNonEqualsIfIdsEquals() {
        Epic epic1 = new Epic("Заголовок", "Описание", Status.NEW);
        epic1.setId(1);
        Epic epic2 = new Epic("Заголовок", "Описание", Status.NEW);
        epic2.setId(2);
        Assertions.assertNotEquals(epic1, epic2);
    }

    @Test
    public void shouldReturnFalseOnDropAbsentTask() {
        boolean resultOfDrop = service.dropTask(11);
        assertFalse(resultOfDrop, "При удалении несуществующего таска - возвращаем false");
    }

    @Test
    public void shouldReturnTrueOnDropExistingTask() {
        service.createTask(task);
        boolean resultOfDrop = service.dropTask(1);
        assertTrue(resultOfDrop, "При удалении существующего таска - возвращаем true");
    }

    @Test
    public void shouldReturnEqualEpicAfterAddEpic() {
        Epic returnedEpic = service.createEpic(epic);
        AssertHelpers.equalsForEpics(returnedEpic, service.getEpic(1));
    }

    @Test
    public void shouldDropSubTasksAfterDropEpic() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId());
        SubTask returnedSubtask = service.createSubTask(subTask);
        service.dropEpic(returnedEpic.getId());
        Assertions.assertEquals(0, service.getSubTaskList().size(), "Список подзадач пустой после удаления единственного эпика");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterAddSingleSubTuskInDoneStatus() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId());
        SubTask returnedSubtask = service.createSubTask(subTask);
        Assertions.assertEquals(Status.DONE, service.getEpic(returnedEpic.getId()).getStatus(), "Статус эпика DONE");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterCreate() {
        Epic returnedEpic = service.createEpic(epic);
        Assertions.assertEquals(Status.NEW, service.getEpic(returnedEpic.getId()).getStatus(), "Статус эпика NEW");
    }

    @Test
    public void shouldSeEpictStatusToInWorkWhenAddedSubTasksInDifferentStatuses() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId(), LocalDateTime.now().plusMinutes(50), 30);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId(), LocalDateTime.now().plusMinutes(90), 30);
        SubTask returnedSubtask = service.createSubTask(subTask);
        SubTask returnedSubtask1 = service.createSubTask(subTask1);
        Assertions.assertEquals(Status.IN_WORK, service.getEpic(returnedEpic.getId()).getStatus(), "Статус эпика IN_WORK");
    }

    @Test
    public void shouldRecalcEpicStatusAfterUpdateTasks() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId(), LocalDateTime.now().plusMinutes(50), 30);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId(), LocalDateTime.now().plusMinutes(83), 70);
        SubTask returnedSubtask = service.createSubTask(subTask);
        SubTask returnedSubtask1 = service.createSubTask(subTask1);
        returnedSubtask1.setStatus(Status.NEW);
        service.updateSubTask(returnedSubtask1);
        Assertions.assertEquals(Status.NEW, service.getEpic(returnedEpic.getId()).getStatus(), "Статус эпика NEW");
    }

    @Test
    public void shouldRecalcEpicStatusAfterDropTasks() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId(), LocalDateTime.now().plusMinutes(50), 30);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId());
        SubTask returnedSubtask = service.createSubTask(subTask);
        SubTask returnedSubtask1 = service.createSubTask(subTask1);
        service.dropSubTask(returnedSubtask1.getId());
        Assertions.assertEquals(Status.NEW, service.getEpic(returnedEpic.getId()).getStatus(), "Статус эпика NEW");
    }

    @Test
    public void shouldReturnNullOnCreateSubTuskIfEpicIdIsNotValid() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, 123123);
        //SubTask returnedSubtask = service.createSubTask(subTask1);
        Assertions.assertThrows(NotFoundException.class, () -> service.createSubTask(subTask1), "Добавление подзадачи к несуществующему эпику приводит к исключению");
    }

    @Test
    public void shouldReturnUnModifiedTasks() {
        Task returnedTask = service.createTask(task);
        int taskId = returnedTask.getId();
        returnedTask.setId(11);
        returnedTask.setTitle("23");
        returnedTask.setDescription("FF");
        returnedTask.setStatus(Status.DONE);
        Assertions.assertNotEquals(returnedTask.getId(), service.getTask(taskId).getId());
        Assertions.assertNotEquals(returnedTask.getTitle(), service.getTask(taskId).getTitle());
        Assertions.assertNotEquals(returnedTask.getDescription(), service.getTask(taskId).getDescription());
        Assertions.assertNotEquals(returnedTask.getStatus(), service.getTask(taskId).getStatus());
    }

    @Test
    public void shouldReturnUnModifiedSubTask() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы попытаться модифицировать его", Status.NEW, returnedEpic.getId());
        SubTask returnedSubTask = service.createSubTask(subTask);
        int taskId = returnedSubTask.getId();
        returnedSubTask.setId(11);
        returnedSubTask.setTitle("23");
        returnedSubTask.setDescription("FF");
        returnedSubTask.setStatus(Status.DONE);
        returnedSubTask.setEpicId(13);
        Assertions.assertNotEquals(returnedSubTask.getId(), service.getSubTask(taskId).getId());
        Assertions.assertNotEquals(returnedSubTask.getTitle(), service.getSubTask(taskId).getTitle());
        Assertions.assertNotEquals(returnedSubTask.getDescription(), service.getSubTask(taskId).getDescription());
        Assertions.assertNotEquals(returnedSubTask.getStatus(), service.getSubTask(taskId).getStatus());
        Assertions.assertNotEquals(returnedSubTask.getEpicId(), service.getSubTask(taskId).getEpicId());
    }

    @Test
    public void shouldReturnUnModifiedEpic() {
        Epic returnedEpic = service.createEpic(epic);
        int taskId = returnedEpic.getId();
        returnedEpic.setId(11);
        returnedEpic.setTitle("23");
        returnedEpic.setDescription("FF");
        returnedEpic.setStatus(Status.DONE);
        returnedEpic.setSubTaskIds(List.of(1, 3, 11));
        Assertions.assertNotEquals(returnedEpic.getId(), service.getEpic(taskId).getId());
        Assertions.assertNotEquals(returnedEpic.getTitle(), service.getEpic(taskId).getTitle());
        Assertions.assertNotEquals(returnedEpic.getDescription(), service.getEpic(taskId).getDescription());
        Assertions.assertNotEquals(returnedEpic.getStatus(), service.getEpic(taskId).getStatus());
        Assertions.assertNotEquals(returnedEpic.getSubTaskIds(), service.getEpic(taskId).getSubTaskIds());
    }

    @Test
    public void shouldReturnUnModifiedTasksOnGetCall() {
        Task task1 = service.createTask(task);
        Task returnedTask = service.getTask(task1.getId());
        int taskId = returnedTask.getId();
        returnedTask.setId(11);
        returnedTask.setTitle("23");
        returnedTask.setDescription("FF");
        returnedTask.setStatus(Status.DONE);
        Assertions.assertNotEquals(returnedTask.getId(), service.getTask(taskId).getId());
        Assertions.assertNotEquals(returnedTask.getTitle(), service.getTask(taskId).getTitle());
        Assertions.assertNotEquals(returnedTask.getDescription(), service.getTask(taskId).getDescription());
        Assertions.assertNotEquals(returnedTask.getStatus(), service.getTask(taskId).getStatus());
    }

    @Test
    public void shouldReturnUnModifiedSubTaskOnGetCall() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы попытаться модифицировать его", Status.NEW, returnedEpic.getId());
        SubTask subTask1 = service.createSubTask(subTask);
        SubTask returnedSubTask = service.getSubTask(subTask1.getId());
        int taskId = returnedSubTask.getId();
        returnedSubTask.setId(11);
        returnedSubTask.setTitle("23");
        returnedSubTask.setDescription("FF");
        returnedSubTask.setStatus(Status.DONE);
        returnedSubTask.setEpicId(13);
        Assertions.assertNotEquals(returnedSubTask.getId(), service.getSubTask(taskId).getId());
        Assertions.assertNotEquals(returnedSubTask.getTitle(), service.getSubTask(taskId).getTitle());
        Assertions.assertNotEquals(returnedSubTask.getDescription(), service.getSubTask(taskId).getDescription());
        Assertions.assertNotEquals(returnedSubTask.getStatus(), service.getSubTask(taskId).getStatus());
        Assertions.assertNotEquals(returnedSubTask.getEpicId(), service.getSubTask(taskId).getEpicId());
    }

    @Test
    public void shouldReturnUnModifiedEpicOnGetCall() {
        Epic epic1 = service.createEpic(epic);
        Epic returnedEpic = service.getEpic(epic1.getId());
        int taskId = returnedEpic.getId();
        returnedEpic.setId(11);
        returnedEpic.setTitle("23");
        returnedEpic.setDescription("FF");
        returnedEpic.setStatus(Status.DONE);
        returnedEpic.setSubTaskIds(List.of(1, 3, 11));
        Assertions.assertNotEquals(returnedEpic.getId(), service.getEpic(taskId).getId());
        Assertions.assertNotEquals(returnedEpic.getTitle(), service.getEpic(taskId).getTitle());
        Assertions.assertNotEquals(returnedEpic.getDescription(), service.getEpic(taskId).getDescription());
        Assertions.assertNotEquals(returnedEpic.getStatus(), service.getEpic(taskId).getStatus());
        Assertions.assertNotEquals(returnedEpic.getSubTaskIds(), service.getEpic(taskId).getSubTaskIds());
    }

    @Test
    public void shouldSaveCurrentStateOfTask() {
        Task returnedTask = service.createTask(task);
        returnedTask.setStatus(Status.DONE);
        service.updateTask(returnedTask);
        Assertions.assertEquals(returnedTask.getId(), service.getHistory().get(0).getId());
        Assertions.assertEquals(returnedTask.getTitle(), service.getHistory().get(0).getTitle());
        Assertions.assertEquals(returnedTask.getDescription(), service.getHistory().get(0).getDescription());
        Assertions.assertNotEquals(returnedTask.getStatus(), service.getHistory().get(0).getStatus());
    }

    @Test
    public void isCorrectEndTimeForTask() {
        Task createdTask = new Task("Таск для добавления", "Просто добавим его в разные списки", Status.NEW, LocalDateTime.now().plusMinutes(50), 30);
        Task returnedTask = service.createTask(createdTask);
        Assertions.assertEquals(createdTask.getStartTime().plusMinutes(createdTask.getDurationInMinutes()), returnedTask.getEndTime(), "Дата завершения таска рассчитана корректно");
    }


    @Test
    public void ifDurationLessThanZeroItsReplacedOnDefaultValue() {
        Task createdTask = new Task("Таск для добавления", "Просто добавим его в разные списки", Status.NEW, LocalDateTime.now().plusMinutes(50), -5);
        Task returnedTask = service.createTask(createdTask);
        Assertions.assertEquals(createdTask.getStartTime().plusMinutes(15), returnedTask.getEndTime(), "Дата завершения таска рассчитана корректно");
    }

    @Test
    public void checkEpicStartDate() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId(), LocalDateTime.now().plusMinutes(50), 30);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId());
        service.createSubTask(subTask);
        service.createSubTask(subTask1);
        Assertions.assertEquals(subTask1.getStartTime(), service.getEpic(returnedEpic.getId()).getStartTime());
    }

    @Test
    public void checkEpicDuration() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId(), LocalDateTime.now().plusMinutes(50), 30);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId());
        service.createSubTask(subTask);
        service.createSubTask(subTask1);
        Assertions.assertEquals(Duration.between(subTask1.getStartTime(), subTask.getEndTime()).toMinutes(), service.getEpic(returnedEpic.getId()).getDurationInMinutes());
    }

    @Test
    public void checkEpicDurationAfterDropTSubTask() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId(), LocalDateTime.now().plusMinutes(50), 30);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId());
        service.createSubTask(subTask);
        SubTask returnedSubtask = service.createSubTask(subTask1);
        service.dropSubTask(returnedSubtask.getId());
        Assertions.assertEquals(Duration.between(subTask.getStartTime(), subTask.getEndTime()).toMinutes(), service.getEpic(returnedEpic.getId()).getDurationInMinutes());
    }

    @Test
    public void checkThatPrioritizedTaskListReturnAllAddedTasks() {
        Epic returnedEpic = service.createEpic(epic);
        SubTask subTask = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.NEW, returnedEpic.getId(), LocalDateTime.now().plusMinutes(50), 30);
        SubTask subTask1 = new SubTask("Добавляем сабтаск к эпику", "Чтобы удалить его", Status.DONE, returnedEpic.getId());
        service.createSubTask(subTask);
        SubTask returnedSubtask = service.createSubTask(subTask1);
        task.setStartTime(LocalDateTime.now().plusMinutes(180));
        task.setDurationInMinutes(80);
        service.createTask(task);
        Assertions.assertEquals(3, service.getPrioritizedTasks().size());
    }

    @Test
    public void checkThatThrowErrorOnIntersectBetweenTasks() {
        Task task1 = new Task("Проверим пересечения", "Первый таск добавим без проблем", Status.DONE, LocalDateTime.of(2042, 11, 11, 5, 16, 30), 50);
        Task task2 = new Task("Проверим пересечения", "А второй с очевидными проблемами", Status.NEW, LocalDateTime.of(2042, 11, 11, 5, 30, 30), 15);
        service.createTask(task1);
        Assertions.assertThrows(ValidationException.class, () -> service.createTask(task2), "Исключение делает бжжж");
    }

    @Test
    public void checkThatThrowErrorOnIntersectBetweenTaskAndSubtask() {
        Epic epic1 = new Epic("Просто эпик", "Без него не создать сабтаск", Status.NEW);
        Epic returnedEpic = service.createEpic(epic1);
        Task task1 = new Task("Проверим пересечения", "Первый таск добавим без проблем", Status.DONE, LocalDateTime.of(2042, 11, 11, 5, 16, 30), 50);
        SubTask subTask = new SubTask("Проверим пересечения", "А второй с очевидными проблемами", Status.NEW, returnedEpic.getId(), LocalDateTime.of(2042, 11, 11, 5, 30, 30), 15);
        service.createTask(task1);
        Assertions.assertThrows(ValidationException.class, () -> service.createSubTask(subTask), "Исключение делает бжжж");
    }

    @Test
    public void checkThatDontThrowErrorOnIntersectBetweenEpicAndTask() {
        Epic epic1 = new Epic("Просто эпик", "Без него не создать сабтаск", Status.NEW);
        Epic returnedEpic = service.createEpic(epic1);
        Task task1 = new Task("Проверим пересечения", "Продолжительность таска между двумя сабтасками, но без пересечения с ними, при этом с эпиком пересечёмся", Status.DONE, LocalDateTime.of(2042, 11, 11, 5, 16, 30), 50);
        SubTask subTask = new SubTask("Проверим пересечения", "Первую задачу эпика мы ставим на текущий год", Status.NEW, returnedEpic.getId(), LocalDateTime.of(2024, 11, 11, 5, 30, 30), 15);
        SubTask subTask1 = new SubTask("Проверим пересечения", "А последнюю - через век", Status.NEW, returnedEpic.getId(), LocalDateTime.of(2124, 11, 11, 5, 30, 30), 15);
        service.createSubTask(subTask);
        service.createSubTask(subTask1);
        Assertions.assertDoesNotThrow(() -> service.createTask(task1));
    }

}