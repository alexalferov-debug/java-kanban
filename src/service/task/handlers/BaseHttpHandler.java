package service.task.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import model.adapters.LocalDateTimeAdapter;
import service.task.TaskService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    TaskService taskService;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    protected void sendText(HttpExchange exchange, String text, int code) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

}
