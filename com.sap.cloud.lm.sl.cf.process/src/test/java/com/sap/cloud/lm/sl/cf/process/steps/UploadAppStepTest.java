package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.domain.CloudPackage;
import org.cloudfoundry.client.lib.domain.ImmutableCloudMetadata;
import org.cloudfoundry.client.lib.domain.ImmutableCloudPackage;
import org.cloudfoundry.client.lib.domain.ImmutableUploadToken;
import org.cloudfoundry.client.lib.domain.Status;
import org.cloudfoundry.client.lib.domain.UploadToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.core.helpers.MtaArchiveElements;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.persistence.services.FileContentProcessor;
import com.sap.cloud.lm.sl.cf.process.Messages;
import com.sap.cloud.lm.sl.cf.process.util.ApplicationArchiveContext;
import com.sap.cloud.lm.sl.cf.process.util.ApplicationArchiveReader;
import com.sap.cloud.lm.sl.cf.process.util.ApplicationZipBuilder;
import com.sap.cloud.lm.sl.cf.process.util.CloudPackagesGetter;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.MapUtil;

public class UploadAppStepTest {

    @Nested
    class UploadAppStepGeneralTest extends SyncFlowableStepTest<UploadAppStep> {

        private static final String APP_NAME = "sample-app-backend";
        private static final String APP_FILE = "web.zip";
        private static final String SPACE = "space";
        private static final String APP_ARCHIVE = "sample-app.mtar";
        private static final String CURRENT_MODULE_DIGEST = "439B99DFFD0583200D5D21F4CD1BF035";
        private static final String NEW_MODULE_DIGEST = "539B99DFFD0583200D5D21F4CD1BF035";
        private final IOException IO_EXCEPTION = new IOException();
        private final CloudOperationException CO_EXCEPTION = new CloudOperationException(HttpStatus.BAD_REQUEST);
        private final UploadToken UPLOAD_TOKEN = ImmutableUploadToken.builder()
                                                                     .packageGuid(UUID.randomUUID())
                                                                     .build();
        private final UUID PACKAGE_GUID = UUID.randomUUID();
        private final MtaArchiveElements mtaArchiveElements = new MtaArchiveElements();
        private final CloudPackagesGetter cloudPackagesGetter = Mockito.mock(CloudPackagesGetter.class);
        @TempDir
        Path tempDir;
        private File appFile;

        @BeforeEach
        public void setUp() throws Exception {
            prepareFileService();
            prepareContext();
        }

