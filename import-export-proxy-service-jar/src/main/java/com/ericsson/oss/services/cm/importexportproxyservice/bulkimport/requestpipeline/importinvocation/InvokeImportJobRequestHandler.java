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

import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.ENM_LOGGED_IN_USER_NAME_KEY;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ScheduleStatusV2.SCHEDULED;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importinvocation.InvokeImportJobResponse.invokeImportJobResponse;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportJobSpecification;
import com.ericsson.oss.services.cm.bulkimport.execution.UnsyncNodeExecPolicy;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.Schedule;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.JobExecutionPolicyMapper;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.JobValidationPolicyMapper;

/**
 * InvokeImportJobRequest class to handle import job invocation request.
 */
@Handle(InvokeImportJobRequest.class)
public class InvokeImportJobRequestHandler implements RequestHandler<InvokeImportJobRequest, InvokeImportJobResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeImportJobRequestHandler.class);

    @Inject
    @Any
    Instance<RequestValidator<InvokeImportJobRequest>> validators;

    @Inject
    ImportInvocationConverter transformer;

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Inject
    ContextService contextService;

    @Override
    public InvokeImportJobResponse handle(final InvokeImportJobRequest invokeImportJobRequest) {
        final ImportJob importJob = updateImportJob(invokeImportJobRequest);
        jobPersistence.update(importJob);
        return invokeImportJobResponse(transformer.convert(importJob));
    }

    @Override
    public Iterable<RequestValidator<InvokeImportJobRequest>> validators() {
        return validators;
    }

    private ImportJob updateImportJob(final InvokeImportJobRequest invokeImportJobRequest) {
        final ImportJob importJob = jobPersistence.getImportJob(invokeImportJobRequest.getJobId());

        final ImportJobSpecification importJobSpecification = importJob.getJobSpecification();
        importJobSpecification.setJobInvocationFlow(invokeImportJobRequest.getJobInvocationFlow());

        final Boolean isValidateInstances = JobValidationPolicyMapper.isValidateInstances(invokeImportJobRequest.getJobValidationPolicy());
        if (isValidateInstances != null) {
            importJobSpecification.setValidateInstances(isValidateInstances);
        }

        final Boolean isValidateInNode = JobValidationPolicyMapper.isValidateInNode(invokeImportJobRequest.getJobValidationPolicy());
        if (isValidateInNode != null) {
            importJobSpecification.setValidateInNode(isValidateInNode);
        }

        final Boolean isAuditSelected = JobValidationPolicyMapper.isAuditSelected(invokeImportJobRequest.getJobValidationPolicy());
        if (isAuditSelected != null) {
            importJobSpecification.setAuditSelected(isAuditSelected);
        }

        final String continueOnErrorLevel = JobExecutionPolicyMapper.getContinueOnErrorLevel(invokeImportJobRequest.getJobExecutionPolicy());
        if (continueOnErrorLevel != null) {
            importJobSpecification.setContinueOnErrorLevel(continueOnErrorLevel);
        }

        final Boolean isParallel = JobExecutionPolicyMapper.isParallel(invokeImportJobRequest.getJobExecutionPolicy());
        if (isParallel != null) {
            importJobSpecification.setParallelExecution(isParallel);
        }

        final UnsyncNodeExecPolicy unsyncNodeExecPolicy =
                JobExecutionPolicyMapper.getUnsyncNodeExecPolicy(invokeImportJobRequest.getJobExecutionPolicy());
        if (unsyncNodeExecPolicy != null) {
            importJobSpecification.setUnsyncNodeExecPolicy(unsyncNodeExecPolicy);
        }

        if (invokeImportJobRequest.getScheduleTime() != null) {
            final Schedule schedule = new Schedule();
            schedule.setScheduleStatus(SCHEDULED);
            schedule.setScheduleTime(invokeImportJobRequest.getScheduleTime());
            schedule.setScheduledBy((String) contextService.getContextValue(ENM_LOGGED_IN_USER_NAME_KEY));
            importJob.setSchedule(schedule);
            LOGGER.info("Import job with jobId={} is scheduled for invocation at {}", importJob.getId(), invokeImportJobRequest.getScheduleTime());
        }

        return importJob;
    }
}
