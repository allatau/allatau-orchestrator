package org.wscp.workflow_orchestrator.old;

import org.wscp.workflow_orchestrator.parser.Parser;
import org.wscp.workflow_orchestrator.parser.YMLParser;
import org.wscp.workflow_orchestrator.interpreter.Interpreter;
import org.wscp.workflow_orchestrator.models.ComputingResource;
import org.wscp.workflow_orchestrator.models.Solver;
import org.wscp.workflow_orchestrator.remote_clients.SSHClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class Worker {
    String wcspfilePath = null;

    public Worker(String path) {
        this.wcspfilePath = path;
    }

    public void doProccess() throws IOException, FileNotFoundException {
        Parser parser = new YMLParser();
        parser.read(this.wcspfilePath);
        parser.parse();
        Map dataMap = parser.getData();
        Interpreter interpreter = new Interpreter(dataMap);
        Solver solver = interpreter.getSolver();
        ComputingResource resource = interpreter.getComputingResource();

        SSHClient ssh = new SSHClient(resource.getUsername(), resource.getPassword(), resource.getHostname(), resource.getPort());
        ssh.startClient();
        String taskFolderName = UUID.randomUUID().toString();
        String calculationsFolder = "~/calculations";
        String taskFolder = calculationsFolder + "/" + taskFolderName;
        ssh.exec(String.format("mkdir -p %s", calculationsFolder));
        ssh.exec(String.format("mkdir %s", taskFolder));
        ssh.exec(String.format("echo \"%s\" > %s/run.sh", solver.getScriptData(), taskFolder));
        ssh.exec(String.format("cd %s && bash %s/run.sh >> %s/logs", taskFolder, taskFolder, taskFolder));
        ssh.exec(String.format("cat %s/logs", taskFolder));
        ssh.stopClient();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Worker worder = new Worker("C:\\dev\\wcsp-test-worker\\samples\\cavity\\wcspfile.yml");
        worder.doProccess();
    }
}
