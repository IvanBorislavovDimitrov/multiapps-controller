package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cloudfoundry.client.lib.domain.CloudBuild;
import org.cloudfoundry.client.lib.domain.CloudBuild.BuildState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sap.cloud.lm.sl.cf.client.lib.domain.RestartParameters;
import com.sap.cloud.lm.sl.cf.core.cf.apps.ApplicationStartupState;
import com.sap.cloud.lm.sl.cf.core.cf.apps.ApplicationStateAction;
import com.sap.cloud.lm.sl.cf.process.Constants;

@RunWith(Parameterized.class)
public class DetermineDesiredStateAchievingActionsStepTest extends DetermineDesiredStateAchievingActionsStepBaseTest {

    @Parameters
    public static List<Object[]> getParameters() throws ParseException {
        return Arrays.asList(new Object[][] {
            // @formatter:off
            // (0)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STOPPED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (1)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STOPPED, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("18-03-2018"), null)), null
            },
            // (2)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (3)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("18-03-2018"), FAKE_ERROR)), null
            },
            // (4)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (5)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (6)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (7)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (8)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.INCONSISTENT, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), IllegalStateException.class
            },
            // (9)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.INCONSISTENT, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), IllegalStateException.class
            },
            // (10)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (11)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (12)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.EXECUTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.EXECUTE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (13)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.EXECUTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.EXECUTE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (14)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.EXECUTE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (15)
            {
                ApplicationStartupState.STOPPED, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.EXECUTE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (16)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STOPPED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (17)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STOPPED, false, Stream.of(ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (18)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (19)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (20)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (21)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (22)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (23)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (24)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.INCONSISTENT, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), IllegalStateException.class
            },
            // (25)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.INCONSISTENT, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), IllegalStateException.class
            },
            // (26)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (27)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (28)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.EXECUTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (29)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.EXECUTED, false, Stream.of(ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (30)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE, ApplicationStateAction.EXECUTE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (31)
            {
                ApplicationStartupState.STARTED, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE, ApplicationStateAction.EXECUTE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (32)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STOPPED, false, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (33)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STOPPED, false, Stream.of(ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (34)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), FAKE_ERROR)), null
            },
            // (35)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (36)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (37)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (38)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (39)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STOP, ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (40)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.INCONSISTENT, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (41)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.INCONSISTENT, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.STAGED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (42)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (43)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (44)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.EXECUTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.EXECUTE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (45)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.EXECUTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.EXECUTE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (46)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.EXECUTE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (47)
            {
                ApplicationStartupState.INCONSISTENT, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.EXECUTE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (48)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STOPPED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (49)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STOPPED, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (50)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (51)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STOPPED, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (52)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (53)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (54)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (55)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.STARTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (56)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.INCONSISTENT, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), IllegalStateException.class
            },
            // (57)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.INCONSISTENT, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), IllegalStateException.class
            },
            // (58)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (59)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.INCONSISTENT, true, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (60)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.EXECUTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (61)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.EXECUTED, false, Collections.emptySet(), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
            // (62)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.EXECUTE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.FAILED, parseDate("21-03-2018"), null)), null
            },
            // (63)
            {
                ApplicationStartupState.EXECUTED, ApplicationStartupState.EXECUTED, true, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.EXECUTE).collect(Collectors.toSet()), Arrays.asList(createCloudBuild(BuildState.FAILED, parseDate("20-03-2018"), null), createCloudBuild(BuildState.STAGED, parseDate("21-03-2018"), null)), null
            },
        });
    } 

    private final Class<? extends Exception> exception;

    public DetermineDesiredStateAchievingActionsStepTest(ApplicationStartupState currentAppState, ApplicationStartupState desiredAppState,
        boolean hasAppChanged, Set<ApplicationStateAction> expectedAppStateActions, List<CloudBuild> cloudBuilds,
        Class<? extends Exception> exception) {
        super(currentAppState, desiredAppState, hasAppChanged, expectedAppStateActions, cloudBuilds);
        this.exception = exception;
    }

    @Test
    public void testExecute() throws Exception {
        if (exception != null) {
            expectedException.expect(exception);
        }
        step.execute(context);
        assertStepFinishedSuccessfully();

        assertEquals(expectedAppStateActions, StepsUtil.getAppStateActionsToExecute(context));
    }

    @Override
    protected void initProperties() {
        context.setVariable(Constants.VAR_VCAP_APP_PROPERTIES_CHANGED, false);
        context.setVariable(Constants.VAR_VCAP_SERVICES_PROPERTIES_CHANGED, false);
        context.setVariable(Constants.VAR_USER_PROPERTIES_CHANGED, false);        
    }

    @Override
    protected RestartParameters getRestartParameters() {
        return new RestartParameters(false, false, false);
    }

    @RunWith(Parameterized.class)
    public static class DetermineAppRestartTest extends DetermineDesiredStateAchievingActionsStepBaseTest {


        @Parameters
        public static List<Object[]> getParameters() throws ParseException {
            return Arrays.asList(new Object[][] {
                // @formatter:off
                // (0)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Collections.emptyList(), true, true, true, true, true, true
                },
                // (1)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Collections.emptyList(), true, false, false, true, false, false
                },
                // (2)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Collections.emptyList(), false, true, false, false, true, false
                },
                // (3)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE, ApplicationStateAction.START, ApplicationStateAction.STOP).collect(Collectors.toSet()), Collections.emptyList(), false, false, true, false, false, true
                },
                // (4)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Collections.emptyList(), false, false, false, false, false, false
                },
                // (5)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Collections.emptyList(), true, false, false, false, false, false
                },
                // (6)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Collections.emptyList(), false, true, false, false, false, false
                },
                // (7)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Collections.emptyList(), false, false, true, false, false, false
                },
                // (8)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Collections.emptyList(), false, false, false, true, false, false
                },
                // (9)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Collections.emptyList(), false, false, false, false, true, false
                },
                // (10)
                {
                    ApplicationStartupState.STARTED, ApplicationStartupState.STARTED, false, Stream.of(ApplicationStateAction.STAGE).collect(Collectors.toSet()), Collections.emptyList(), false, false, false, false, false, true
                },
            });
        }

        private boolean vcapPropertiesChanged;
        private boolean vcapServicesChanged;
        private boolean userPropertiesChanged;
        private boolean shouldRestartOnVcapAppChange;
        private boolean shouldRestartOnVcapServicesChange;
        private boolean shouldRestartOnUserProvidedChange;

        public DetermineAppRestartTest(ApplicationStartupState currentAppState, ApplicationStartupState desiredAppState,
            boolean hasAppChanged, Set<ApplicationStateAction> expectedAppStateActions, List<CloudBuild> cloudBuilds, boolean vcapPropertiesChanged, boolean vcapServicesChanged, boolean userPropertiesChanged , boolean shouldRestartOnVcapAppChange, boolean shouldRestartOnVcapServicesChange,  boolean shouldRestartOnUserProvidedChange) {
            super(currentAppState, desiredAppState, hasAppChanged, expectedAppStateActions, cloudBuilds);
            this.vcapPropertiesChanged = vcapPropertiesChanged;
            this.vcapServicesChanged = vcapServicesChanged;
            this.userPropertiesChanged = userPropertiesChanged;
            this.shouldRestartOnVcapAppChange = shouldRestartOnVcapAppChange;
            this.shouldRestartOnVcapServicesChange = shouldRestartOnVcapServicesChange;
            this.shouldRestartOnUserProvidedChange = shouldRestartOnUserProvidedChange;
        }

        @Test
        public void testParameters() {
            step.execute(context);

            assertEquals(expectedAppStateActions, StepsUtil.getAppStateActionsToExecute(context));
        }

        @Override
        protected DetermineDesiredStateAchievingActionsStep createStep() {
            return new DetermineDesiredStateAchievingActionsStep();
        }

        @Override
        protected void initProperties() {
            context.setVariable(Constants.VAR_VCAP_APP_PROPERTIES_CHANGED, vcapPropertiesChanged);
            context.setVariable(Constants.VAR_VCAP_SERVICES_PROPERTIES_CHANGED, vcapServicesChanged);
            context.setVariable(Constants.VAR_USER_PROPERTIES_CHANGED, userPropertiesChanged);
        }

        @Override
        protected RestartParameters getRestartParameters() {
            return new RestartParameters(shouldRestartOnVcapAppChange, shouldRestartOnVcapServicesChange, shouldRestartOnUserProvidedChange);
        }
    }
}
