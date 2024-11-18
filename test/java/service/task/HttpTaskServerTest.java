package service.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import helpers.AssertHelpers;
import model.Endpoint;
import model.Epic;
import model.SubTask;
import model.Task;
import model.adapters.LocalDateTimeAdapter;
import model.dto.EpicDto;
import model.dto.Message;
import model.dto.SubTaskDTO;
import org.junit.jupiter.api.*;
import service.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static model.Status.DONE;
import static model.Status.NEW;

public class HttpTaskServerTest {
    HttpTaskServer httpTaskServer;
    TaskService taskService;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    String baseUrl = "http://localhost:8080%s";

    @BeforeEach
    public void startServer() throws IOException {
        taskService = Managers.getDefaults();
        httpTaskServer = new HttpTaskServer(8080, taskService);
        httpTaskServer.start();
    }

    @AfterEach
    public void stopServer() {
        httpTaskServer.stop(0);
    }

    @Test
    @DisplayName("Если тасков нет - возвращается пустой список")
    public void returnEmptyTaskList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.TASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> res = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        Assertions.assertEquals(Collections.emptyList(), res);
    }

    @Test
    @DisplayName("Если сабтасков нет - возвращается пустой список")
    public void returnEmptySubTaskList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.SUBTASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> res = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
        }.getType());
        Assertions.assertEquals(Collections.emptyList(), res);
    }

    @Test
    @DisplayName("Если эпиков нет - возвращается пустой список")
    public void returnEmptyEpicsList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.EPIC.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> res = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());
        Assertions.assertEquals(Collections.emptyList(), res);
    }

    @TestFactory
    @DisplayName("Проверка параметров запроса на добавление таска")
    public Iterable<DynamicTest> taskTestFactory() throws IOException, InterruptedException {
        Task task = new Task("Тестовый таск", "Добавляем в менеджер", NEW);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.TASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task taskFromResponse = gson.fromJson(response.body(), Task.class);
        task.setId(1);
        return Arrays.asList(
                DynamicTest.dynamicTest("Ответ содержит код 201",
                        () -> Assertions.assertEquals(201, response.statusCode())),
                DynamicTest.dynamicTest("Тело содержит данные таска",
                        () -> AssertHelpers.equalsForTasks(task, taskFromResponse)),
                DynamicTest.dynamicTest("После добавления таска видим его в сервисе",
                        () -> AssertHelpers.equalsForTasks(task, taskService.getTask(1)))
        );
    }

    @TestFactory
    @DisplayName("Проверка параметров запроса на добавление сабтаска")
    public Iterable<DynamicTest> subtaskTestFactory() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "нужен для добавление сабтаска", NEW);
        Epic epic1 = taskService.createEpic(epic);
        SubTask task = new SubTask("Тестовый таск", "Добавляем в менеджер", NEW, epic1.getId());
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.SUBTASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task, SubTask.class))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTaskDTO taskFromResponse = gson.fromJson(response.body(), SubTaskDTO.class);
        task.setId(2);
        return Arrays.asList(
                DynamicTest.dynamicTest("Ответ содержит код 201",
                        () -> Assertions.assertEquals(201, response.statusCode())),
                DynamicTest.dynamicTest("Тело ответа содержит данные сабтаска + данные эпика",
                        () -> {
                            SubTask subTask = taskService.getSubTask(2);
                            Assertions.assertEquals(subTask.getId(), taskFromResponse.getId(), "Статусы совпадают");
                            Assertions.assertEquals(subTask.getTitle(), taskFromResponse.getTitle(), "Заголовки совпадают");
                            Assertions.assertEquals(subTask.getDescription(), taskFromResponse.getDescription(), "Описания совпадают");
                            Assertions.assertEquals(subTask.getStatus(), taskFromResponse.getStatus(), "Статусы совпадают");
                            Assertions.assertEquals(subTask.getStartTime(), taskFromResponse.getStartTime(), "Время начала совпадает");
                            Assertions.assertEquals(subTask.getEndTime(), taskFromResponse.getEndTime(), "Время окончания совпадает");
                            Assertions.assertEquals(subTask.getDurationInMinutes(), taskFromResponse.getDurationInMinutes(), "Продолжительность совпадает");
                            AssertHelpers.equalsForEpics(epic1, taskFromResponse.getEpic());
                        }),
                DynamicTest.dynamicTest("После добавления таска видим его в сервисе",
                        () -> AssertHelpers.equalsForSubTasks(task, taskService.getSubTask(2)))
        );
    }

    @TestFactory
    @DisplayName("Проверка параметров запроса на добавление эпика")
    public Iterable<DynamicTest> epicTestFactory() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "нужен для добавление сабтаска", NEW);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.EPIC.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic, Epic.class))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        EpicDto taskFromResponse = gson.fromJson(response.body(), EpicDto.class);
        epic.setId(1);
        return Arrays.asList(
                DynamicTest.dynamicTest("Ответ содержит код 201",
                        () -> Assertions.assertEquals(201, response.statusCode())),
                DynamicTest.dynamicTest("Тело ответа содержит данные сабтаска + данные эпика",
                        () -> {
                            Assertions.assertEquals(epic.getId(), taskFromResponse.getId(), "Статусы совпадают");
                            Assertions.assertEquals(epic.getTitle(), taskFromResponse.getTitle(), "Заголовки совпадают");
                            Assertions.assertEquals(epic.getDescription(), taskFromResponse.getDescription(), "Описания совпадают");
                            Assertions.assertEquals(epic.getStatus(), taskFromResponse.getStatus(), "Статусы совпадают");
                            Assertions.assertEquals(epic.getStartTime(), taskFromResponse.getStartTime(), "Время начала совпадает");
                            Assertions.assertEquals(epic.getEndTime(), taskFromResponse.getEndTime(), "Время окончания совпадает");
                            Assertions.assertEquals(epic.getDurationInMinutes(), taskFromResponse.getDurationInMinutes(), "Продолжительность совпадает");
                            Assertions.assertEquals(Collections.emptyList(), taskFromResponse.getSubTasks());
                        }),
                DynamicTest.dynamicTest("После добавления таска видим его в сервисе",
                        () -> AssertHelpers.equalsForEpics(epic, taskService.getEpic(1)))
        );
    }

    @TestFactory
    @DisplayName("Проверка параметров запроса на добавление таска")
    public Iterable<DynamicTest> taskTestFactoryWithIncorrectRequestType() throws IOException, InterruptedException {
        Task task = new Task("Тестовый таск", "Добавляем в менеджер", NEW);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.TASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(task))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Message respBody = gson.fromJson(response.body(), Message.class);
        task.setId(1);
        return Arrays.asList(
                DynamicTest.dynamicTest("Ответ содержит код 404",
                        () -> Assertions.assertEquals(404, response.statusCode())),
                DynamicTest.dynamicTest("Тело содержит сообщение \"Метод %название_метода% не поддерживается\"",
                        () -> Assertions.assertEquals("Метод PUT не поддерживается", respBody.getMessage())),
                DynamicTest.dynamicTest("Таск не добавлен",
                        () -> Assertions.assertEquals(Collections.emptyList(), taskService.getTaskList()))
        );
    }

    @TestFactory
    @DisplayName("Не удалось добавить таск с пустым телом запроса")
    public Iterable<DynamicTest> taskTestFactoryWithoutRequestBody() throws IOException, InterruptedException {
        Task task = new Task("Тестовый таск", "Добавляем в менеджер", NEW);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.TASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("{\"a\":\"b\"}")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Message respBody = gson.fromJson(response.body(), Message.class);
        task.setId(1);
        return Arrays.asList(
                DynamicTest.dynamicTest("Ответ содержит код 400",
                        () -> Assertions.assertEquals(400, response.statusCode())),
                DynamicTest.dynamicTest("Во время парсинга тела запроса произошла ошибка: Не удалось распарсить объект из переданного запроса\"",
                        () -> Assertions.assertEquals("Во время парсинга тела запроса произошла ошибка: Не удалось распарсить объект из переданного запроса", respBody.getMessage())),
                DynamicTest.dynamicTest("Таск не добавлен",
                        () -> Assertions.assertEquals(Collections.emptyList(), taskService.getTaskList()))
        );
    }

    @Test
    @DisplayName("После обновления таска - возвращается статус ответа 201")
    public void updateTaskResponseShouldBe201() throws IOException, InterruptedException {
        Task task = new Task("Тестовый таск", "Добавляем в менеджер", NEW);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format(baseUrl, Endpoint.TASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task createdTask = gson.fromJson(response.body(), Task.class);
        createdTask.setDescription("Измененное описание");
        HttpRequest requestForUpdate = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(createdTask))).build();
        HttpResponse<String> responseForUpdate = client.send(requestForUpdate, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(201, responseForUpdate.statusCode());
    }

    @TestFactory
    @DisplayName("Возвращается корректная история обращений к таскам")
    public Iterable<DynamicTest> checkTaskHistory() throws IOException, InterruptedException {
        Task task = new Task("Тестовый таск", "Добавляем в менеджер", NEW);
        SubTask subTask = new SubTask("Тестовый сабтаск", "Описание", DONE, 1);
        Epic epic = new Epic("Тестовый эпик", "ЛДВаоылдваоывлдаоыва", NEW);
        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create(String.format(baseUrl, Endpoint.TASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task createdTask = gson.fromJson(response.body(), Task.class);

        URI urlEpic = URI.create(String.format(baseUrl, Endpoint.EPIC.getEndpoint()));
        request = HttpRequest.newBuilder().uri(urlEpic).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic))).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic createdEpic = gson.fromJson(response.body(), Epic.class);

        subTask.setEpicId(createdEpic.getId());
        URI urlSubtask = URI.create(String.format(baseUrl, Endpoint.SUBTASK.getEndpoint()));
        request = HttpRequest.newBuilder().uri(urlSubtask).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask))).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask createdSubtask = gson.fromJson(response.body(), SubTask.class);

        subTask.setDescription("Описание меняется");
        request = HttpRequest.newBuilder().uri(urlSubtask).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask))).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask anotherSubtask = gson.fromJson(response.body(), SubTask.class);

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.TASK.getModifiedEndpoint(String.valueOf(createdTask.getId()))))).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.TASK.getModifiedEndpoint(String.valueOf(createdTask.getId()))))).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.SUBTASK.getModifiedEndpoint(String.valueOf(createdSubtask.getId()))))).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.HISTORY.getEndpoint()))).GET().build();
        HttpResponse<String> anotherResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromResponse = gson.fromJson(anotherResponse.body(), new TypeToken<List<Task>>() {
        }.getType());
        return Arrays.asList(
                DynamicTest.dynamicTest("Ответ содержит код 200",
                        () -> Assertions.assertEquals(200, anotherResponse.statusCode())),
                DynamicTest.dynamicTest("Тело ответа содержит ровно то же, что и сервис",
                        () -> Assertions.assertTrue(taskService.getHistory().containsAll(tasksFromResponse)))
        );
    }

    @TestFactory
    @DisplayName("Возвращается коректный список сортированных тасков")
    public Iterable<DynamicTest> checkPrioritized() throws IOException, InterruptedException {
        Task task = new Task("Тестовый таск", "Добавляем в менеджер", NEW);
        SubTask subTask = new SubTask("Тестовый сабтаск", "Описание", DONE, 1);
        Epic epic = new Epic("Тестовый эпик", "ЛДВаоылдваоывлдаоыва", NEW);
        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create(String.format(baseUrl, Endpoint.TASK.getEndpoint()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task createdTask = gson.fromJson(response.body(), Task.class);

        URI urlEpic = URI.create(String.format(baseUrl, Endpoint.EPIC.getEndpoint()));
        request = HttpRequest.newBuilder().uri(urlEpic).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic))).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic createdEpic = gson.fromJson(response.body(), Epic.class);

        subTask.setEpicId(createdEpic.getId());
        URI urlSubtask = URI.create(String.format(baseUrl, Endpoint.SUBTASK.getEndpoint()));
        request = HttpRequest.newBuilder().uri(urlSubtask).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask))).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask createdSubtask = gson.fromJson(response.body(), SubTask.class);

        subTask.setDescription("Описание меняется");
        request = HttpRequest.newBuilder().uri(urlSubtask).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask))).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask anotherSubtask = gson.fromJson(response.body(), SubTask.class);

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.TASK.getModifiedEndpoint(String.valueOf(createdTask.getId()))))).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.TASK.getModifiedEndpoint(String.valueOf(createdTask.getId()))))).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.SUBTASK.getModifiedEndpoint(String.valueOf(createdSubtask.getId()))))).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(URI.create(String.format(baseUrl, Endpoint.HISTORY.getEndpoint()))).GET().build();
        HttpResponse<String> anotherResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromResponse = gson.fromJson(anotherResponse.body(), new TypeToken<List<Task>>() {
        }.getType());
        return Arrays.asList(
                DynamicTest.dynamicTest("Ответ содержит код 200",
                        () -> Assertions.assertEquals(200, anotherResponse.statusCode())),
                DynamicTest.dynamicTest("Тело ответа содержит ровно то же, что и сервис",
                        () -> {
                        List<Task> prioritized = taskService.getPrioritizedTasks();
                        for (int i = 0; i < tasksFromResponse.size();i++){
                            AssertHelpers.equalsForTasks(prioritized.get(i),tasksFromResponse.get(i));//Тут важен не только факт наличия, но и порядок
                        }
                        })
        );
    }
}
