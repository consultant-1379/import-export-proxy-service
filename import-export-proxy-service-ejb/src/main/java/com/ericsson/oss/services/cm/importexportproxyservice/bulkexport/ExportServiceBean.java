/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport;

import static com.ericsson.oss.services.cm.export.api.BatchExportConstants.CONTEXT_SERVICE_USER_VALUE;
import static com.ericsson.oss.services.cm.export.api.BatchExportConstants.MASTER_EXPORT_JOB;
import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.EXECUTION_ERROR;
import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.INVALID_JOB_ID;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.EXPORT_GENERIC_ERROR;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.EXPORT_JOB_SECURITY_EXCEPTION;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.JOB_ID_NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.batch.operations.JobSecurityException;
import javax.batch.operations.NoSuchJobException;
import javax.batch.operations.NoSuchJobExecutionException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.services.cm.export.api.*;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.persistence.JobPersistenceService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.persistence.JobRepositoryBean;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.exception.handling.ExportServiceExceptionHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.filter.ExportFilterModelHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.log.ExportServiceLog;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.retry.DpsRetryPolicies;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.service.ExportStatusBean;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.validation.ValidationException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.validation.ValidationExportService;

/**
 * Implementation class for {@code ExportServiceBean}. This class provides the implementation for
 * {@link ExportService}.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ExportServiceBean implements ExportService {

    private static final String EXPORT_STATUS = "exportStatus";
    private static final String EXPORT_STATUS_LIST = "exportStatusList";
    private static final String EXPORT_RESULT = "exportResult";
    private static final String EXPORT_FILTER_LIST = "exportFilterList";

    @EServiceRef
    private ProxyExportService exportService;

    @Inject
    private ExportServiceExceptionHandler exceptionHandler;

    @Inject
    private ExportServiceLog exportServiceLogger;

    @Inject
    private JobRepositoryBean jobRepositoryBean;

    @Inject
    private JobPersistenceService jobPersistenceService;

    @Inject
    private ExportFilterModelHandler exportModelHandler;

    @Inject
    private ValidationExportService validationExportService;

    @Inject
    private Logger logger;

    @Inject
    private ExportStatusBean exportStatusBean;

    @Inject
    private ContextService contextService;

    @EServiceRef
    private DataPersistenceService dataPersistenceService;

    @Inject
    private RetryManager retryManager;

    @Inject
    private DpsRetryPolicies retryPolicy;

    @Override
    public ExportIdentifier start(final NodeSearchCriteria nodeSearchCriteria, final ExportFilter exportFilter,
            final String exportType, final String configName) {
        logger.info("Export Service starts with nodeSearchCriteria, exportFilter, exportType, configName");
        return exportService.start(nodeSearchCriteria, exportFilter, exportType, configName);
    }

    @Override
    public ExportIdentifier start(final ExportParameters exportParameters) {
        logger.info("Export Service starts with exportParameters");
        return exportService.start(exportParameters);
    }

    @Override
    public ExportStatus status(final Long jobId) {
        final ExportStatus exportNodesStatus = new ExportStatus();
        try {
            validateIfJobExistsInDatabase(jobId);
            exportNodesStatus.setExportStatus(exportStatus(jobId));
        } catch (final ValidationException validationException) {
            exceptionHandler.handleInternalException(exportNodesStatus, validationException.getMessage(), validationException.solution(),
                    ExportServiceBean.class.getName(), EXECUTION_ERROR, validationException.code());
        } catch (final Exception ex) {
            exceptionHandler.handleError(exportNodesStatus, ex, EXPORT_GENERIC_ERROR.solution(), EXECUTION_ERROR, EXPORT_GENERIC_ERROR.code());
        }
        exportServiceLogger.logExportCommand(EXPORT_STATUS, exportNodesStatus, jobId);
        return exportNodesStatus;
    }

    @Override
    public ExportStatus status(final String jobName) {
        final ExportStatus exportNodesStatus = new ExportStatus();
        try {
            validationExportService.validateJobName(jobName);
            final long jobId = jobRepositoryBean.getJobId(jobName);
            validationExportService.validateJobId(jobId, jobName);
            exportNodesStatus.setExportStatus(exportStatus(jobId));
        } catch (final ValidationException validationException) {
            exceptionHandler.handleInternalException(exportNodesStatus, validationException.getMessage(), validationException.solution(),
                    ExportServiceBean.class.getName(), EXECUTION_ERROR, validationException.code());
        } catch (final Exception ex) {
            exceptionHandler.handleError(exportNodesStatus, ex, EXPORT_GENERIC_ERROR.solution(), EXECUTION_ERROR, EXPORT_GENERIC_ERROR.code());
        }
        exportServiceLogger.logExportCommand(EXPORT_STATUS, exportNodesStatus, jobName);
        return exportNodesStatus;
    }

    @Override
    public ExportResponse result(final Long jobId) {
        ExportResponse exportResponse = new ExportResponse();
        try {
            final JobExecutionEntity jobExecution = validationExportService.validateJobExecution(jobId, MASTER_EXPORT_JOB);
            exportResponse = exportStatusBean.result(jobExecution);
        } catch (final ValidationException validationException) {
            exceptionHandler.handleInternalException(exportResponse, validationException.getMessage(), validationException.solution(),
                    ExportServiceBean.class.getName(), EXECUTION_ERROR, validationException.code());
        } catch (final Exception ex) {
            exceptionHandler.handleError(exportResponse, ex, EXPORT_GENERIC_ERROR.solution(), EXECUTION_ERROR, EXPORT_GENERIC_ERROR.code());
        }
        exportServiceLogger.logExportCommand(EXPORT_RESULT, exportResponse, jobId);
        return exportResponse;
    }

    private ExportJobExecutionEntity exportStatus(final Long jobId) {
        final JobExecutionEntity jobExecution = validationExportService.validateJobExecution(jobId, MASTER_EXPORT_JOB);
        return exportStatusBean.status(jobExecution);
    }

    @Override
    public ExportMetrics metrics(final Long id) {
        logger.info("Export Metrics with JobId");
        return exportService.metrics(id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public ExportStatusList list() {
        final long start = System.nanoTime();
        final ExportStatusList exportList = new ExportStatusList();
        try {
            exportList.setExportList(getExecutionEntities());
        } catch (final JobSecurityException jobSecurityException) {
            exceptionHandler.handleError(exportList, jobSecurityException, EXPORT_JOB_SECURITY_EXCEPTION.solution(), EXECUTION_ERROR,
                    EXPORT_GENERIC_ERROR.code());
        } catch (final Exception ex) {
            exceptionHandler.handleError(exportList, ex, EXPORT_GENERIC_ERROR.solution(), EXECUTION_ERROR, EXPORT_GENERIC_ERROR.code());
        }
        final String statusTime = "Completed in " + (System.nanoTime() - start) / 1.0e9 + " seconds";
        exportServiceLogger.logExportCommand(EXPORT_STATUS_LIST, exportList, statusTime);
        return exportList;
    }

    @Override
    public ExportFilterList listFilters() {
        final long start = System.nanoTime();
        final ExportFilterList exportFilterList = new ExportFilterList();
        try {
            exportFilterList.setFilters(exportModelHandler.getPredefinedExportFilters());
            exportFilterList.setFiltersWithDescriptions(exportModelHandler.getPredefinedExportFiltersWithDescriptions());
        } catch (final Exception ex) {
            exceptionHandler.handleError(exportFilterList, ex, EXPORT_GENERIC_ERROR.solution(), EXECUTION_ERROR, EXPORT_GENERIC_ERROR.code());
        }
        final String statusTime = "duration=" + (System.nanoTime() - start) / 1.0e6 + " ms";
        exportServiceLogger.logExportCommand(EXPORT_FILTER_LIST, exportFilterList, statusTime);
        return exportFilterList;
    }

    private void validateIfJobExistsInDatabase(final Long jobId) {
        final long jobInstanceId = jobRepositoryBean.getJobInstanceIdFromJobId(jobId);
        if (INVALID_JOB_ID.equals(jobInstanceId)) {
            throw validationExportService.createValidationException(JOB_ID_NOT_FOUND, jobId.toString());
        }
    }

    private List<ExportJobExecutionEntity> getExecutionEntities() {
        final List<ExportJobExecutionEntity> executionEntities = new ArrayList<>();
        try {
            // Get job IDs from Postgres
            final Map<Long, ExportJobExecutionEntity> jobExecutionEntities = getJobIdsFromPostgres();
            // Get all ExportJobExecutionEntity objects from all JobOutput POs
            retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<Void>() {
                @Override
                public Void execute(final RetryContext retryContext) throws Exception {
                    try {
                        jobExecutionEntities.putAll(jobPersistenceService.mapJobOutputPosToExportJobExecutionEntities());
                    } catch (final RetriableCommandException e) {
                        logger.error(retryPolicy.getException(e).getMessage());
                        throw retryPolicy.getException(e);
                    }
                    return null;
                }
            });
            for (final Map.Entry<Long, ExportJobExecutionEntity> job : jobExecutionEntities.entrySet()) {
                try {
                    // if the JobOutput PO exist for this job, add this from the list
                    if (job.getValue() != null) {
                        final ExportJobExecutionEntity jobExecutionEntity = job.getValue();
                        // Logic to maintain the backward compatibility for jobs created before this update
                        // if the expected node exported counter is 0 then JobOuput PO was created without the node counters
                        // TODO Remove this logic as soon as the main customers have upgraded ENM
                        if (jobExecutionEntity.getExpectedNodesExported() > 0) {
                            executionEntities.add(jobExecutionEntity);
                        } else {
                            final JobExecutionEntity execution = jobRepositoryBean.getJobExecution(job.getKey());
                            if (isJobExecutionValid(job.getKey())) {
                                executionEntities.add(exportStatusBean.status(execution));
                            }
                        }
                    } else {
                        final JobExecutionEntity execution = jobRepositoryBean.getJobExecution(job.getKey());
                        if (isJobExecutionValid(job.getKey())) {
                            executionEntities.add(exportStatusBean.status(execution));
                        }
                    }
                } catch (final NoSuchJobExecutionException noSuchJobExecutionException) {
                    // TODO This block can be removed once the main customers has upgraded ENM
                    logger.warn("Exception occurred when retrieving job execution.", noSuchJobExecutionException);
                }
            }
        } catch (final NoSuchJobException noSuchJobException) {
            logger.warn("Exception occurred when retrieving jobs list.", noSuchJobException);
        }
        return executionEntities;
    }

    private Map<Long, ExportJobExecutionEntity> getJobIdsFromPostgres() {
        final Map<Long, ExportJobExecutionEntity> jobsMap = new TreeMap<>();
        final List<Long> jobIds = jobRepositoryBean.getJobExecutionIds(MASTER_EXPORT_JOB);
        for (final Long jobId : jobIds) {
            jobsMap.put(jobId, null);
        }
        return jobsMap;
    }

    private boolean isJobExecutionValid(final Long jobId) {
        boolean valid = true;
        try {
            validationExportService.validateJobExecution(jobId, MASTER_EXPORT_JOB);
        } catch (final ValidationException validationException) {
            final String user = contextService.getContextValue(CONTEXT_SERVICE_USER_VALUE);
            final String message = String.format("JobExecution is not valid for job id %s, job will be cleaned up "
                    + "later by scheduled clean up", jobId);
            exportServiceLogger.logErrorMessage(user, JOB_ID_NOT_FOUND.code(), message, "Export Status");
            valid = false;
        }
        return valid;
    }
}