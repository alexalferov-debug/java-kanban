import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import service.Managers;
import service.task.FileBackedTaskService;
import service.task.TaskService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        Path path = Paths.get(System.getProperty("user.home"), "tasks", "myTasks.csv");
        TaskService taskService = Managers.getFileService(path,false);
        Epic epic = new Epic("Тестовый эпик 1", "Добавим в него много задач, потом удалим эпик", Status.DONE);
        Epic epic1 = new Epic("Тестовый эпик 2", "Удалим пустой эпик", Status.NEW);
        Task task = new Task("Таск для тестов", "Описание таска, таск без статуса", null);
        Task task2 = new Task("второй таск для тестов", "сразу закроем, потом переоткроем", Status.DONE);

        if (taskService.dropSubTask(0)) {
            System.out.println("Если ты это видишь, удаление сабтасков работает неправильно");
        } else {
            System.out.println("Так как тасков нет - удалить подтаск не удалось");
        }
        if (taskService.dropEpic(1)) {
            System.out.println("Если ты это видишь, удаление эпиков работает неправильно");
        } else {
            System.out.println("Так как эпиков нет - удалить не удалось");
        }
        if (taskService.dropTask(1)) {
            System.out.println("Если ты это видишь, удаление тасков работает неправильно");
        } else {
            System.out.println("Так как тасков нет - удалить не удалось");
        }

        Task addedTask = taskService.createTask(task);
        System.out.println("В коллекцию добавили таск: " + addedTask);

        Task addedTask2 = taskService.createTask(task2);
        System.out.println("В коллекцию добавили таск: " + addedTask2);

        System.out.println("Распечатаем все элементы коллекции: ");
        for (Task returnedTask : taskService.getTaskList()) {
            System.out.println(returnedTask);
        }

        System.out.println("Удалим таск и распечатаем список снова: ");
        taskService.dropTask(task.getId());
        System.out.println("Распечатаем все элементы коллекции: ");
        for (Task returnedTask : taskService.getTaskList()) {
            System.out.println(returnedTask);
        }

        Epic returnedEpic;
        SubTask subTask = new SubTask("подтаска для первого эпика", "закрываем", Status.DONE, 1);

        System.out.println("Создаём сабтаск");
        SubTask returnedSubTask = taskService.createSubTask(subTask);
        if (Objects.isNull(returnedSubTask)) {
            System.out.println("Не удалось добавить сабтаск в несуществующий эпик");
        }


        System.out.println("Создаём эпик и добавляем в него подтаск: ");
        returnedEpic = taskService.createEpic(epic);
        subTask = new SubTask("подтаска для первого эпика", "закрываем", Status.DONE, returnedEpic.getId());
        SubTask subTask1 = new SubTask("другая подтаска для первого эпика", "закрываем", Status.NEW, returnedEpic.getId());
        SubTask subTask2 = new SubTask("ещё подтаска для первого эпика", "закрываем", Status.NEW, returnedEpic.getId());
        SubTask subTask4 = new SubTask("и ещё подтаска для первого эпика", "закрываем", Status.NEW, returnedEpic.getId());
        SubTask subTask5 = new SubTask("очень много их подтаска для первого эпика", "закрываем", Status.NEW, returnedEpic.getId());
        SubTask subTask7 = new SubTask("и все в первом эпике подтаска для первого эпика", "закрываем", Status.IN_WORK, returnedEpic.getId());
        SubTask subTask9 = new SubTask("да, и эта тоже подтаска для первого эпика", "закрываем", Status.DONE, returnedEpic.getId());

        System.out.println("Эпик создан, изначальный статус = NEW: " + returnedEpic);
        returnedSubTask = taskService.createSubTask(subTask);
        if (Objects.isNull(returnedSubTask)) {
            System.out.println("Не удалось добавить сабтаск в несуществующий эпик");
        } else {
            System.out.println("Сабтаск добавлен успешно: " + returnedSubTask);
        }
        if (taskService.getEpic(returnedEpic.getId()).getStatus().equals(Status.DONE)) {
            System.out.println("В эпик добавлен завершенный сабтаск, статус изменен на done: " + returnedEpic);
        } else {
            System.out.println("Статус эпика некорректный(" + taskService.getEpic(returnedEpic.getId()).getStatus() + "), должен быть DONE");
        }

        System.out.println("Обновим статус сохраненного сабтаска, выставим new: ");
        returnedSubTask.setStatus(Status.NEW);
        System.out.println("Успешно обновили сабтаск: " + taskService.updateSubTask(returnedSubTask));
        if (taskService.getEpic(returnedEpic.getId()).getStatus().equals(Status.NEW)) {
            System.out.println("В эпике обновлен сабтаск, статус изменен на new: " + returnedEpic);
        } else {
            System.out.println("Статус эпика некорректный(" + taskService.getEpic(returnedEpic.getId()).getStatus() + "), должен быть DONE");
        }
        List<Task> getAnotherHistory = taskService.getHistory();

        Epic returnedEpic3 = taskService.createEpic(epic1);
        SubTask subTask11 = new SubTask("подтаска для последнего эпика", "поиграем со статусами", Status.IN_WORK, returnedEpic3.getId());


        System.out.println("Добавим все остальные сабтаски:");
        SubTask returnedSubTask1 = taskService.createSubTask(subTask1);
        SubTask returnedSubTask2 = taskService.createSubTask(subTask2);
        SubTask returnedSubTask4 = taskService.createSubTask(subTask4);
        SubTask returnedSubTask5 = taskService.createSubTask(subTask5);
        SubTask returnedSubTask7 = taskService.createSubTask(subTask7);
        SubTask returnedSubTask9 = taskService.createSubTask(subTask9);
        SubTask returnedSubTask11 = taskService.createSubTask(subTask11);
        System.out.println("Получим и распечатаем список всех сабтасков, входящих в эпик: ");
        for (SubTask subTaskFromEpic1 : taskService.getSubTasksForEpic(returnedEpic)) {
            System.out.println(subTaskFromEpic1);
        }

        if (taskService.getEpic(returnedEpic.getId()).getStatus().equals(Status.IN_WORK)) {
            System.out.println("В эпике обновлен сабтаск, статус изменен на IN_WORK: " + returnedEpic);
        } else {
            System.out.println("Статус эпика некорректный(" + taskService.getEpic(returnedEpic.getId()).getStatus() + "), должен быть DONE");
        }

        List<Task> getHistory = taskService.getHistory();

        returnedSubTask7.setStatus(Status.NEW);
        returnedSubTask9.setStatus(Status.NEW);
        returnedSubTask11.setStatus(Status.NEW);
        taskService.updateSubTask(returnedSubTask7);
        taskService.updateSubTask(returnedSubTask9);
        taskService.updateSubTask(returnedSubTask11);

        if (taskService.getEpic(returnedEpic.getId()).getStatus().equals(Status.NEW)) {
            System.out.println("В эпике обновлены сабтаски, статус изменен на NEW: " + returnedEpic);
        } else {
            System.out.println("Статус эпика некорректный(" + taskService.getEpic(returnedEpic.getId()).getStatus() + "), должен быть DONE");
        }

        System.out.println("Список эпиков: ");
        for (Epic epic3 : taskService.getEpicList()) {
            System.out.println(epic3);
        }

        System.out.println("Удаляем эпик, проверяем, что сабтаски тоже удалены: ");
        taskService.dropEpic(returnedEpic.getId());
        for (SubTask subTaskFromList : taskService.getSubTaskList()) {
            System.out.println(subTaskFromList);
        }

        returnedSubTask11.setStatus(Status.DONE);
        taskService.updateSubTask(returnedSubTask11);
        returnedEpic3 = taskService.getEpic(returnedEpic3.getId());

        System.out.println("Закрываем единственный сабтаск, проверяем, что эпик тоже закрывается");
        if (taskService.getEpic(returnedEpic3.getId()).getStatus().equals(Status.DONE)) {
            System.out.println("В эпике обновлен сабтаск, статус изменен на DONE: " + returnedEpic3);
        } else {
            System.out.println("Статус эпика некорректный(" + taskService.getEpic(returnedEpic3.getId()).getStatus() + "), должен быть DONE");
        }

        returnedEpic3.setDescription("Не будем удалять пустой эпик, поиграемся со статусами");
        taskService.updateEpic(returnedEpic3);
        if (taskService.getEpic(returnedEpic3.getId()).getStatus().equals(Status.DONE)) {
            System.out.println("В эпике обновлен сабтаск, статус изменен на DONE: " + returnedEpic3);
        } else {
            System.out.println("Статус эпика некорректный(" + taskService.getEpic(returnedEpic3.getId()).getStatus() + "), должен быть DONE");
        }

        System.out.println("Удаляем сабтаск, проверяем, что статус эпика изменился на new");
        taskService.dropSubTask(returnedSubTask11.getId());
        returnedEpic3 = taskService.getEpic(returnedEpic3.getId());
        if (taskService.getEpic(returnedEpic3.getId()).getStatus().equals(Status.NEW)) {
            System.out.println("В эпике обновлен сабтаск, статус изменен на NEW: " + returnedEpic3);
        } else {
            System.out.println("Статус эпика некорректный(" + taskService.getEpic(returnedEpic3.getId()).getStatus() + "), должен быть DONE");
        }
        SubTask subTask3 = new SubTask("Добавим в эпик, чтоб не грустил","Щас в тесты сохраню предзаолненный файл",Status.NEW,returnedEpic3.getId());
        taskService.createSubTask(subTask3);
    }

}
