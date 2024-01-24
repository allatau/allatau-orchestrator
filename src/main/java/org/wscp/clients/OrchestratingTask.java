package org.wscp.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.wscp.model.TaskStatus;
import org.wscp.server.sqlite.DbHandler;
import org.wscp.model.Task;
import org.wscp.server.sqlite.DbHandlerImpl;
import org.wscp.workflow_orchestrator.Agent;

import java.io.*;
import java.sql.SQLException;
import java.util.Map;


public class OrchestratingTask {
    private static Agent agent;

    public OrchestratingTask(Agent agent) {
        OrchestratingTask.agent = agent;
    }

    public OrchestratingTask() {
        OrchestratingTask.agent = new Agent();
    }

    public static void assignAgent(Agent agent) {
        OrchestratingTask.agent = agent;
    }

    public static void run() throws Exception {

       try {
           agent.process();
       } catch (Exception e) {
           throw(e);
       }

        System.out.println("The agent (id=" + agent.getId() + ") have prepared a job to be started");

//        agent.assignComputationalModel(null);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String agentJson =  gson.toJson(agent);

        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > mapObj = mapObject.convertValue(agent, Map.class);


        try {
            // Создаем экземпляр по работе с БД
            DbHandler dbHandler = DbHandlerImpl.getInstance();

            // Добавляем запись
            dbHandler.addTask(new Task(agent.getId(), TaskStatus.CREATING, mapObj));
            // Получаем все записи и выводим их на консоль

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        String taskInstanceStringJSON = "{\n" +
                "    \"id\": \"a81dddd8-4dc9-11ed-bdc3-0242ac120003\",\n" +
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
                "      \"source /opt/openfoam9/etc/bashrc\",\n" +
                "      \"blockMesh\",\n" +
                "      \"icoFoam\"\n" +
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
//        System.out.println(taskInstanceMap);

//        String computationalModelStringYML = "computational_model:\n" +
//                "  resource: \"C:/Users/deale/dev/wscp/wscp-mvp-platfrom/workflow-orchestrator/samples/case/case.tar.gz\"\n" +
//                "  type: \"file_system\"";

        String computationalModelStringJSON = "{\"computational_model\": {\"resource\": \"https://gist.github.com/dealenx/8caaacdb0bfcbba43d8f16c36dd86376/raw/5c9e50b61ec6b8be048c4e42a4f9ea7dde03dad9/case.tar.gz\",\"type\": \"http\"}}";

        String computationalModelStringYML = "computational_model:\n" +
                "  resource: \"https://gist.github.com/dealenx/8caaacdb0bfcbba43d8f16c36dd86376/raw/5c9e50b61ec6b8be048c4e42a4f9ea7dde03dad9/case.tar.gz\"\n" +
                "  type: \"http\"";


        String computingResourceStringJSON = "{\n" +
                "  \"computing_resource\": {\n" +
                "    \"hostname\": \"localhost\",\n" +
                "    \"port\": 2022,\n" +
                "    \"username\": \"sshuser\",\n" +
                "    \"password\": \"123\"\n" +
                "  }\n" +
                "}";

        String computingResourceStringYML = "computing_resource:\n" +
                "  hostname: \"localhost\"\n" +
                "  port: 2022\n" +
                "  username: \"sshuser\"\n" +
                "  password: \"123\"";


        String starterStringJSON = "{\n" +
                "  \"solver\": {\n" +
                "    \"software\": \"openfoam\",\n" +
                "    \"version\": \"9\",\n" +
                "    \"script\": [\n" +
                "      \"source /opt/openfoam9/etc/bashrc\",\n" +
                "      \"blockMesh\",\n" +
                "      \"icoFoam\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        String starterStringYML = "solver:\n" +
                "  software: \"openfoam\"\n" +
                "  version: \"9\"\n" +
                "  script:\n" +
                "    - \"source /opt/openfoam9/etc/bashrc\"\n" +
                "    - \"blockMesh\"\n" +
                "    - \"icoFoam\"";

        Agent agent = new Agent();
        agent.assignId((String) taskInstanceMap.get("id"));
        agent.assignComputationalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
        agent.assignStarter(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));

        agent.assignComputingResource(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
        agent.assignNumericalModel(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
        agent.assignConverterService(new ByteArrayInputStream(taskInstanceStringJSON.getBytes()));
//        agent.assignParser(new YMLParser());
//        agent.assignComputingResource(new ByteArrayInputStream(computingResourceStringYML.getBytes()));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String agentJson =  gson.toJson(agent);
        System.out.println(agentJson);

        try {
            agent.process();
        } catch (Exception e) {
            throw(e);
        }

//        assignAgent(agent);
//
//        run();
    }
}
