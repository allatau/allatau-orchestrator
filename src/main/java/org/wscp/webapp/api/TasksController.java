package org.wscp.webapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.scheduling.JobScheduler;
import org.wscp.model.Task;
import org.wscp.model.TaskStatus;
import org.wscp.server.sqlite.DbHandler;
import org.wscp.services.OrchestratingService;
import org.wscp.workflow_orchestrator.Agent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Controller("/tasks")
public class TasksController {

    @Inject
    private JobScheduler jobScheduler;

    @Inject
    private OrchestratingService orchestratingService;

    @Inject
    private DbHandler finalDbHandler;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getAllTasks() {
        return finalDbHandler.getAllTasks();
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Task getTaskById(@PathVariable String id) {

        return finalDbHandler.getTaskById(id);
    }

    @Get("/{id}/abort")
    @Produces(MediaType.APPLICATION_JSON)
    public Task abortTaskById(@PathVariable String id) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Task task = finalDbHandler.getTaskById(id);
        Agent agent = gson.fromJson(gson.toJson(task.extra), Agent.class);
        agent.assignId(task.id);

        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > agentObj = mapObject.convertValue(agent, Map.class);
        finalDbHandler.addTask(new Task(agent.getId(),TaskStatus.ABORTING, agentObj));

        task = finalDbHandler.getTaskById(id);

        final JobId enqueuedJobId = jobScheduler.<OrchestratingService>enqueue(orchestratingService -> orchestratingService.doCalculationAbort(agent, JobContext.Null));

        return task;
    }

    @Post
    @Produces(MediaType.APPLICATION_JSON)
//    public Task addTask(@PathVariable String jsonData) {

    public Task addTask(@Body String jsonData) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();


        String taskInstanceStringJSON = jsonData;

        Map taskInstanceMap = gson.fromJson(taskInstanceStringJSON, Map.class);

        InputStream computationalModelInput = new ByteArrayInputStream(taskInstanceStringJSON.getBytes());

        InputStream computingResourceInput = new ByteArrayInputStream(taskInstanceStringJSON.getBytes());

        InputStream starterInput = new ByteArrayInputStream(taskInstanceStringJSON.getBytes());

        Agent agent = new Agent();
        agent.assignId((String) taskInstanceMap.get("id"));
        agent.assignComputationalModel(computationalModelInput);
        agent.assignComputingResource(computingResourceInput);
        agent.assignStarter(starterInput);
        agent.assignNumericalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
        agent.assignConverterService(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));


        String agentJson =  gson.toJson(agent);

        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > mapObj = mapObject.convertValue(agent, Map.class);

        Task t = new Task(agent.getId(), TaskStatus.CREATING, mapObj);

        finalDbHandler.addTask(t);
        // Получаем все записи и выводим их на консоль

        System.out.println("The agent (id=" + agent.getId() + ") have prepared a job to be started");

        return t;
    }
}