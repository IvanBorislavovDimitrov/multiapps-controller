package org.cloudfoundry.multiapps.controller.shutdown.client.configuration;

import java.text.MessageFormat;
import java.util.UUID;

import org.cloudfoundry.multiapps.common.util.JsonUtil;
import org.cloudfoundry.multiapps.controller.core.configuration.Environment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnvironmentBasedShutdownConfiguration implements ShutdownConfiguration {

    private static final String VCAP_APPLICATION = "VCAP_APPLICATION";
    private static final String CFG_URL = "APPLICATION_URL";
    private static final String CFG_USERNAME = "SHUTDOWN_USERNAME";
    private static final String CFG_PASSWORD = "SHUTDOWN_PASSWORD";

    private static final String ARG_0_IS_NOT_SPECIFIED_IN_APP_ENV = "{0} is not specified in the application's environment.";

    private final Environment environment;

    public EnvironmentBasedShutdownConfiguration() {
        this(new Environment());
    }

    public EnvironmentBasedShutdownConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Override
    public UUID getApplicationGuid() {
        UUID applicationGuid = getVcapApplication().applicationGuid;
        if (applicationGuid == null) {
            throw new IllegalStateException(MessageFormat.format("Could not find application GUID in {0}.", VCAP_APPLICATION));
        }
        return applicationGuid;
    }

    @Override
    public String getApplicationUrl() {
        String multiappsControllerUrl = environment.getString(CFG_URL);
        if (multiappsControllerUrl == null) {
            throw new IllegalStateException(MessageFormat.format(ARG_0_IS_NOT_SPECIFIED_IN_APP_ENV, CFG_URL));
        }
        return multiappsControllerUrl;
    }

    @Override
    public String getCloudControllerUrl() {
        String cloudControllerUrl = getVcapApplication().cloudControllerUrl;
        if (cloudControllerUrl == null) {
            throw new IllegalStateException(MessageFormat.format("Could not find cloud controller URL in {0}.", VCAP_APPLICATION));
        }
        return cloudControllerUrl;
    }

    @Override
    public String getUsername() {
        String username = environment.getString(CFG_USERNAME);
        if (username == null) {
            throw new IllegalStateException(MessageFormat.format(ARG_0_IS_NOT_SPECIFIED_IN_APP_ENV, CFG_USERNAME));
        }
        return username;
    }

    @Override
    public String getPassword() {
        String password = environment.getString(CFG_PASSWORD);
        if (password == null) {
            throw new IllegalStateException(MessageFormat.format(ARG_0_IS_NOT_SPECIFIED_IN_APP_ENV, CFG_PASSWORD));
        }
        return password;
    }

    private VcapApplication getVcapApplication() {
        return JsonUtil.fromJson(environment.getString(VCAP_APPLICATION), VcapApplication.class);
    }

    private static class VcapApplication {

        @JsonProperty("application_id")
        private UUID applicationGuid;
        @JsonProperty("cf_api")
        private String cloudControllerUrl;

    }

}
