package service.task.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.dto.Message;
import service.task.TaskService;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            getHistory(exchange);
        } else {
            sendText(exchange, gson.toJson(new Message("Метод " + method + " не поддерживается")), 404);
        }
    }

    public void getHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskService.getHistory()), 200);
    }

}
