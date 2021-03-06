package org.cloudfoundry.multiapps.controller.core.security.serialization.masking;

import org.cloudfoundry.multiapps.mta.model.ProvidedDependency;

public class ProvidedDependencyMasker extends AbstractMasker<ProvidedDependency> {

    @Override
    public void mask(ProvidedDependency dependency) {
        maskProperties(dependency);
        maskParameters(dependency);
    }

}
