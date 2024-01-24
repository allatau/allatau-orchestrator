package org.wscp.server.sqlite;

import org.wscp.model.Task;

import java.util.List;

public interface DbHandler {

    List<Task> getAllTasks();
    void addTask(Task task);
    void updateTask(Task task);

    Task getTaskById(String id);

    void deleteTask(String id);

    void recordJobIdToTaskById(String taskId, String jobId);
}