        @SuppressWarnings("rawtypes")
        private void prepareFileService() throws Exception {
            appFile = new File(tempDir.toString() + File.separator + APP_FILE);
            if (!appFile.exists()) {
                appFile.createNewFile();
            }
            doAnswer(invocation -> {
                FileContentProcessor contentProcessor = invocation.getArgument(2);
                return contentProcessor.process(null);
            }).when(fileService)
              .processFileContent(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        }

        private void prepareContext() {
            CloudApplicationExtended app = ImmutableCloudApplicationExtended.builder()
                                                                            .name(APP_NAME)
                                                                            .moduleName(APP_NAME)
                                                                            .build();
            context.setVariable(Variables.APP_TO_PROCESS, app);
            context.setVariable(Variables.MODULES_INDEX, 0);
            context.setVariable(Variables.APP_ARCHIVE_ID, APP_ARCHIVE);
            context.setVariable(Variables.SPACE_GUID, SPACE);
            mtaArchiveElements.addModuleFileName(APP_NAME, APP_FILE);
            context.setVariable(Variables.MTA_ARCHIVE_ELEMENTS, mtaArchiveElements);
            context.setVariable(Variables.VCAP_APP_PROPERTIES_CHANGED, false);
            when(configuration.getMaxResourceFileSize()).thenReturn(ApplicationConfiguration.DEFAULT_MAX_RESOURCE_FILE_SIZE);
        }

        @AfterEach
        public void tearDown() {
            FileUtils.deleteQuietly(appFile.getParentFile());
        }

        @Test
        void testSuccessfulUpload() throws Exception {
            prepareClients(null, null, NEW_MODULE_DIGEST);
            step.execute(execution);
            assertEquals(UPLOAD_TOKEN, context.getVariable(Variables.UPLOAD_TOKEN));
            assertEquals(StepPhase.POLL.toString(), getExecutionStatus());
        }

        @Test
        void testFailedUploadWithIOException() throws Exception {
            String expectedIOExceptionMessage = MessageFormat.format(Messages.ERROR_RETRIEVING_MTA_MODULE_CONTENT, APP_FILE);
            prepareClients(expectedIOExceptionMessage, null, NEW_MODULE_DIGEST);
            Exception exception = assertThrows(SLException.class, () -> step.execute(execution));
            assertTrue(exception.getMessage()
                                .contains(expectedIOExceptionMessage));
            assertFalse(appFile.exists());
            assertNull(context.getVariable(Variables.UPLOAD_TOKEN));
            assertEquals(StepPhase.RETRY.toString(), getExecutionStatus());
        }

        @Test
        void testFailedUploadWithCFException() throws Exception {
            String expectedCFExceptionMessage = MessageFormat.format(Messages.CF_ERROR, CO_EXCEPTION.getMessage());
            prepareClients(null, expectedCFExceptionMessage, NEW_MODULE_DIGEST);
            Exception exception = assertThrows(SLException.class, () -> step.execute(execution));
            assertTrue(exception.getMessage()
                                .contains(expectedCFExceptionMessage));
            assertFalse(appFile.exists());
            assertNull(context.getVariable(Variables.UPLOAD_TOKEN));
            assertEquals(StepPhase.RETRY.toString(), getExecutionStatus());
        }

        @Test
        void testWithAvailableValidCloudPackage() throws Exception {
            prepareClients(null, null, CURRENT_MODULE_DIGEST);
            mockCloudPackagesGetter(createCloudPackage(Status.PROCESSING_UPLOAD));
            step.execute(execution);
            UploadToken uploadToken = context.getVariable(Variables.UPLOAD_TOKEN);
            assertEquals(PACKAGE_GUID, uploadToken.getPackageGuid());
            assertEquals(StepPhase.POLL.toString(), getExecutionStatus());
        }

        @Test
        void testWithAvailableFailedLatestPackageAndNonChangedApplicationContent() throws Exception {
            prepareClients(null, null, CURRENT_MODULE_DIGEST);
            mockCloudPackagesGetter(createCloudPackage(Status.FAILED));
            step.execute(execution);
            assertEquals(UPLOAD_TOKEN, context.getVariable(Variables.UPLOAD_TOKEN));
            assertEquals(StepPhase.POLL.toString(), getExecutionStatus());
        }

        @Test
        void testWithAvailableExpiredCloudPackageAndChangedContent() throws Exception {
            prepareClients(null, null, NEW_MODULE_DIGEST);
            mockCloudPackagesGetter(createCloudPackage(Status.EXPIRED));
            step.execute(execution);
            assertEquals(UPLOAD_TOKEN, context.getVariable(Variables.UPLOAD_TOKEN));
            assertEquals(StepPhase.POLL.toString(), getExecutionStatus());
        }

        @Test
        void testWithUnavailableNewerCloudPackageAndUnchangedContent() throws Exception {
            prepareClients(null, null, CURRENT_MODULE_DIGEST);
            step.execute(execution);
            assertNull(context.getVariable(Variables.UPLOAD_TOKEN));
            assertEquals(StepPhase.DONE.toString(), getExecutionStatus());
        }

        private CloudPackage createCloudPackage(Status status) {
            ImmutableCloudMetadata cloudMetadata = ImmutableCloudMetadata.builder()
                                                                         .guid(PACKAGE_GUID)
                                                                         .build();
            return ImmutableCloudPackage.builder()
                                        .metadata(cloudMetadata)
                                        .status(status)
                                        .build();
        }

        private void mockCloudPackagesGetter(CloudPackage cloudPackage) {
            Mockito.when(cloudPackagesGetter.getLatestUnusedPackage(any(), any()))
                   .thenReturn(Optional.of(cloudPackage));
        }

        private void prepareClients(String expectedIOExceptionMessage, String expectedCFExceptionMessage, String applicationDigest)
            throws Exception {
            if (expectedIOExceptionMessage == null && expectedCFExceptionMessage == null) {
                when(client.asyncUploadApplication(eq(APP_NAME), eq(appFile), any())).thenReturn(UPLOAD_TOKEN);
            } else if (expectedIOExceptionMessage != null) {
                when(client.asyncUploadApplication(eq(APP_NAME), eq(appFile), any())).thenThrow(IO_EXCEPTION);
            } else {
                when(client.asyncUploadApplication(eq(APP_NAME), eq(appFile), any())).thenThrow(CO_EXCEPTION);
            }
            CloudApplicationExtended application = createApplication(applicationDigest);
            when(client.getApplication(APP_NAME)).thenReturn(application);
        }

        private CloudApplicationExtended createApplication(String digest) {
            Map<String, Object> deployAttributes = new HashMap<>();
            deployAttributes.put(com.sap.cloud.lm.sl.cf.core.Constants.ATTR_APP_CONTENT_DIGEST, digest);
            return ImmutableCloudApplicationExtended.builder()
                                                    .metadata(ImmutableCloudMetadata.builder()
                                                                                    .guid(UUID.randomUUID())
                                                                                    .build())
                                                    .name(UploadAppStepGeneralTest.APP_NAME)
                                                    .moduleName(UploadAppStepGeneralTest.APP_NAME)
                                                    .putEnv(com.sap.cloud.lm.sl.cf.core.Constants.ENV_DEPLOY_ATTRIBUTES,
                                                            JsonUtil.toJson(deployAttributes))
                                                    .build();
        }

        @Override
        protected UploadAppStep createStep() {
            return new UploadAppStepMock();
        }

        private class UploadAppStepMock extends UploadAppStep {

            public UploadAppStepMock() {
                applicationArchiveReader = getApplicationArchiveReader();
                applicationZipBuilder = getApplicationZipBuilder(applicationArchiveReader);
                cloudPackagesGetter = UploadAppStepGeneralTest.this.cloudPackagesGetter;
            }

            @Override
            protected ApplicationArchiveContext createApplicationArchiveContext(InputStream appArchiveStream, String fileName,
                                                                                long maxSize) {
                return super.createApplicationArchiveContext(getClass().getResourceAsStream(APP_ARCHIVE), fileName, maxSize);
            }

            private ApplicationArchiveReader getApplicationArchiveReader() {
                return new ApplicationArchiveReader();
            }

            private ApplicationZipBuilder getApplicationZipBuilder(ApplicationArchiveReader applicationArchiveReader) {
                return new ApplicationZipBuilder(applicationArchiveReader) {
                    @Override
                    protected Path createTempFile() {
                        return appFile.toPath();
                    }
                };
            }

        }

    }

