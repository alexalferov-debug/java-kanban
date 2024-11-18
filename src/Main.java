import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import service.Managers;
import service.task.HttpTaskServer;
import service.task.TaskService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer(8080, Managers.getDefaults());
        httpTaskServer.start();
    }
}
