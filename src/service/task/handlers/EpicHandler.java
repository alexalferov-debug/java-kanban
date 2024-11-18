package service.task.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Epic;
import model.dto.EpicDto;
import model.dto.Message;
import model.dto.SubTaskWithoutEpicDTO;
import service.task.TaskService;
import service.task.exceptions.NotFoundException;
import service.task.exceptions.ValidationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] uriParts = exchange.getRequestURI().getPath().split("/");
        switch (method) {
            case "GET": {
                {
                    if (uriParts.length > 3 && uriParts[3].equals("subtasks")) {
                        try {
                            Epic epic = taskService.getEpic(Integer.parseInt(uriParts[2]));
                            sendText(exchange, gson.toJson(subTaskListToDto(epic)), 202);
                        } catch (NumberFormatException e) {
                            sendText(exchange, gson.toJson(new Message("Передан некорректный идентификатор " + uriParts[2])), 500);
                        }
                    } else {
                        getEpic(exchange);
                    }
                    break;
                }
            }
            case "POST": {
                addEpic(exchange);
                break;
            }
            case "DELETE": {
                deleteEpic(exchange);
                break;
            }
            default: {
                sendText(exchange, gson.toJson(new Message("Метод " + method + " не поддерживается")), 404);
                break;
            }
        }
    }

    private void addEpic(HttpExchange exchange) throws IOException {
        Epic task;
        try {
            task = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Epic.class);
            task.check();
        } catch (Exception e) {
            task = null;
            sendText(exchange, gson.toJson(new Message("Во время парсинга тела запроса произошла ошибка: " + e.getMessage())), 400);
        }
        if (Objects.isNull(task.getId())) {
            try {
                Epic createdTask = taskService.createEpic(task);
                sendText(exchange, gson.toJson(EpicDto.createDTO(createdTask, taskService.getSubTasksForEpic(createdTask))), 201);
            } catch (ValidationException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 406);
            } catch (NotFoundException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message("Во время работы приложения возникла ошибка: " + e.getMessage())), 500);
            }
        } else {
            try {
                Epic updatedTask = taskService.updateEpic(task);
                sendText(exchange, gson.toJson(EpicDto.createDTO(updatedTask, taskService.getSubTasksForEpic(updatedTask))), 201);
            } catch (ValidationException e) {
                sendText(exchange, e.getMessage(), 406);
            } catch (NotFoundException e) {
                sendText(exchange, e.getMessage(), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message("Во время работы приложения возникла ошибка: " + e.getMessage())), 500);
            }
        }
    }

    private void getEpic(HttpExchange exchange) throws IOException {
        String[] uriParts = exchange.getRequestURI().getPath().split("/");
        if (uriParts.length == 2) {
            sendText(exchange, gson.toJson(epicsListToDto(taskService.getEpicList())), 202);
        } else {
            try {
                Epic epic = taskService.getEpic(Integer.parseInt(uriParts[2]));
                sendText(exchange, gson.toJson(EpicDto.createDTO(epic, taskService.getSubTasksForEpic(epic))), 202);
            } catch (NotFoundException e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 404);
            } catch (Exception e) {
                sendText(exchange, gson.toJson(new Message(e.getMessage())), 500);
            }
        }
    }

    private void deleteEpic(HttpExchange exchange) throws IOException {
        String[] uriParts = exchange.getRequestURI().getPath().split("/");
        if (uriParts.length < 3) {
            sendText(exchange, gson.toJson("Не передан id таска, который требуется удалить"), 404);
        } else {
            try {
                if (taskService.dropEpic(Integer.parseInt(uriParts[2]))) {
                    sendText(exchange, gson.toJson(new Message("Эпик " + uriParts[2] + " успешно удалён")), 202);
                } else {
                    sendText(exchange, gson.toJson(new Message("Эпик " + uriParts[2] + "не найден")), 404);
                }
            } catch (NumberFormatException e) {
                sendText(exchange, gson.toJson(new Message("Передан некорректный идентификатор " + uriParts[2])), 500);
            }
        }
    }

    private List<EpicDto> epicsListToDto(List<Epic> epics) {
        List<EpicDto> epicDtos = new ArrayList<>();
        if (epics.isEmpty()) {
            return Collections.emptyList();
        }
        return epics.stream()
                .map(epic -> EpicDto.createDTO(epic, taskService.getSubTasksForEpic(epic)))
                .collect(Collectors.toList());
    }

    public List<SubTaskWithoutEpicDTO> subTaskListToDto(Epic epic) {
        if (epic.getSubTaskIds().isEmpty()) {
            return Collections.emptyList();
        }
        return epic.getSubTaskIds().stream()
                .map(subTask -> SubTaskWithoutEpicDTO.returnSubTaskDto(taskService.getSubTask(subTask)))
                .collect(Collectors.toList());
    }
}
