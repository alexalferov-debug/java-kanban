package model;

public enum Endpoint {
    TASK("/tasks"),
    SUBTASK("/subtasks"),
    EPIC("/epics"),
    HISTORY("/history"),
    PRIORITIZED("/prioritized");


    Endpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    private final String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    public String getModifiedEndpoint(String addPart) {
        return this.endpoint + addPart;
    }
}
