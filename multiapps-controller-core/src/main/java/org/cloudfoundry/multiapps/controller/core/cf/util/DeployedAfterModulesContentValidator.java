package org.cloudfoundry.multiapps.controller.core.cf.util;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.cloudfoundry.multiapps.common.ContentException;
import org.cloudfoundry.multiapps.controller.core.Messages;
import org.cloudfoundry.multiapps.mta.model.Module;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;

public class DeployedAfterModulesContentValidator implements ModulesContentValidator {

    private static final String DEFAULT_MESSAGE_DELIMITER = ", ";

    private final CloudControllerClient client;

    public DeployedAfterModulesContentValidator(CloudControllerClient client) {
        this.client = client;
    }

    @Override
    public void validate(List<Module> modules) {
        Set<String> modulesInModelNames = getModuleNames(modules);

        List<String> modulesWithDependenciesNotDeployed = modules.stream()
                                                                 .filter(module -> module.getMajorSchemaVersion() >= 3)
                                                                 .filter(module -> !areModuleDependenciesResolvable(module,
                                                                                                                    modulesInModelNames))
                                                                 .map(Module::getName)
                                                                 .collect(Collectors.toList());

        if (!modulesWithDependenciesNotDeployed.isEmpty()) {
            throw new ContentException(Messages.UNRESOLVED_MODULE_DEPENDENCIES,
                                       String.join(DEFAULT_MESSAGE_DELIMITER, modulesWithDependenciesNotDeployed));
        }
    }

    private boolean areModuleDependenciesResolvable(Module module, Set<String> modulesInModelNames) {
        List<String> moduleDependencies = module.getDeployedAfter();
        if (moduleDependencies == null) {
            return true;
        }
        return moduleDependencies.stream()
                                 .filter(dependency -> !modulesInModelNames.contains(dependency))
                                 .map(dependencyModule -> client.getApplication(dependencyModule, false))
                                 .noneMatch(Objects::isNull);
    }

    private Set<String> getModuleNames(List<Module> modules) {
        return modules.stream()
                      .map(Module::getName)
                      .collect(Collectors.toSet());
    }

}
