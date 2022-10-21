package org.cloudfoundry.multiapps.controller.process.steps;

import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudServiceInstanceExtended;
import org.cloudfoundry.multiapps.controller.process.util.ImmutableAsyncJobToAsyncExecutionStateAdapter;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;
import com.sap.cloudfoundry.client.facade.domain.CloudAsyncJob;

public class PollServiceInstanceOperationsExecution implements AsyncExecution {

    @Override
    public AsyncExecutionState execute(ProcessContext context) {
        CloudServiceInstanceExtended serviceInstanceToPoll = context.getVariable(Variables.SERVICE_TO_PROCESS);
        String serviceToProcessJobId = context.getVariable(Variables.SERVICE_TO_PROCESS_JOB_ID);
        CloudControllerClient client = context.getControllerClient();
        CloudAsyncJob asyncJob = client.getAsyncJob(serviceToProcessJobId);
        ImmutableAsyncJobToAsyncExecutionStateAdapter asyncJobToAsyncExecutionStateAdapter = getAsyncJobToAsyncExecutionStateAdapter(context,
                                                                                                                                     serviceInstanceToPoll,
                                                                                                                                     asyncJob);
        return asyncJobToAsyncExecutionStateAdapter.evaluateState(asyncJob);
    }

    private static ImmutableAsyncJobToAsyncExecutionStateAdapter
            getAsyncJobToAsyncExecutionStateAdapter(ProcessContext context, CloudServiceInstanceExtended serviceInstanceToPoll,
                                                    CloudAsyncJob asyncJob) {
        return ImmutableAsyncJobToAsyncExecutionStateAdapter.builder()
                                                            .onCompleteListener(() -> context.getStepLogger()
                                                                                             .info("Service polling operation completed: {0}",
                                                                                                   serviceInstanceToPoll.getName())) // TODO:
                                                                                                                                     // change
                                                                                                                                     // to
                                                                                                                                     // debug
                                                            .onErrorListener(() -> context.getStepLogger()
                                                                                          .error("Service polling operation failed: {0}",
                                                                                                 serviceInstanceToPoll.getName()))
                                                            .onCompleteListener(() -> context.getStepLogger()
                                                                                             .info("Service polling operation in progress: {0}, for service instance: {1}",
                                                                                                   asyncJob,
                                                                                                   serviceInstanceToPoll.getName()))
                                                            .onProcessingListener(() -> context.getStepLogger()
                                                                                               .info("Service polling operation in progress: {0}, for service instance: {1}",
                                                                                                     asyncJob,
                                                                                                     serviceInstanceToPoll.getName()))
                                                            .build();
    }

    @Override
    public String getPollingErrorMessage(ProcessContext context) {
        return "Error while polling service instance";
    }
}
