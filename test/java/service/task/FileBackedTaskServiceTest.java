package service.task;

import helpers.AssertHelpers;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;
import service.Managers;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

class FileBackedTaskServiceTest {

    private TaskService service;
    static File emptyFile;
    Epic epic1 = new Epic("Тестовый эпик 2", "Не будем удалять пустой эпик, поиграемся со статусами", Status.NEW);
    SubTask subTask3 = new SubTask("Добавим в эпик, чтоб не грустил", "Щас в тесты сохраню предзаолненный файл", Status.NEW, 5);
    Task task2 = new Task("второй таск для тестов", "сразу закроем, потом переоткроем", Status.DONE, LocalDateTime.now().plusMinutes(40),50);
    Task task = new Task("Таск для тестов", "Описание таска, таск без статуса", null,LocalDateTime.now(),30);

    @AfterEach
    public void tearDownWriteTests() {
        emptyFile.deleteOnExit();
        service = null;
    }

    @BeforeEach
    public void setupWriteTests() {
        try {
            emptyFile = File.createTempFile("mytaskstest", ".csv", null);
        } catch (IOException e) {
            System.out.println("Не удалось создать пустой временный файл");
        }
    }

    @Test
    public void checkTasksFromFile() {
        service = Managers.getFileService(emptyFile.toPath(),false);
        Task createdTask = service.createTask(task2);
        TaskService service1 = Managers.getFileService(Path.of(emptyFile.toURI()),true);
        AssertHelpers.equalsForTasks(service1.getTask(createdTask.getId()), service.getTask(createdTask.getId()));
    }

    @Test
    public void checkReadEpicsFromFile() {
        service = Managers.getFileService(emptyFile.toPath(),false);
        Epic createdTask = service.createEpic(epic1);
        TaskService service1 = Managers.getFileService(Path.of(emptyFile.toURI()),true);
        AssertHelpers.equalsForTasks(service1.getEpic(createdTask.getId()), service.getEpic(createdTask.getId()));
    }

    @Test
    public void checkReadSubtasksFromFile() {
        service = Managers.getFileService(emptyFile.toPath(),false);
        Epic createdEpic = service.createEpic(epic1);
        subTask3.setEpicId(createdEpic.getId());
        SubTask createdTask = service.createSubTask(subTask3);
        TaskService service1 = Managers.getFileService(Path.of(emptyFile.toURI()),true);
        AssertHelpers.equalsForTasks(service1.getSubTask(createdTask.getId()), service.getSubTask(createdTask.getId()));
    }

    @Test
    public void checkWriteTaskToFile() {
        service = Managers.getFileService(emptyFile.toPath(),false);
        Task createdTask = service.createTask(task2);
        TaskService anotherService = FileBackedTaskService.loadFromFile(emptyFile.toPath());
        AssertHelpers.equalsForTasks(service.getTask(createdTask.getId()), anotherService.getTask(createdTask.getId()));
    }

    @Test
    public void checkWriteTwoTaskToFile() {
        service = Managers.getFileService(emptyFile.toPath(),false);
        Task createdTask = service.createTask(task2);
        Task anotherCreatedTask = service.createTask(task);
        TaskService anotherService = FileBackedTaskService.loadFromFile(emptyFile.toPath());
        AssertHelpers.equalsForTasks(service.getTask(createdTask.getId()), anotherService.getTask(createdTask.getId()));
        AssertHelpers.equalsForTasks(service.getTask(anotherCreatedTask.getId()), anotherService.getTask(anotherCreatedTask.getId()));
    }

    @Test
    public void checkWriteEpicToFile() {
        service = Managers.getFileService(emptyFile.toPath(),false);
        Epic createdTask = service.createEpic(epic1);
        TaskService anotherService = FileBackedTaskService.loadFromFile(emptyFile.toPath());
        AssertHelpers.equalsForEpics(service.getEpic(createdTask.getId()), anotherService.getEpic(createdTask.getId()));
    }

    @Test
    public void checkWriteSubtaskToFile() {
        service = Managers.getFileService(emptyFile.toPath(),false);
        Epic createdEpic = service.createEpic(epic1);
        subTask3.setEpicId(createdEpic.getId());
        SubTask createdTask = service.createSubTask(subTask3);
        TaskService anotherService = FileBackedTaskService.loadFromFile(emptyFile.toPath());
        AssertHelpers.equalsForSubTasks(service.getSubTask(createdTask.getId()), anotherService.getSubTask(createdTask.getId()));
        AssertHelpers.equalsForEpics(service.getEpic(createdEpic.getId()), anotherService.getEpic(createdEpic.getId()));
    }

    @Test
    public void checkReadDataFromEmptyFile(){
        service = Managers.getFileService(emptyFile.toPath(),true);
        Epic createdEpic = service.createEpic(epic1);
        TaskService anotherService = FileBackedTaskService.loadFromFile(emptyFile.toPath());
        Assertions.assertEquals(anotherService.getEpic(createdEpic.getId()).getId(),1);
    }
}
