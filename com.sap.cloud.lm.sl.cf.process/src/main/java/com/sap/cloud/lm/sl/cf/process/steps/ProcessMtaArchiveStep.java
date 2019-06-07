package com.sap.cloud.lm.sl.cf.process.steps;

import java.util.Map;
import java.util.function.Function;
import java.util.jar.Manifest;

import javax.inject.Inject;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.dao.OperationDao;
import com.sap.cloud.lm.sl.cf.core.helpers.MtaArchiveElements;
import com.sap.cloud.lm.sl.cf.core.helpers.MtaArchiveHelper;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.persistence.processors.DefaultFileDownloadProcessor;
import com.sap.cloud.lm.sl.cf.persistence.processors.FileDownloadProcessor;
import com.sap.cloud.lm.sl.cf.persistence.services.FileContentProcessor;
import com.sap.cloud.lm.sl.cf.persistence.services.FileStorageException;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.process.util.ProcessConflictPreventer;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.mta.handlers.ArchiveHandler;
import com.sap.cloud.lm.sl.mta.handlers.DescriptorParserFacade;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;

@Component("processMtaArchiveStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProcessMtaArchiveStep extends SyncFlowableStep {

    @Inject
    private OperationDao operationDao;
    @Inject
    private ApplicationConfiguration configuration;

    protected Function<OperationDao, ProcessConflictPreventer> conflictPreventerSupplier = dao -> new ProcessConflictPreventer(
        operationDao);

    @Override
    protected StepPhase executeStep(ExecutionWrapper execution) {
        try {
            getStepLogger().debug(Messages.PROCESSING_MTA_ARCHIVE);
            String appArchiveId = StepsUtil.getRequiredString(execution.getContext(), Constants.PARAM_APP_ARCHIVE_ID);
            processApplicationArchive(execution.getContext(), appArchiveId);
            setMtaIdForProcess(execution.getContext());
            getStepLogger().debug(Messages.MTA_ARCHIVE_PROCESSED);
            return StepPhase.DONE;
        } catch (FileStorageException fse) {
            SLException e = new SLException(fse, fse.getMessage());
            getStepLogger().error(e, Messages.ERROR_PROCESSING_MTA_ARCHIVE);
            throw e;
        } catch (SLException e) {
            getStepLogger().error(e, Messages.ERROR_PROCESSING_MTA_ARCHIVE);
            throw e;
        }
    }

    private void processApplicationArchive(final DelegateExecution context, String appArchiveId) throws FileStorageException {
        FileDownloadProcessor deploymentDescriptorProcessor = new DefaultFileDownloadProcessor(StepsUtil.getSpaceId(context), appArchiveId,
            createDeploymentDescriptorFileContentProcessor(context));
        fileService.processFileContent(deploymentDescriptorProcessor);
        FileDownloadProcessor manifestProcessor = new DefaultFileDownloadProcessor(StepsUtil.getSpaceId(context), appArchiveId,
            createManifestFileContentProcessor(appArchiveId, context));
        fileService.processFileContent(manifestProcessor);
    }

    private FileContentProcessor createDeploymentDescriptorFileContentProcessor(DelegateExecution context) {
        return appArchiveStream -> {
            String descriptorString = ArchiveHandler.getDescriptor(appArchiveStream, configuration.getMaxMtaDescriptorSize());
            DescriptorParserFacade descriptorParserFacade = new DescriptorParserFacade();
            DeploymentDescriptor deploymentDescriptor = descriptorParserFacade.parseDeploymentDescriptor(descriptorString);
            StepsUtil.setDeploymentDescriptor(context, deploymentDescriptor);
        };
    }

    private FileContentProcessor createManifestFileContentProcessor(String appArchiveId, DelegateExecution context) {
        return appArchiveStream -> {
            // Create and initialize helper
            Manifest manifest = ArchiveHandler.getManifest(appArchiveStream, configuration.getMaxManifestSize());
            MtaArchiveHelper helper = getHelper(manifest);
            helper.init();
            getStepLogger().debug("MTA Archive ID: {0}", appArchiveId);
            MtaArchiveElements mtaArchiveElements = new MtaArchiveElements();
            setMtaArchiveModulesInContext(helper, mtaArchiveElements, context);
            setMtaRequiredDependenciesInContext(helper, mtaArchiveElements, context);
            logMtaArchiveResources(helper, mtaArchiveElements);
            StepsUtil.setMtaArchiveElements(context, mtaArchiveElements);
        };
    }

    protected MtaArchiveHelper getHelper(Manifest manifest) {
        return new MtaArchiveHelper(manifest);
    }

    private void setMtaArchiveModulesInContext(MtaArchiveHelper helper, MtaArchiveElements mtaArchiveElements, DelegateExecution context) {
        Map<String, String> mtaArchiveModules = helper.getMtaArchiveModules();
        mtaArchiveModules.forEach(mtaArchiveElements::addModuleFileName);
        getStepLogger().debug("MTA Archive Modules: {0}", mtaArchiveModules.keySet());
        StepsUtil.setMtaArchiveModules(context, mtaArchiveModules.keySet());
    }

    private void setMtaRequiredDependenciesInContext(MtaArchiveHelper helper, MtaArchiveElements mtaArchiveElements,
        DelegateExecution context) {
        Map<String, String> mtaArchiveRequiresDependencies = helper.getMtaRequiresDependencies();
        mtaArchiveRequiresDependencies.forEach(mtaArchiveElements::addRequiredDependencyFileName);
        getStepLogger().debug("MTA Archive Requires: {0}", mtaArchiveRequiresDependencies.keySet());
        StepsUtil.setMtaArchiveElements(context, mtaArchiveElements);
    }

    private void logMtaArchiveResources(MtaArchiveHelper helper, MtaArchiveElements mtaArchiveElements) {
        Map<String, String> mtaArchiveResources = helper.getMtaArchiveResources();
        mtaArchiveResources.forEach(mtaArchiveElements::addResourceFileName);
        getStepLogger().debug("MTA Archive Resources: {0}", mtaArchiveResources.keySet());
    }

    private void setMtaIdForProcess(DelegateExecution context) {
        DeploymentDescriptor deploymentDescriptor = StepsUtil.getDeploymentDescriptor(context);
        String mtaId = deploymentDescriptor.getId();
        context.setVariable(Constants.PARAM_MTA_ID, mtaId);
        conflictPreventerSupplier.apply(operationDao)
            .acquireLock(mtaId, StepsUtil.getSpaceId(context), StepsUtil.getCorrelationId(context));
    }
}
