package org.wscp.webapp.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import org.wscp.services.OrchestratingService;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.scheduling.JobScheduler;
import org.wscp.workflow_orchestrator.Agent;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

import static java.time.Instant.now;

@Controller("/orchestrate")
public class OrchestrateController {

    @Inject
    private JobScheduler jobScheduler;

    @Inject
    private OrchestratingService orchestratingService;

//    @Get("/do-example-calculation")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String longRunningJob(@QueryValue(value = "value", defaultValue = "Hello world") String value) {
//        final JobId enqueuedJobId = jobScheduler.<OrchestratingService>enqueue(orchestratingService -> orchestratingService.doExampleCalculation(value));
//        return "Job Enqueued: " + enqueuedJobId;
//    }

    @Get("/do-example-calculation")
    @Produces(MediaType.TEXT_PLAIN)
    public String longRunningJob(@QueryValue(value = "value", defaultValue = "Hello world") String value) {
        UUID uuid = UUID.randomUUID();

        String taskInstanceStringJSON = "{\n" +
                "  \"id\": " + uuid.toString() + ",\n" +
                "  \"computational_model\": null,\n" +
                "  \"computing_resource\": {\n" +
                "    \"hostname\": \"localhost\",\n" +
                "    \"port\": 2022,\n" +
                "    \"username\": \"sshuser\",\n" +
                "    \"password\": \"123\"\n" +
                "  },\n" +
                "  \"solver\": {\n" +
                "    \"software\": \"openfoam\",\n" +
                "    \"version\": \"9\",\n" +
                "    \"script\": [\n" +
                "      \"while :; do date '+%H:%M:%S'; sleep 1; done\",\n" +
//                "      \"source /opt/openfoam9/etc/bashrc\",\n" +
//                "      \"blockMesh\",\n" +
//                "      \"icoFoam\"\n" +
                "    ]\n" +
                "  },\n" +
                "\"numerical_model\": {\n" +
                "    \"payload\": {\n" +
                "      \"L\": {\n" +
                "        \"value\": 1,\n" +
                "        \"type\": \"float\"\n" +
                "      },\n" +
                "      \"U\": {\n" +
                "        \"value\": 1,\n" +
                "        \"type\": \"float\"\n" +
                "      },\n" +
                "      \"nu\": {\n" +
                "        \"value\": 0.01,\n" +
                "        \"type\": \"float\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"geometry\": {\n" +
                "      \"type\": \"OpenFoam\",\n" +
                "      \"source_type\": \"base64\",\n" +
                "      \"source\": \"UEsDBBQAAAAAAI....<BASE64>\"\n" +
                "    }\n" +
                "  },\n" +
                "\"converter_service\": {" +
                "\"resource\": \"https://microservice-zero.vercel.app\"," +
                "\"type\": \"microservice\"" +
                "}" +
                "}";

        Gson gsonn = new GsonBuilder().setPrettyPrinting().create();

        Map taskInstanceMap = gsonn.fromJson(taskInstanceStringJSON, Map.class);

        Agent agent = new Agent();
        agent.assignId((String) taskInstanceMap.get("id"));
        agent.assignComputationalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
        agent.assignStarter(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));

        agent.assignComputingResource(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
        agent.assignNumericalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
        agent.assignConverterService(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//
        final JobId enqueuedJobId = jobScheduler.<OrchestratingService>enqueue(orchestratingService -> orchestratingService.doCalculation(agent, JobContext.Null));
//        final JobId enqueuedJobId = jobScheduler.<OrchestratingService>enqueue(orchestratingService -> orchestratingService.doExampleCalculation("hello", JobContext.Null));
        return "Job Enqueued: " + enqueuedJobId;
    }
}