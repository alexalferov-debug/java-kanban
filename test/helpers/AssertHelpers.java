package helpers;

import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Assertions;

public class AssertHelpers{

    public static void equalsForTasks(Task expected, Task actual){
        Assertions.assertEquals(expected.getId(),actual.getId(),"Статусы совпадают");
        Assertions.assertEquals(expected.getTitle(),actual.getTitle(),"Заголовки совпадают");
        Assertions.assertEquals(expected.getDescription(),actual.getDescription(),"Описания совпадают");
        Assertions.assertEquals(expected.getStatus(),actual.getStatus(),"Статусы совпадают");
    }

    public static void equalsForEpics(Epic expected,Epic actual){
        equalsForTasks(expected,actual);
        Assertions.assertEquals(expected.getSubTaskIds(),actual.getSubTaskIds(),"Списки сабтасков совпадают");
    }

    public static void equalsForSubTasks(SubTask expected, SubTask actual){
        equalsForTasks(expected,actual);
        Assertions.assertEquals(expected.getEpicId(),actual.getEpicId(),"Идентификаторы эпиков совпадают");
    }

}
