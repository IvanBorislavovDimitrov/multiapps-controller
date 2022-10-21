package org.cloudfoundry.multiapps.controller.process.util;

import org.cloudfoundry.client.v3.jobs.JobState;
import org.cloudfoundry.multiapps.controller.process.steps.AsyncExecutionState;
import org.immutables.value.Value;

import com.sap.cloudfoundry.client.facade.domain.CloudAsyncJob;

@Value.Immutable
public interface AsyncJobToAsyncExecutionStateAdapter {

    Runnable getOnErrorListener();

    Runnable getOnCompleteListener();

    Runnable getOnProcessingListener();

    Runnable getOnPollingListener();

    default AsyncExecutionState evaluateState(CloudAsyncJob asyncJob) {
        if (asyncJob.getState() == JobState.FAILED) {
            getOnErrorListener().run();
            return AsyncExecutionState.ERROR;
        }
        if (asyncJob.getState() == JobState.COMPLETE) {
            getOnCompleteListener().run();
            return AsyncExecutionState.FINISHED;
        }
        if (asyncJob.getState() == JobState.PROCESSING) {
            getOnProcessingListener().run();
            return AsyncExecutionState.RUNNING;
        }
        if (asyncJob.getState() == JobState.POLLING) {
            getOnPollingListener().run();
            return AsyncExecutionState.RUNNING;
        }
        throw new IllegalStateException("Invalid Job State Provided");
    }

}
