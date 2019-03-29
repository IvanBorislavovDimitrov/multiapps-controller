package com.sap.cloud.lm.sl.cf.core.helpers;

import com.sap.cloud.lm.sl.cf.core.Constants;
import com.sap.cloud.lm.sl.cf.core.dao.OperationDtoDao;
import com.sap.cloud.lm.sl.cf.core.dao.filters.OperationFilter;
import com.sap.cloud.lm.sl.cf.core.dto.persistence.OperationDto;
import com.sap.cloud.lm.sl.cf.core.flowable.FlowableFacade;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.ApplicationColor;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.common.ConflictException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.flowable.engine.impl.context.Context;
import org.flowable.engine.runtime.Execution;
import org.flowable.variable.api.history.HistoricVariableInstance;

public class ApplicationColorDetector {

    private static final ApplicationColor COLOR_OF_APPLICATIONS_WITHOUT_SUFFIX = ApplicationColor.BLUE;

    public ApplicationColor detectFirstDeployedApplicationColor(DeployedMta deployedMta, OperationDtoDao operationDtoDao,
        String correlationId, ProgressMessageService progressMessageService, FlowableFacade flowableFacade) {
        if (deployedMta == null) {
            return null;
        }
        ApplicationColor firstApplicationColor = null;
        Date firstApplicationColorCreatedOn = null;
        for (DeployedMtaModule module : deployedMta.getModules()) {
            Date moduleCreatedOn = module.getCreatedOn();
            if (firstApplicationColorCreatedOn == null || firstApplicationColorCreatedOn.after(moduleCreatedOn)) {
                firstApplicationColorCreatedOn = moduleCreatedOn;
                firstApplicationColor = getApplicationColor(module);
            }
        }

        OperationDto currentOperationDto = operationDtoDao.find(correlationId);

        int countOfUnsuccessfulUndeploys = 0;
        OperationDto operationFirstDto = null;
        for (OperationDto operationDto : operationDtoDao.findAll()
            .stream()
            .filter(op -> op.getMtaId()
                .equals(currentOperationDto.getMtaId())
                && !op.getProcessId()
                    .equals(currentOperationDto.getProcessId()))
            .sorted((o1, o2) -> o2.getEndedAt()
                .compareTo(o1.getEndedAt()))
            .collect(Collectors.toList())) {
            if (operationFirstDto == null) {
                operationFirstDto = operationDto;
            }

            List<ProgressMessage> opertaionProgressMessages = progressMessageService.findByProcessId(operationDto.getProcessId());
            boolean errorWhileUndeploy = opertaionProgressMessages.stream()
                .anyMatch(progressMessage -> progressMessage.getType() == ProgressMessageType.ERROR && progressMessage.getTaskId()
                    .equals("undeployAppTask"));
            if (!errorWhileUndeploy) {
                break;
            } else {
                countOfUnsuccessfulUndeploys++;
            }
        }

        List<Execution> executionsByProcessId = flowableFacade.getExecutionsByProcessId(operationFirstDto.getProcessId());
        List<Execution> allProcessExecutions = flowableFacade.getAllProcessExecutions(operationFirstDto.getProcessId());
        
        List<HistoricVariableInstance> list = Context.getProcessEngineConfiguration()
            .getHistoryService()
            .createHistoricVariableInstanceQuery()
            .processInstanceId("5994")
            .variableName("deployedMtaColor")
            .list();

        if (countOfUnsuccessfulUndeploys % 2 != 0) {
            firstApplicationColor = firstApplicationColor.getAlternativeColor();
        }

        return firstApplicationColor;
    }

    public ApplicationColor detectSingularDeployedApplicationColor(DeployedMta deployedMta) {
        if (deployedMta == null) {
            return null;
        }
        ApplicationColor deployedApplicationColor = null;
        for (DeployedMtaModule module : deployedMta.getModules()) {
            ApplicationColor moduleApplicationColor = getApplicationColor(module);
            if (deployedApplicationColor == null) {
                deployedApplicationColor = (moduleApplicationColor);
            }
            if (deployedApplicationColor != moduleApplicationColor) {
                throw new ConflictException(Messages.CONFLICTING_APP_COLORS, deployedMta.getMetadata()
                    .getId());
            }
        }
        return deployedApplicationColor;
    }

    private ApplicationColor getApplicationColor(DeployedMtaModule deployedMtaModule) {
        for (ApplicationColor color : ApplicationColor.values()) {
            if (deployedMtaModule.getAppName()
                .endsWith(color.asSuffix())) {
                return color;
            }
        }
        return COLOR_OF_APPLICATIONS_WITHOUT_SUFFIX;
    }

}