    @Nested
    class UploadAppStepTimeoutTest extends SyncFlowableStepTest<UploadAppStep> {

        private static final String APP_NAME = "sample-app-backend";

        @BeforeEach
        public void prepareContext() {
            context.setVariable(Variables.MODULES_INDEX, 0);
            step.initializeStepLogger(execution);
        }

        @Test
        void testGetTimeoutWithoutAppParameter() {
            CloudApplicationExtended app = ImmutableCloudApplicationExtended.builder()
                                                                            .name(APP_NAME)
                                                                            .build();

            testGetTimeout(app, UploadAppStep.DEFAULT_APP_UPLOAD_TIMEOUT);
        }

        @Test
        void testGetTimeoutWithAppParameter() {
            CloudApplicationExtended app = ImmutableCloudApplicationExtended.builder()
                                                                            .name(APP_NAME)
                                                                            .env(MapUtil.asMap(com.sap.cloud.lm.sl.cf.core.Constants.ENV_DEPLOY_ATTRIBUTES,
                                                                                               "{\"upload-timeout\":1800}"))
                                                                            .build();

            testGetTimeout(app, 1800);
        }

        private void testGetTimeout(CloudApplicationExtended app, int expectedUploadTimeout) {
            context.setVariable(Variables.APP_TO_PROCESS, app);

            int uploadTimeout = step.getTimeout(context);
            assertEquals(expectedUploadTimeout, uploadTimeout);
        }

        @Override
        protected UploadAppStep createStep() {
            return new UploadAppStep();
        }

    }

    @Nested
    class UploadAppStepWithoutFileNameTest extends SyncFlowableStepTest<UploadAppStep> {
        private static final String SPACE = "space";
        private static final String APP_NAME = "simple-app";
        private static final String APP_ARCHIVE = "sample-app.mtar";

        @BeforeEach
        public void setUp() {
            prepareContext();
        }

        private void prepareContext() {
            // module name must be null
            CloudApplicationExtended app = ImmutableCloudApplicationExtended.builder()
                                                                            .name(APP_NAME)
                                                                            .build();
            context.setVariable(Variables.APP_TO_PROCESS, app);
            context.setVariable(Variables.MODULES_INDEX, 0);
            context.setVariable(Variables.APP_ARCHIVE_ID, APP_ARCHIVE);
            context.setVariable(Variables.SPACE_GUID, SPACE);
            MtaArchiveElements mtaArchiveElements = new MtaArchiveElements();
            mtaArchiveElements.addModuleFileName(APP_NAME, APP_NAME);
            context.setVariable(Variables.MTA_ARCHIVE_ELEMENTS, mtaArchiveElements);
        }

        @Test
        void testWithMissingFileNameMustReturnDone() {
            step.execute(execution);
            assertStepFinishedSuccessfully();
        }

        @Override
        protected UploadAppStep createStep() {
            return new UploadAppStep();
        }

    }

}
