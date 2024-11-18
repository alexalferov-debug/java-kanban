package service.task.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.dto.Message;
import service.task.TaskService;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    public PrioritizedHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            getPrioritizedTasks(exchange);
        } else {
            sendText(exchange, gson.toJson(new Message("Метод " + method + " не поддерживается")), 404);
        }
    }

    public void getPrioritizedTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskService.getPrioritizedTasks()), 200);
    }

}
