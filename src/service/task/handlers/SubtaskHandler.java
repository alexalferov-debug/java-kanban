package service.task.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.SubTask;
import model.dto.Message;
import model.dto.SubTaskDTO;
import service.task.TaskService;
import service.task.exceptions.NotFoundException;
import service.task.exceptions.ValidationException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET": {
                getSubtasks(exchange);
                break;
            }
            case "POST": {
                addSubtask(exchange);
                break;
            }
            case "DELETE": {
                deleteSubtask(exchange);
                break;
            }
            default: {
                sendText(exchange, gson.toJson(new Message("Метод " + method + " не поддерживается")), 404);
                break;
            }
        }
    }

    private void addSubtask(HttpExchange exchange) throws IOException {
        SubTask task;
        try {
            task = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), SubTask.class);
            task.check();
        } catch (Exception e) {
            task = null;
            sendText(exchange, gson.toJson(new Message("Во время парсинга тела запроса произошла ошибка: " + e.getMessage())), 400);
        }
        if (Objects.isNull(task.getId())) {
            try {
                SubTask createdTask = taskService.createSubTask(task);
                sendText(exchange, gson.toJson(SubTaskDTO.returnSubTaskDto(createdTask, taskService.getEpic(createdTask.getEpicId()))), 201);
            } catch (ValidationException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 406);
            } catch (NotFoundException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message("Во время работы приложения возникла ошибка: " + e.getMessage())), 500);
            }
        } else {
            try {
                SubTask updatedTask = taskService.updateSubTask(task);
                sendText(exchange, gson.toJson(SubTaskDTO.returnSubTaskDto(updatedTask, taskService.getEpic(updatedTask.getEpicId()))), 201);
            } catch (ValidationException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 406);
            } catch (NotFoundException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message("Во время работы приложения возникла ошибка: " + e.getMessage())), 500);
            }
        }
    }

    private void getSubtasks(HttpExchange exchange) throws IOException {
        String[] uriParts = exchange.getRequestURI().getPath().split("/");
        if (uriParts.length == 2) {
            sendText(exchange, gson.toJson(subTaskListToDto(taskService.getSubTaskList())), 202);
        } else {
            try {
                SubTask subTask = taskService.getSubTask(Integer.parseInt(uriParts[2]));
                sendText(exchange, gson.toJson(SubTaskDTO.returnSubTaskDto(subTask, taskService.getEpic(subTask.getEpicId()))), 202);
            } catch (NotFoundException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 500);
            }
        }
    }

    private void deleteSubtask(HttpExchange exchange) throws IOException {
        String[] uriParts = exchange.getRequestURI().getPath().split("/");
        if (uriParts.length < 3) {
            sendText(exchange, "Не передан id таска, который требуется удалить", 404);
        } else {
            try {
                if (taskService.dropSubTask(Integer.parseInt(uriParts[2]))) {
                    sendText(exchange, gson.toJson(new Message("таск " + uriParts[2] + " успешно удалён")), 202);
                } else {
                    sendText(exchange, gson.toJson(new Message("таск " + uriParts[2] + " не найден")), 404);
                }
            } catch (NumberFormatException e) {
                sendText(exchange, gson.toJson(new Message("Передан некорректный идентификатор " + uriParts[2])), 500);
            }
        }
    }

    public List<SubTaskDTO> subTaskListToDto(List<SubTask> subTaskList) {
        if (subTaskList.isEmpty()) {
            return Collections.emptyList();
        }
        return subTaskList.stream()
                .map(subTask -> SubTaskDTO.returnSubTaskDto(subTask, taskService.getEpic(subTask.getEpicId())))
                .collect(Collectors.toList());
    }
}
