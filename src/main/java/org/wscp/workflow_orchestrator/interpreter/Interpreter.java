package org.wscp.workflow_orchestrator.interpreter;

import org.wscp.workflow_orchestrator.models.*;

import java.util.*;

public class Interpreter {

    private Map configDataMapped;
    private Solver solver = new Solver();
    private ComputingResource computingResource= null;
    private ComputationalModel computationalModel = null;

    private ConverterService converterService = null;

    private NumericalModel numericalModel = null;


    private JobScheduler jobScheduler = null;

    public Interpreter(Map configDataMapped) {
        this.configDataMapped = configDataMapped;
        this.doProccess();
    }

    public Interpreter() {

    }

    public void doProccess() {
        this.interpretSolver();
        this.interpretComputingResource();
    }

    private void interpretSolver() {
        solver = new Solver();
        Map solverMapped = (Map) this.configDataMapped.get("solver");

        solver.setSoftware((String) solverMapped.get("software") );
        solver.setVersion((String) solverMapped.get("software") );
        solver.setScript((List<String>) solverMapped.get("script") );
    }

    public void interpretSolver(Map dataMapped) {
        Map solverMapped = (Map) dataMapped.get("solver");
        solver.setSoftware((String) solverMapped.get("software") );
        solver.setVersion((String) solverMapped.get("software") );
        solver.setScript((List<String>) solverMapped.get("script") );
    }

    private void interpretComputationalModel() {
        computationalModel = new ComputationalModel();
        Map solverMapped = (Map) this.configDataMapped.get("computational_model");


        computationalModel.setResource((String) solverMapped.get("resource") );
        computationalModel.setType((String) solverMapped.get("type") );
    }

    public void interpretComputationalModel(Map dataMapped) {

        Map computationalModelMapped = (Map) dataMapped.get("computational_model");

        if(Objects.isNull(computationalModelMapped) == true) {
            System.out.println("Computational model is null");
            return;
        }
        computationalModel = new ComputationalModel();
        computationalModel.setResource((String) computationalModelMapped.get("resource") );
        computationalModel.setType((String) computationalModelMapped.get("type") );
    }

    private void interpretComputingResource() {
        this.computingResource = new ComputingResource();
        Map computingResourceMapped = (Map) this.configDataMapped.get("computing_resource");

        interpretComputingResourceProccess(computingResourceMapped);
    }

    public void interpretComputingResource(Map dataMapped) {
        this.computingResource = new ComputingResource();
        Map computingResourceMapped = (Map) dataMapped.get("computing_resource");
        interpretComputingResourceProccess(computingResourceMapped);
    }

    private void interpretComputingResourceProccess(Map computingResourceMapped) {
        this.computingResource.setHostname((String) computingResourceMapped.get("hostname"));
        this.computingResource.setPort((int) ((Double) computingResourceMapped.get("port")).intValue());

        this.computingResource.setUsername((String) computingResourceMapped.get("username"));
        this.computingResource.setPassword((String) computingResourceMapped.get("password"));
    }


    public void interpretJobScheduler(Map dataMapped) {

        Map jobSchedulerMapped = (Map) dataMapped.get("job_scheduler");
        if(Objects.isNull(jobSchedulerMapped) == false) {
            jobScheduler = new JobScheduler();

            jobScheduler.setSoftware((String) jobSchedulerMapped.get("software") );
        }

    }

    public void interpretConverterService(Map dataMapped) {

        Map converterServiceMapped = (Map) dataMapped.get("converter_service");

        if(Objects.isNull(converterServiceMapped) == true) {
            System.out.println("Service Model is null");
            return;
        }

        converterService = new ConverterService();

        converterService.setResource((String) converterServiceMapped.get("resource"));
        converterService.setType((String) converterServiceMapped.get("type"));
    }

    public void interpretNumericalModel(Map dataMapped) {

        Map numericalModelMapped = (Map) dataMapped.get("numerical_model");

        if(Objects.isNull(numericalModelMapped) == true) {
            System.out.println("Numerical Model is null");
            return;
        }

        numericalModel = new NumericalModel();

        numericalModel.setPayload((Map) numericalModelMapped.get("payload"));

        Map geometryMapped = (Map) numericalModelMapped.get("geometry");
        Geometry geometry = new Geometry();
        geometry.setType((String) geometryMapped.get("type"));
        geometry.setSourceType((String) geometryMapped.get("type_source"));
        geometry.setSource((String) geometryMapped.get("source"));
        numericalModel.setGeometry(geometry);


        numericalModel.setGeometry(geometry);
    }



    public Solver getSolver() {
        return solver;
    }

    public ComputingResource getComputingResource() {
        return computingResource;
    }

    public JobScheduler getJobScheduler() {
        return jobScheduler;
    }

    public ComputationalModel getComputationalModel() { return computationalModel; }

    public ConverterService getConverterService() { return converterService; }

    public NumericalModel getNumericalModel() { return numericalModel; }

}
