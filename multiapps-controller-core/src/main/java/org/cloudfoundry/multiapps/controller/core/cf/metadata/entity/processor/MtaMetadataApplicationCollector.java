package org.cloudfoundry.multiapps.controller.core.cf.metadata.entity.processor;

import java.util.List;

import javax.inject.Named;

import com.sap.cloudfoundry.client.facade.domain.CloudServiceInstance;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.criteria.MtaMetadataCriteria;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;
import com.sap.cloudfoundry.client.facade.domain.CloudApplication;

@Named
public class MtaMetadataApplicationCollector implements MtaMetadataEntityCollector<CloudApplication> {

    @Override
    public List<CloudApplication> collect(CloudControllerClient client, MtaMetadataCriteria criteria) {
        long start = System.currentTimeMillis();
        List<CloudApplication> applicationsByMetadataLabelSelector = client.getApplicationsByMetadataLabelSelector(criteria.get());
        long end = System.currentTimeMillis();
        System.out.println("TIME FOR APPLICATION: " + (end - start) / 1000.0);
        return applicationsByMetadataLabelSelector;
    }
}
