package service.task.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import model.dto.Message;
import service.task.TaskService;
import service.task.exceptions.NotFoundException;
import service.task.exceptions.ValidationException;

import java.io.IOException;
import java.util.Objects;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    public TaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET": {
                getTasks(exchange);
                break;
            }
            case "POST": {
                addTask(exchange);
                break;
            }
            case "DELETE": {
                deleteTask(exchange);
                break;
            }
            default: {
                sendText(exchange, gson.toJson(new Message("Метод " + method + " не поддерживается")), 404);
                break;
            }
        }
    }

    private void addTask(HttpExchange exchange) throws IOException {
        Task task;
        try {
            task = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Task.class);
            task.check();
        } catch (Exception e) {
            task = null;
            sendText(exchange, gson.toJson(new Message("Во время парсинга тела запроса произошла ошибка: " + e.getMessage())), 400);
        }
        if (Objects.isNull(task.getId())) {
            try {
                Task createdTask = taskService.createTask(task);
                sendText(exchange, gson.toJson(createdTask), 201);
            } catch (ValidationException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 406);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message("Во время работы приложения возникла ошибка: " + e.getMessage())), 500);
            }
        } else {
            try {
                Task updatedTask = taskService.updateTask(task);
                sendText(exchange, gson.toJson(updatedTask), 201);
            } catch (ValidationException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 406);
            } catch (NotFoundException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message("Во время работы приложения возникла ошибка: " + e.getMessage())), 500);
            }
        }
    }

    private void getTasks(HttpExchange exchange) throws IOException {
        String[] uriParts = exchange.getRequestURI().getPath().split("/");
        if (uriParts.length == 2) {
            sendText(exchange, gson.toJson(taskService.getTaskList()), 202);
        } else {
            try {
                sendText(exchange, gson.toJson(taskService.getTask(Integer.parseInt(uriParts[2]))), 202);
            } catch (NotFoundException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 500);
            }
        }
    }

    private void deleteTask(HttpExchange exchange) throws IOException {
        String[] uriParts = exchange.getRequestURI().getPath().split("/");
        if (uriParts.length < 3) {
            sendText(exchange, "Не передан id таска, который требуется удалить", 404);
        } else {
            try {
                if (taskService.dropTask(Integer.parseInt(uriParts[2]))) {
                    sendText(exchange, gson.toJson(new Message("таск " + uriParts[2] + " успешно удалён")), 202);
                } else {
                    sendText(exchange, gson.toJson(new Message("таск " + uriParts[2] + " не найден")), 404);
                }
            } catch (NumberFormatException e) {
                sendText(exchange, gson.toJson(new Message("Передан некорректный идентификатор " + uriParts[2])), 500);
            }
        }
    }
}
