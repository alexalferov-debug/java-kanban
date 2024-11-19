import service.Managers;
import service.task.HttpTaskServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer(8080, Managers.getDefaults());
        httpTaskServer.start();
    }
}
