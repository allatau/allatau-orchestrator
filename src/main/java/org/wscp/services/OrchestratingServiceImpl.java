package org.wscp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.micronaut.annotations.Recurring;
import org.jobrunr.scheduling.JobScheduler;
import org.wscp.model.Task;
import org.wscp.model.TaskStatus;
import org.wscp.server.sqlite.DbHandler;
import org.wscp.workflow_orchestrator.Agent;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.jobs.context.JobRunrDashboardLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Singleton
public class OrchestratingServiceImpl implements OrchestratingService {

    private static final Logger LOGGER = new JobRunrDashboardLogger(LoggerFactory.getLogger(OrchestratingServiceImpl.class));

    @Inject
    private  DbHandler finalDbHandler;

    @Inject
    private JobScheduler jobScheduler;


    @Recurring(id = "recurring-monitoring-job", cron = "*/15 * * * * *")
    @Job(name = "Monitor calculations jobs")
    public int aRecurringMonitoringJob(JobContext jobContext) {
        System.out.println("Monitoring at tasks every 15 seconds.");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        int countTasksInvolved = 0;

        List<Task> tasks = finalDbHandler.getAllTasks();
        for (Task task : tasks) {
            Agent agent = gson.fromJson(gson.toJson(task.extra), Agent.class);

            agent.assignId(task.id);
            if(task.status.getTaskStatus().equals(TaskStatus.RUNNING.getTaskStatus())) {

                final JobId enqueuedJobIdForLogs = jobScheduler.<OrchestratingService>enqueue(orchestratingService -> orchestratingService.viewCalculationLogs(agent, JobContext.Null));
                finalDbHandler.recordJobIdToTaskById(enqueuedJobIdForLogs.toString(), agent.getId());

                final JobId enqueuedJobId = jobScheduler.<OrchestratingService>enqueue(orchestratingService -> orchestratingService.doCalculationCheck(agent, JobContext.Null));
                finalDbHandler.recordJobIdToTaskById(enqueuedJobId.toString(), agent.getId());
                LOGGER.info("id: " + agent.getId() + " status: " + task.status);



                countTasksInvolved = countTasksInvolved + 1;
            } else if (task.status.getTaskStatus().equals(TaskStatus.CREATING.getTaskStatus())) {
                final JobId enqueuedJobId = jobScheduler.<OrchestratingService>enqueue(orchestratingService -> orchestratingService.doCalculation(agent, JobContext.Null));
                finalDbHandler.recordJobIdToTaskById(enqueuedJobId.toString(), agent.getId());
                LOGGER.info("id: " + agent.getId() + " status: " + task.status);
                countTasksInvolved = countTasksInvolved + 1;
            }

        }

        if(countTasksInvolved == 0) {

//            final UUID jobId = jobContext.getJobId();
//
//            BackgroundJob.delete(jobId);

        }

        return countTasksInvolved;
    }

    public void doCalculation(Agent agent, JobContext jobContext) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        agent.assignLogger(LOGGER);
//        agent.assignId((String) taskInstanceMap.get("id"));
//        agent.assignComputationalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//        agent.assignStarter(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//
//        agent.assignComputingResource(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//        agent.assignNumericalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//        agent.assignConverterService(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));


        try {
            String agentJson =  gson.toJson(agent);

//            LOGGER.info("Agent Json:");
//            LOGGER.info(agentJson);

            ObjectMapper mapObject = new ObjectMapper();
            Map < String, Object > agentObj = mapObject.convertValue(agent, Map.class);
            finalDbHandler.addTask(new Task(agent.getId(),TaskStatus.RUNNING, agentObj));

            agent.process();



            // Получаем все записи и выводим их на консоль
            LOGGER.info("The agent (id=" + agent.getId() + ") have prepared a job to be started");
        } catch (Exception e) {
            ObjectMapper mapObject = new ObjectMapper();
            Map < String, Object > agentObj = mapObject.convertValue(agent, Map.class);
            finalDbHandler.addTask(new Task(agent.getId(),TaskStatus.FAILED, agentObj));
            throw(e);
        }
    }


    public void doCalculationCheck(Agent agent, JobContext jobContext) throws Exception {
        agent.assignLogger(LOGGER);

        try {
            boolean isTaskСompletion = false;

            agent.assignLogger(LOGGER);

            ObjectMapper mapObject = new ObjectMapper();
            Map < String, Object > agentObj = mapObject.convertValue(agent, Map.class);

            isTaskСompletion = agent.checkСompletion();

            if(isTaskСompletion == true) {

                finalDbHandler.addTask(new Task(agent.getId(),TaskStatus.COMPLETED, agentObj));
                LOGGER.info("id: " + agent.getId() + " status: " + TaskStatus.COMPLETED);
            }


        } catch (Exception e) {
            throw(e);
        }
    }

    public void viewCalculationLogs(Agent agent, JobContext jobContext) throws Exception {
        agent.assignLogger(LOGGER);

        try {

            agent.assignLogger(LOGGER);

            agent.viewLogs();

        } catch (Exception e) {
            throw(e);
        }
    }

    public void doCalculationAbort(Agent agent, JobContext jobContext) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        agent.assignLogger(LOGGER);



        try {
            String agentJson =  gson.toJson(agent);

            ObjectMapper mapObject = new ObjectMapper();
            Map < String, Object > agentObj = mapObject.convertValue(agent, Map.class);
            finalDbHandler.addTask(new Task(agent.getId(),TaskStatus.ABORTING, agentObj));

            LOGGER.info("The agent (id=" + agent.getId() + ") have prepared a job to be killed");

            agent.killProccess();

//            agent.assignLogger(null);
            //        Gson gson = new GsonBuilder().setPrettyPrinting().create();


            // Добавляем запись
            finalDbHandler.addTask(new Task(agent.getId(),TaskStatus.ABORTED, agentObj));
            // Получаем все записи и выводим их на консоль
            LOGGER.info("The agent (id=" + agent.getId() + ") have aborted");


        } catch (Exception e) {
            throw(e);
        }
    }

