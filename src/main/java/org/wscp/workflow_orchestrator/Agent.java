package org.wscp.workflow_orchestrator;

import com.google.gson.Gson;
import org.wscp.workflow_orchestrator.interpreter.Interpreter;
import org.wscp.workflow_orchestrator.models.*;
import org.wscp.workflow_orchestrator.parser.JSONParser;
import org.wscp.workflow_orchestrator.parser.Parser;
import org.wscp.workflow_orchestrator.remote_clients.SFTPClient;
import org.wscp.workflow_orchestrator.remote_clients.SSHClient;
import org.wscp.workflow_orchestrator.remote_clients.MicroserviceHTTPClient;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


interface ComputationalModelImpl {
    void assignComputationalModel(InputStream is);
}

interface ComputingResourceImpl {
    void assignComputingResource(InputStream is);
}

interface StarterImpl {
    void assignStarter(InputStream is);
}

interface ProcessorImpl {
    void process() throws Exception;
}



public class Agent implements ComputationalModelImpl, ComputingResourceImpl, StarterImpl, ProcessorImpl {

    public Solver solver;

    public JobScheduler jobScheduler = null;

    public ComputingResource resource;

    public ComputationalModel computationalModel = null;

    public NumericalModel numericalModel = null;

    public ConverterService converterService = null;

    private String id = UUID.randomUUID().toString();


    private transient Logger logger = null;

//    private InputStream computationalModelInput  = null;

    public Agent() {
        logger = LoggerFactory.getLogger(Agent.class);
    }

    public void assignLogger(Logger logger) {
        this.logger = logger;
    }

    public String getId() {
        return id;
    }

    public void assignId(String id) {
        this.id = id;
    }

    public void assignComputationalModel(InputStream is) {

        assignComputationalModel(is, new JSONParser());

    }

