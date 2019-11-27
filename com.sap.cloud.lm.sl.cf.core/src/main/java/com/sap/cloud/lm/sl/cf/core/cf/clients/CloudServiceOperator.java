package com.sap.cloud.lm.sl.cf.core.cf.clients;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.CloudServicePlan;
import org.springframework.http.HttpStatus;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.util.UserMessageLogger;

public abstract class CloudServiceOperator extends CustomControllerClient {

    protected static final String SERVICE_INSTANCES_URL = "/v2/service_instances";
    protected static final String ACCEPTS_INCOMPLETE_TRUE = "?accepts_incomplete=true";
    protected static final String CREATE_SERVICE_URL_ACCEPTS_INCOMPLETE_TRUE = SERVICE_INSTANCES_URL + ACCEPTS_INCOMPLETE_TRUE;
    protected static final String SERVICE_NAME = "name";
    protected static final String SPACE_GUID = "space_guid";
    protected static final String SERVICE_PLAN_GUID = "service_plan_guid";
    protected static final String SERVICE_PARAMETERS = "parameters";
    protected static final String SERVICE_TAGS = "tags";

    protected CloudServiceOperator(RestTemplateFactory restTemplateFactory) {
        super(restTemplateFactory);
    }

    protected CloudServicePlan findPlanForService(CloudControllerClient client, CloudService service, UserMessageLogger userMessageLogger) {
        return findPlanForService(client, service, service.getPlan(), userMessageLogger);
    }

    protected CloudServicePlan findPlanForService(CloudControllerClient client, CloudService service, String newPlan,
                                                  UserMessageLogger userMessageLogger) {
        List<CloudServiceOffering> offerings = getServiceOfferings(client, service);
        CloudServicePlan cloudServicePlan = null;
        for (CloudServiceOffering offering : offerings) {
            for (CloudServicePlan plan : offering.getServicePlans()) {
                if (plan.getName()
                        .equals(newPlan)) {
                    if (userMessageLogger != null) {
                        userMessageLogger.info("Service Name: " + offering.getName() + " Plan ID: " + plan.getMetadata()
                                                                 .getGuid() + " New plan: " + newPlan);
                    }
                    if (cloudServicePlan == null) {
                        cloudServicePlan = plan;
                    }
                }
            }
        }
        if (userMessageLogger != null) {
            userMessageLogger.debug("Service Offerings: " + offerings);
        }
        if (cloudServicePlan != null) {
            return cloudServicePlan;
        }
        throw new CloudOperationException(HttpStatus.NOT_FOUND,
                                          HttpStatus.NOT_FOUND.getReasonPhrase(),
                                          MessageFormat.format(Messages.NO_SERVICE_PLAN_FOUND, service.getName(), newPlan,
                                                               service.getLabel()));
    }

    private List<CloudServiceOffering> getServiceOfferings(CloudControllerClient client, CloudService service) {
        return client.getServiceOfferings()
                     .stream()
                     .filter(offering -> isSameLabel(offering, service.getLabel()))
                     .filter(offering -> isSameVersion(offering, service.getVersion()))
                     .collect(Collectors.toList());
    }

    private boolean isSameLabel(CloudServiceOffering offering, String label) {
        return label.equals(offering.getName());
    }

    private boolean isSameVersion(CloudServiceOffering offering, String version) {
        return version == null || version.equals(offering.getVersion());
    }

}
