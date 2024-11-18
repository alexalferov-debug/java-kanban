package service.task;

import com.sun.net.httpserver.HttpServer;
import static model.Endpoint.*;
import service.task.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    HttpServer httpServer = HttpServer.create();


    public HttpTaskServer(int port, TaskService taskService) throws IOException {
        httpServer.bind(new InetSocketAddress(port), 0);
        httpServer.createContext(TASK.getEndpoint(), new TaskHandler(taskService));
        httpServer.createContext(SUBTASK.getEndpoint(), new SubtaskHandler(taskService));
        httpServer.createContext(EPIC.getEndpoint(), new EpicHandler(taskService));
        httpServer.createContext(HISTORY.getEndpoint(), new HistoryHandler(taskService));
        httpServer.createContext(PRIORITIZED.getEndpoint(), new PrioritizedHandler(taskService));
    }

    public void start() {
        httpServer.start();
        System.out.println("Сервер успешно запущен на порту " + httpServer.getAddress().getPort());
    }

    public void stop(int delay) {
        httpServer.stop(delay);
        System.out.println("Сервер остановлен");
    }
}
