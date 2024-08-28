/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importinvocation;

import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.EXECUTION_ERROR;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.EXECUTION_SKIPPED;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.INVALID;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.PARSED;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.VALID;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors.NEITHER_IMPORT_FILE_NOR_OPERATIONS_DEFINED;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage.responseMessage;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.CreateImportJobRequestValidator.validateNoControversialExecutionPolicies;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.CreateImportJobRequestValidator.validateNoControversialValidationPolicies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobInvocationFlow;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.util.FormatLogger;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.BulkCmImportConfiguration;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.RequestValidationException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.persistence.BatchPersistenceService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage;

/**
 * Validate import job invocation request.
 */
public class InvokeImportJobRequestValidator implements RequestValidator<InvokeImportJobRequest> {
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(InvokeImportJobRequestValidator.class));

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Inject
    BatchPersistenceService batchPersistenceService;

    @Inject
    ImportOperationPersistenceService importOperationPersistenceService;

    @Inject
    BulkCmImportConfiguration bulkCmImportConfiguration;

    @Override
    public List<ResponseMessage> validate(final InvokeImportJobRequest request) {
        try {
            final List<ResponseMessage> errors = new ArrayList<>();
            final JobInvocationFlow jobInvocationFlow = request.getJobInvocationFlow();

            errors.addAll(validateInvocationFlowSet(jobInvocationFlow));
            errors.addAll(validateNoControversialExecutionPolicies(request.getJobExecutionPolicy()));
            errors.addAll(validateNoControversialValidationPolicies(request.getJobValidationPolicy()));

            if (errors.size() > 0) {
                return errors;
            }

            final Long jobId = request.getJobId();
            final ImportJob importJob = jobPersistence.getImportJob(jobId);
            validateJobExist(importJob, jobId);
            validateFileOrOperationsExist(importJob);
            validateMinimumFutureDateTimeForScheduling(request.getScheduleTime());
            switch (jobInvocationFlow) {
                case VALIDATE:
                    validateJobStatus(importJob, JobStatus.CREATED, JobStatus.VALIDATED, JobStatus.EXECUTED, JobStatus.PARSED, JobStatus.AUDITED);
                    //in case of re-validate after validation or execution phase is necessary to validate the operation status
                    if (importJob.getStatus().equals(JobStatus.EXECUTED) || importJob.getStatus().equals(JobStatus.VALIDATED)) {
                        validateImportOperationsForReValidate(importJob,
                                PARSED.name(),
                                INVALID.name(),
                                VALID.name(),
                                EXECUTION_ERROR.name()
                        );
                        validateBatchJobStatus(importJob);
                    }
                    break;
                case EXECUTE:
                    validateJobStatus(importJob, JobStatus.VALIDATED, JobStatus.EXECUTED, JobStatus.AUDITED);
                    validateImportOperations(importJob, VALID.name(), EXECUTION_ERROR.name(), EXECUTION_SKIPPED.name());
                    validateBatchJobStatus(importJob);
                    break;
                default:
                    //Noting to do
            }
        } catch (final RequestValidationException e) {
            LOGGER.debug("Import Job Request Validation Exception", e);
            return e.getMessages();
        }
        return new ArrayList<>();
    }

    private static void validateJobExist(final ImportJob importJob, final long jobId) {
        if (importJob == null) {
            throw new RequestValidationException(responseMessage()
                    .error(CmBulkImportServiceErrors.UNKNOWN_IMPORT_JOB, jobId)
                    .build());
        }
    }

    private void validateFileOrOperationsExist(final ImportJob importJob) {
        final List<ImportOperation> importOperations = importOperationPersistenceService.getAllImportOperations(importJob.getId());
        final String fileName = importJob.getJobSpecification().getFileName();
        if (importOperations.isEmpty() && StringUtils.isBlank(fileName)) {
            throw new RequestValidationException(responseMessage()
                    .error(NEITHER_IMPORT_FILE_NOR_OPERATIONS_DEFINED)
                    .build());
        }
    }

    private static List<ResponseMessage> validateInvocationFlowSet(final JobInvocationFlow jobInvocationFlow) {
        final List<ResponseMessage> errors = new ArrayList<>();
        if (jobInvocationFlow == null) {
            errors.add(responseMessage()
                    .error(CmBulkImportServiceErrors.UNSUPPORTED_INVOCATION_FLOW, jobInvocationFlow)
                    .build());
        }
        return errors;
    }

    private static void validateJobStatus(final ImportJob importJob, final JobStatus... jobStatus) {
        if (!Arrays.asList(jobStatus).contains(importJob.getStatus())) {
            throw new RequestValidationException(responseMessage().error(CmBulkImportServiceErrors.JOB_STATUS_MISMATCH).build());
        }
    }

    private void validateImportOperations(final ImportJob importJob, final String... importOperationStatuses) {
        final List<ImportOperation> importOperations = importOperationPersistenceService.getAllImportOperations(importJob.getId(),
                importOperationStatuses);
        if (importOperations.isEmpty()) {
            throw new RequestValidationException(responseMessage().error(CmBulkImportServiceErrors.NO_OPERATIONS_TO_EXECUTE).build());
        }
    }

    private void validateImportOperationsForReValidate(final ImportJob importJob, final String... importOperationStatuses) {
        final List<ImportOperation> importOperations = importOperationPersistenceService.getAllImportOperations(importJob.getId(),
                importOperationStatuses);
        if (importOperations.isEmpty()) {
            throw new RequestValidationException(responseMessage().error(CmBulkImportServiceErrors.NO_OPERATIONS_TO_RE_VALIDATE).build());
        }
    }

    private void validateBatchJobStatus(final ImportJob importJob) {
        final Long jobId = importJob.getId();
        final long executionId = importJob.getJobSpecification().getExecutionId();
        if (!canBatchJobRestarted(executionId)) {
            final String batchJobStatus = getBatchJobStatus(executionId);
            final ResponseMessage responseMessage = responseMessage()
                    .error(CmBulkImportServiceErrors.JOB_STATUS_MISMATCH)
                    .withParameter("jobId", jobId)
                    .withParameter("executionId", executionId)
                    .withParameter("executionStatus", batchJobStatus)
                    .build();
            final RequestValidationException validationException = new RequestValidationException(responseMessage);
            throw validationException;
        }
    }

    private void validateMinimumFutureDateTimeForScheduling(final Date scheduleTime) {
        if (scheduleTime != null && scheduleTime.before(getMinimumFutureDateTimeForScheduling())) {
            throw new RequestValidationException(responseMessage()
                    .error(CmBulkImportServiceErrors.INVALID_SCHEDULE_TIME, bulkCmImportConfiguration.getMinimumDelayForScheduling().toString())
                    .build());
        }
    }

    private Date getMinimumFutureDateTimeForScheduling() {
        return DateUtils.addMinutes(new Date(), bulkCmImportConfiguration.getMinimumDelayForScheduling());
    }

    private boolean canBatchJobRestarted(final long executionId) {
        final String status = getBatchJobStatus(executionId);
        return "STOPPED".equals(status) || "FAILED".equals(status);
    }

    public String getBatchJobStatus(final long executionId) {
        return batchPersistenceService.getBatchJobStatusFromJobId(executionId);
    }

}
