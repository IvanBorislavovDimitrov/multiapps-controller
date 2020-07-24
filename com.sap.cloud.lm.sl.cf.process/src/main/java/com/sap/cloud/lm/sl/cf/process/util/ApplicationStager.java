package com.sap.cloud.lm.sl.cf.process.util;

import static java.text.MessageFormat.format;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudBuild;
import org.cloudfoundry.client.lib.domain.PackageState;
import org.cloudfoundry.client.lib.domain.UploadToken;
import org.springframework.http.HttpStatus;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.process.Messages;
import com.sap.cloud.lm.sl.cf.process.steps.ProcessContext;
import com.sap.cloud.lm.sl.cf.process.steps.StepPhase;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;

public class ApplicationStager {

    private final ProcessContext context;
    private final StepLogger logger;
    private final CloudControllerClient client;

    public ApplicationStager(ProcessContext context) {
        this.context = context;
        this.logger = context.getStepLogger();
        this.client = context.getControllerClient();
    }

    public StagingState getStagingState() {
        UUID buildGuid = context.getVariable(Variables.BUILD_GUID);
        if (buildGuid == null) {
            return ImmutableStagingState.builder()
                                        .state(PackageState.STAGED)
                                        .build();
        }
        CloudBuild build = getBuild(buildGuid);
        return getStagingState(build);
    }

    private CloudBuild getBuild(UUID buildGuid) {
        try {
            return client.getBuild(buildGuid);
        } catch (CloudOperationException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                checkIfApplicationExists();
            }
            throw e;
        }
    }

    private void checkIfApplicationExists() {
        CloudApplicationExtended app = context.getVariable(Variables.APP_TO_PROCESS);
        // This will produce an exception with a more meaningful message why the build is missing
        client.getApplication(app.getName());
    }

    private StagingState getStagingState(CloudBuild build) {
        PackageState packageState = getBuildState(build);
        ImmutableStagingState.Builder builder = ImmutableStagingState.builder()
                                                                     .state(packageState);
        if (packageState == PackageState.FAILED) {
            builder.error(build.getError());
        }
        return builder.build();
    }

    private PackageState getBuildState(CloudBuild build) {
        switch (build.getState()) {
            case FAILED:
                return PackageState.FAILED;
            case STAGED:
                return PackageState.STAGED;
            case STAGING:
                return PackageState.PENDING;
        }
        throw new IllegalArgumentException("Invalid build state");
    }

    private CloudBuild getLastBuild(List<CloudBuild> builds) {
        return builds.stream()
                     .max(Comparator.comparing(build -> build.getMetadata()
                                                             .getCreatedAt()))
                     .orElse(null);
    }

    public void bindDropletToApplication(UUID appGuid) {
        UUID buildGuid = context.getVariable(Variables.BUILD_GUID);
        client.bindDropletToApp(client.getBuild(buildGuid)
                                      .getDropletInfo()
                                      .getGuid(),
                                appGuid);
    }

    public StepPhase stageApp(CloudApplication app) {
        UploadToken uploadToken = context.getVariable(Variables.UPLOAD_TOKEN);
        if (uploadToken == null) {
            return StepPhase.DONE;
        }
        logger.info(Messages.STAGING_APP, app.getName());
        return createBuild(uploadToken.getPackageGuid());
    }

    private StepPhase createBuild(UUID packageGuid) {
        try {
            context.setVariable(Variables.BUILD_GUID, client.createBuild(packageGuid)
                                                            .getMetadata()
                                                            .getGuid());
        } catch (CloudOperationException e) {
            handleCloudOperationException(e, packageGuid);
        }
        return StepPhase.POLL;
    }

    private void handleCloudOperationException(CloudOperationException e, UUID packageGuid) {
        if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
            logger.info(Messages.BUILD_FOR_PACKAGE_0_ALREADY_EXISTS, packageGuid);
            logger.warn(e, e.getMessage());
            processLastBuild(packageGuid);
            return;
        }
        throw e;
    }

    private void processLastBuild(UUID packageGuid) {
        CloudBuild lastBuild = getLastBuild(client.getBuildsForPackage(packageGuid));
        if (lastBuild == null) {
            throw new CloudOperationException(HttpStatus.NOT_FOUND, format(Messages.NO_BUILDS_FOUND_FOR_PACKAGE, packageGuid));
        }
        context.setVariable(Variables.BUILD_GUID, lastBuild.getMetadata()
                                                           .getGuid());
    }
}
