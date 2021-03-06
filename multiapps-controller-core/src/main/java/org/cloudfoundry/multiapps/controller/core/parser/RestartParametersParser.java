package org.cloudfoundry.multiapps.controller.core.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.multiapps.controller.client.lib.domain.RestartParameters;
import org.cloudfoundry.multiapps.controller.core.model.SupportedParameters;
import org.cloudfoundry.multiapps.mta.util.PropertiesUtil;

public class RestartParametersParser implements ParametersParser<RestartParameters> {

    @Override
    public RestartParameters parse(List<Map<String, Object>> parametersList) {
        Map<String, Boolean> restartParameters = getRestartParametersFromDescriptor(parametersList);
        boolean shouldRestartOnVcapAppChange = restartParameters.getOrDefault(SupportedParameters.VCAP_APPLICATION_ENV, true);
        boolean shouldRestartOnVcapServicesChange = restartParameters.getOrDefault(SupportedParameters.VCAP_SERVICES_ENV, true);
        boolean shouldRestartOnUserProvidedChange = restartParameters.getOrDefault(SupportedParameters.USER_PROVIDED_ENV, true);
        return new RestartParameters(shouldRestartOnVcapAppChange, shouldRestartOnVcapServicesChange, shouldRestartOnUserProvidedChange);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getRestartParametersFromDescriptor(List<Map<String, Object>> parametersList) {
        return (Map<String, Boolean>) PropertiesUtil.getPropertyValue(parametersList, SupportedParameters.RESTART_ON_ENV_CHANGE,
                                                                      Collections.emptyMap());
    }

}
