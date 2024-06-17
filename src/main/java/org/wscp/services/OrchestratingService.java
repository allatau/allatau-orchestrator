package org.wscp.services;

import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.micronaut.annotations.Recurring;
import org.wscp.workflow_orchestrator.Agent;

public interface OrchestratingService {


    public int aRecurringMonitoringJob(JobContext jobContext);

//    @Job(name = "Doing an EXAMPLE calculation on the remote cluster", retries=1)
//    void doExampleCalculation(String anArgument, JobContext jobContext) throws Exception;

    @Job(name = "Doing an calculation on the remote cluster", retries=1)
    void doCalculation(Agent agent, JobContext jobContext) throws Exception;

    @Job(name = "Aborting an calculation on the remote cluster", retries=1)
    void doCalculationAbort(Agent agent, JobContext jobContext) throws Exception;

    @Job(name = "Checking the calculation on the remote cluster", retries=1)
    void doCalculationCheck(Agent agent, JobContext jobContext) throws Exception;

    @Job(name = "Viewing logs of the calculation on the remote cluster", retries=1)
    void viewCalculationLogs(Agent agent, JobContext jobContext) throws Exception;

    @Job(name = "To archive files of the calculation on the remote cluster", retries=1)
    void doCalculationArchive(Agent agent, JobContext jobContext) throws Exception;

}