    public void assignComputationalModel(InputStream is, Parser parser) {

        try {
            parser.read(is);
            parser.parse();
            Interpreter interpreter = new Interpreter();
            Map dataMap = parser.getData();
//            System.out.println(dataMap);
            logger.info(dataMap.toString());
            interpreter.interpretComputationalModel(dataMap);

            computationalModel = interpreter.getComputationalModel();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void assignNumericalModel(InputStream is) {

        assignNumericalModel(is, new JSONParser());

    }

    public void assignNumericalModel(InputStream is, Parser parser) {

            try {
                parser.read(is);
                parser.parse();
                Interpreter interpreter = new Interpreter();
                Map dataMap = parser.getData();
//                System.out.println(dataMap);
                logger.info(dataMap.toString());
                interpreter.interpretNumericalModel(dataMap);

                numericalModel = interpreter.getNumericalModel();
            } catch (Exception e) {
                System.out.println(e);
            }

    }

    public void assignComputingResource(InputStream is) {
        assignComputingResource(is, new JSONParser());
    }

    public void assignConverterService(InputStream is) {
        assignConverterService(is, new JSONParser());
    }

    public void assignConverterService(InputStream is, Parser parser) {
        try {
            parser.read(is);
            parser.parse();
            Interpreter interpreter = new Interpreter();
            Map dataMap = parser.getData();
//            System.out.println(dataMap);
            logger.info(dataMap.toString());
            interpreter.interpretConverterService(dataMap);
            converterService = interpreter.getConverterService();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public void assignComputingResource(InputStream is, Parser parser) {
        try {
//            Parser parser = new JSONParser();
            parser.read(is);
            parser.parse();

            Interpreter interpreter = new Interpreter();
            Map dataMap = parser.getData();
//            System.out.println(dataMap);
            logger.info(dataMap.toString());
            interpreter.interpretComputingResource(dataMap);
            resource = interpreter.getComputingResource();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void assignStarter(InputStream is) {
        assignStarter(is, new JSONParser());
    }

    public void assignStarter(InputStream is, Parser parser) {
        try {
            parser.read(is);
            parser.parse();
            Interpreter interpreter = new Interpreter();
            Map dataMap = parser.getData();

            interpreter.interpretSolver(dataMap);
            interpreter.interpretJobScheduler(dataMap);
            solver = interpreter.getSolver();
            jobScheduler = interpreter.getJobScheduler();

        } catch (Exception e) {
//            System.out.println(e);
            logger.info(e.toString());
        }

    }

    public void process() throws Exception {

            InputStream computationalModelInput = null;

//            System.out.println("Computational model: " + computationalModel);
            logger.info("Computational model: " + computationalModel);

            if(Objects.isNull(computationalModel) == false) {
                if(computationalModel.getType().equals("http")) {
                    computationalModelInput = new URL(computationalModel.getResource()).openStream();
                }

                if(computationalModel.getType().equals("file_system")) {
                    computationalModelInput = new BufferedInputStream(new FileInputStream(computationalModel.getResource()));
                }
            } else {
//                System.out.println("Computational model is not assigned");
                logger.info("Computational model is not assigned");

                Gson gson = new Gson();


                String numericalModelJson =  gson.toJson(numericalModel);


//                System.out.println("numericalModelJson: " + numericalModelJson);
                logger.info("numericalModelJson: " + numericalModelJson);

                String microserviceAddress =  converterService.getResource();

                MicroserviceHTTPClient microserviceClient = new MicroserviceHTTPClient();

                String endpoint = MessageFormat.format("{0}/generate", microserviceAddress);
                String path = microserviceClient.generate(endpoint, numericalModelJson);
                computationalModelInput = new URL(path).openStream();
            }


            SSHClient ssh = new SSHClient(resource.getUsername(), resource.getPassword(), resource.getHostname(), resource.getPort());
            ssh.startClient();
            String taskFolderName = this.id;
            String calculationsFolder = "~/calculations";
            String taskFolder = calculationsFolder + "/" + taskFolderName;
            ssh.exec(String.format("mkdir -p %s", calculationsFolder));
            ssh.exec(String.format("mkdir %s", taskFolder));

            String absoluteTaskPath = ssh.exec(String.format("cd %s && pwd", taskFolder));

            absoluteTaskPath = absoluteTaskPath.replace("\n","");

//            System.out.println("absoluteTaskPath: " + absoluteTaskPath);
            logger.info("absoluteTaskPath: " + absoluteTaskPath);

//            try {
//                SFTPClient ftp = new SFTPClient(resource.getHostname(), resource.getPort(), resource.getUsername(), resource.getPassword());
//                ftp.uploadFromStream(absoluteTaskPath, computationalModelInput, "case.tar.gz",  false);
//                ftp.closeChannel();
//            }  catch (Exception e) {
//                System.out.println(e);
//            }
            SFTPClient ftp = new SFTPClient(resource.getHostname(), resource.getPort(), resource.getUsername(), resource.getPassword());
            ftp.uploadFromStream(absoluteTaskPath, computationalModelInput, "case.tar.gz",  false);
            ftp.closeChannel();
            ssh.exec(String.format("cd %s && tar -xf case.tar.gz", taskFolder));


            ssh.exec(String.format("echo \"%s\" > %s/run.sh", "#!/bin/sh\n" + String.format("cd %s \n", absoluteTaskPath) + solver.getScriptData() + String.format("\n touch %s/jobCompleted \n", absoluteTaskPath), taskFolder));
            ssh.exec(String.format("chmod +x %s/run.sh", taskFolder));
            //            System.out.println("jobScheduler: " + jobScheduler);
            logger.info("jobScheduler: " + jobScheduler);
            // RUN SCRIT
            if(Objects.isNull(jobScheduler)) {
                ssh.exec(String.format("cd %s", taskFolder));
                ssh.exec(String.format("nohup %s/run.sh > %s/logs & echo $! > %s/run.pid", taskFolder, taskFolder, taskFolder));
//                String logs = ssh.exec(String.format("cat %s/logs", taskFolder));
//
//                System.out.println("==== logs: ===");
//                System.out.println(logs);
            } else {
                if(jobScheduler.getSoftware().equals("pbs")) {
                    ssh.exec(String.format("cd %s && qsub -o %s/logs -e %s/errors %s/run.sh ", taskFolder, taskFolder, taskFolder, taskFolder));
                }
            }

            ssh.stopClient();

    }

    public boolean checkСompletion() {
        boolean fileExists = false;
        try {
            SSHClient ssh = new SSHClient(resource.getUsername(), resource.getPassword(), resource.getHostname(), resource.getPort());
            ssh.startClient();
            String taskFolderName = this.id;
            String calculationsFolder = "~/calculations";
            String taskFolder = calculationsFolder + "/" + taskFolderName;
            String jobCompletedFile = taskFolder + "/" + "jobCompleted";

            String fileStatus = ssh.exec(String.format("test -f %s/jobCompleted && echo \"exists\"", taskFolder));

            fileStatus = fileStatus.replace("\n","");

            if(fileStatus.equals("exists")) fileExists = true;

//            System.out.println( "is the job completed: " +  fileExists );

            ssh.stopClient();

        } catch (Exception e) {
//            System.out.println(e);
            logger.info(e.toString());
        }

        return fileExists;
    }

    public void viewLogs() {
        try {
            SSHClient ssh = new SSHClient(resource.getUsername(), resource.getPassword(), resource.getHostname(), resource.getPort());
            ssh.startClient();
            String taskFolderName = this.id;
            String calculationsFolder = "~/calculations";
            String taskFolder = calculationsFolder + "/" + taskFolderName;

            String logs = ssh.exec(String.format("cat  %s/logs", taskFolder));

            logger.info("Current log:");
            logger.info('\n'+ logs);

//            String logsLines[] = logs.split("c");
//            logger.info("Current log:");
//
//            for (String logLine : logsLines) {
//                logger.info(logLine);
//            }

            ssh.stopClient();

        } catch (Exception e) {
//            System.out.println(e);
            logger.info(e.toString());
        }
    }

    public boolean killProccess() {
        boolean fileExists = false;
        try {
            SSHClient ssh = new SSHClient(resource.getUsername(), resource.getPassword(), resource.getHostname(), resource.getPort());
            ssh.startClient();
            String taskFolderName = this.id;
            String calculationsFolder = "~/calculations";
            String taskFolder = calculationsFolder + "/" + taskFolderName;

            ssh.exec(String.format("kill -9 `cat %s/run.pid`", taskFolder));

            ssh.stopClient();

        } catch (Exception e) {
//            System.out.println(e);
            logger.info(e.toString());
        }

        return fileExists;
    }
}