//    public void doExampleCalculation(String anArgument, JobContext jobContext) throws Exception {
////            Thread.currentThread().interrupt();
////        }
////    }
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//        UUID uuid = UUID.randomUUID();
//
//        String taskInstanceStringJSON = "{\n" +
//                "  \"id\": " + uuid.toString() + ",\n" +
//                "  \"computational_model\": null,\n" +
//                "  \"computing_resource\": {\n" +
//                "    \"hostname\": \"localhost\",\n" +
//                "    \"port\": 2022,\n" +
//                "    \"username\": \"sshuser\",\n" +
//                "    \"password\": \"123\"\n" +
//                "  },\n" +
//                "  \"solver\": {\n" +
//                "    \"software\": \"openfoam\",\n" +
//                "    \"version\": \"9\",\n" +
//                "    \"script\": [\n" +
//                "      \"source /opt/openfoam9/etc/bashrc\",\n" +
//                "      \"blockMesh\",\n" +
//                "      \"icoFoam\"\n" +
//                "    ]\n" +
//                "  },\n" +
//                "\"numerical_model\": {\n" +
//                "    \"payload\": {\n" +
//                "      \"L\": {\n" +
//                "        \"value\": 1,\n" +
//                "        \"type\": \"float\"\n" +
//                "      },\n" +
//                "      \"U\": {\n" +
//                "        \"value\": 1,\n" +
//                "        \"type\": \"float\"\n" +
//                "      },\n" +
//                "      \"nu\": {\n" +
//                "        \"value\": 0.01,\n" +
//                "        \"type\": \"float\"\n" +
//                "      }\n" +
//                "    },\n" +
//                "    \"geometry\": {\n" +
//                "      \"type\": \"OpenFoam\",\n" +
//                "      \"source_type\": \"base64\",\n" +
//                "      \"source\": \"UEsDBBQAAAAAAI....<BASE64>\"\n" +
//                "    }\n" +
//                "  },\n" +
//                "\"converter_service\": {" +
//                "\"resource\": \"https://microservice-zero.vercel.app\"," +
//                "\"type\": \"microservice\"" +
//                "}" +
//                "}";
//
//        Gson gsonn = new GsonBuilder().setPrettyPrinting().create();
//
//        Map taskInstanceMap = gsonn.fromJson(taskInstanceStringJSON, Map.class);
//
//        Agent agent = new Agent();
//        agent.assignLogger(LOGGER);
//        agent.assignId((String) taskInstanceMap.get("id"));
//        agent.assignComputationalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//        agent.assignStarter(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//
//        agent.assignComputingResource(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//        agent.assignNumericalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//        agent.assignConverterService(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//
//
//        try {
//            agent.process();
//
////            agent.assignLogger(null);
//    //        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String agentJson =  gson.toJson(agent);
//
//            ObjectMapper mapObject = new ObjectMapper();
//            Map < String, Object > agentObj = mapObject.convertValue(agent, Map.class);
//
//            // Добавляем запись
//            finalDbHandler.addTask(new Task(agent.getId(),TaskStatus.RUNNING, agentObj));
//            // Получаем все записи и выводим их на консоль
//
//            System.out.println("The agent (id=" + agent.getId() + ") have prepared a job to be started");
//        } catch (Exception e) {
//            throw(e);
//        }
//    }
}
