package org.cloudfoundry.multiapps.controller.core.validators.parameters;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.multiapps.common.ParsingException;
import org.cloudfoundry.multiapps.controller.core.model.SupportedParameters;
import org.cloudfoundry.multiapps.mta.model.Module;
import org.cloudfoundry.multiapps.mta.schema.Element.ElementBuilder;
import org.cloudfoundry.multiapps.mta.schema.ListElement;
import org.cloudfoundry.multiapps.mta.schema.MapElement;
import org.cloudfoundry.multiapps.mta.schema.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TasksValidator implements ParameterValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksValidator.class);

    public static final String TASK_NAME_KEY = "name";
    public static final String TASK_COMMAND_KEY = "command";
    public static final String TASK_MEMORY_KEY = "memory";
    public static final String TASK_DISK_QUOTA_KEY = "disk-quota";

    private static final MapElement TASK = new MapElement();

    static {
        TASK.add(TASK_NAME_KEY, new ElementBuilder().type(String.class)
                                                    .required(true)
                                                    .buildSimple());
        TASK.add(TASK_COMMAND_KEY, new ElementBuilder().type(String.class)
                                                       .required(true)
                                                       .buildSimple());
        TASK.add(TASK_MEMORY_KEY, new ElementBuilder().type(String.class)
                                                      .buildSimple());
        TASK.add(TASK_DISK_QUOTA_KEY, new ElementBuilder().type(String.class)
                                                          .buildSimple());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isValid(Object tasks, final Map<String, Object> context) {
        if (!(tasks instanceof List)) {
            return false;
        }
        try {
            new SchemaValidator(new ListElement(TASK)).validate((List<Object>) tasks);
        } catch (ParsingException e) {
            // TODO: If we just return 'false' here, then the real cause of the issue would be lost. Refactor ParameterValidators so that
            // their validate methods throw an exception with a descriptive message, instead of just returning 'true' or 'false'.
            // LMCROSSITXSADEPLOY-237
            LOGGER.error(MessageFormat.format("Error validating tasks: {0}", e.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    public Class<?> getContainerType() {
        return Module.class;
    }

    @Override
    public String getParameterName() {
        return SupportedParameters.TASKS;
    }

}
