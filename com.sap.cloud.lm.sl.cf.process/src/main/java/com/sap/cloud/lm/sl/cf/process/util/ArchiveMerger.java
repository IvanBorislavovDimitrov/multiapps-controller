package com.sap.cloud.lm.sl.cf.process.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;

import com.sap.cloud.lm.sl.cf.core.files.FilePartsMerger;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.persistence.model.FileEntry;
import com.sap.cloud.lm.sl.cf.persistence.processors.DefaultFileDownloadProcessor;
import com.sap.cloud.lm.sl.cf.persistence.services.FileContentProcessor;
import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.persistence.services.FileStorageException;
import com.sap.cloud.lm.sl.cf.persistence.util.Configuration;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.process.steps.StepsUtil;
import com.sap.cloud.lm.sl.common.SLException;

public class ArchiveMerger {

    private static final String PART_POSTFIX = ".part.";

    private FileService fileService;
    private StepLogger stepLogger;
    private DelegateExecution context;
    private Logger logger;

    public ArchiveMerger(FileService fileService, StepLogger stepLogger, DelegateExecution context, Logger logger) {
        this.fileService = fileService;
        this.stepLogger = stepLogger;
        this.context = context;
        this.logger = logger;
    }

    public void createArchiveFromParts(List<FileEntry> archivePartEntries, ApplicationConfiguration configuration) {
        List<FileEntry> sortedParts = sort(archivePartEntries);
        String archiveName = getArchiveName(sortedParts.get(0));
        FilePartsMerger archiveMerger = null;
        try {
            archiveMerger = getArchiveMerger(archiveName);
            mergeFileParts(sortedParts, archiveMerger);
            persistMergedArchive(archiveMerger.getMergedFilePath(), context, configuration);
            deleteRemainingFileParts(sortedParts);
        } catch (FileStorageException e) {
            stepLogger.info(Messages.ERROR_MERGING_ARCHIVE);
            throw new SLException(e, Messages.ERROR_PROCESSING_ARCHIVE_PARTS_CONTENT, e.getMessage());
        } catch (IOException e) {
            stepLogger.info(Messages.ERROR_MERGING_ARCHIVE);
            throw new SLException(e, Messages.ERROR_MERGING_ARCHIVE_PARTS, e.getMessage());
        } finally {
            deleteMergedFile(archiveMerger);
        }
    }

    private List<FileEntry> sort(List<FileEntry> archivePartEntries) {
        return archivePartEntries.stream()
            .sorted(Comparator.comparingInt(this::getEntryIndex))
            .collect(Collectors.toList());
    }

    private int getEntryIndex(FileEntry fileEntry) {
        return Integer.parseInt(fileEntry.getName()
            .substring(fileEntry.getName()
                .indexOf(PART_POSTFIX) + PART_POSTFIX.length()));
    }

    private String getArchiveName(FileEntry fileEntry) {
        String fileEntryName = fileEntry.getName();
        return fileEntryName.substring(0, fileEntryName.indexOf(PART_POSTFIX));
    }

    private FilePartsMerger getArchiveMerger(String archiveName) throws IOException {
        return new FilePartsMerger(archiveName);
    }

    private void mergeFileParts(List<FileEntry> sortedParts, FilePartsMerger archiveMerger) throws FileStorageException {
        FileContentProcessor archivePartProcessor = archiveMerger::merge;
        for (FileEntry fileEntry : sortedParts) {
            stepLogger.debug(Messages.MERGING_ARCHIVE_PART, fileEntry.getId(), fileEntry.getName());
            fileService.processFileContent(
                new DefaultFileDownloadProcessor(StepsUtil.getSpaceId(context), fileEntry.getId(), archivePartProcessor));
        }
    }

    private void deleteRemainingFileParts(List<FileEntry> sortedParts) {
        sortedParts.forEach(this::attemptToDeleteFilePart);
    }

    private void attemptToDeleteFilePart(FileEntry fileEntry) {
        try {
            fileService.deleteFile(fileEntry.getSpace(), fileEntry.getId());
        } catch (FileStorageException e) {
            logger.warn(Messages.ERROR_DELETING_ARCHIVE_PARTS_CONTENT, e);
        }
    }

    private void persistMergedArchive(Path archivePath, DelegateExecution context, ApplicationConfiguration configuration)
        throws FileStorageException {
        Configuration fileConfiguration = configuration.getFileConfiguration();
        String name = archivePath.getFileName()
            .toString();
        FileEntry uploadedArchive = fileService.addFile(StepsUtil.getSpaceId(context), StepsUtil.getServiceId(context), name,
            fileConfiguration.getFileUploadProcessor(), archivePath.toFile());
        context.setVariable(Constants.PARAM_APP_ARCHIVE_ID, uploadedArchive.getId());
    }

    private void deleteMergedFile(FilePartsMerger archiveMerger) {
        if (archiveMerger == null) {
            return;
        }
        try {
            Files.deleteIfExists(archiveMerger.getMergedFilePath());
        } catch (IOException e) {
            logger.warn("Merged file not deleted");
        } finally {
            archiveMerger.close();
        }
    }
}