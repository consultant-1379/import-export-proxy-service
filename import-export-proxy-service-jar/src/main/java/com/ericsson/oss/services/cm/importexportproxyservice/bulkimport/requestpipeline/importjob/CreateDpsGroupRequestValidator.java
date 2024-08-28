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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import static java.util.Arrays.asList;

import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus.CANCELLED;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus.CREATED;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus.PARSED;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus.PARSING;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors.CREATE_GROUP_JOB_STATUS_MISMATCH;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors.UNKNOWN_IMPORT_JOB;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage.responseMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.NotFoundException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage;

/**
 * Validation class for Create Dps Group.
 */
public class CreateDpsGroupRequestValidator implements RequestValidator<CreateDpsGroupRequest> {

    private static final List<JobStatus> INVALID_STATES_FOR_CREATE_GROUP = asList(CANCELLED, CREATED, PARSING, PARSED);

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Override
    public List<ResponseMessage> validate(final CreateDpsGroupRequest request) {
        final Long jobId = request.getJobId();
        final List<ResponseMessage> errors = new ArrayList<>();
        final ImportJob importJob = jobPersistence.getImportJob(jobId);
        errors.addAll(validateJobExistence(importJob, jobId));
        errors.addAll(validateCurrentJobStatus(importJob));
        return errors;
    }

    private List<ResponseMessage> validateJobExistence(final ImportJob importJob, final long jobId) {
        if (importJob == null) {
            throw new NotFoundException(responseMessage()
                    .error(UNKNOWN_IMPORT_JOB, jobId)
                    .build());
        }
        return Collections.emptyList();
    }

    private List<ResponseMessage> validateCurrentJobStatus(final ImportJob importJob) {
        if (INVALID_STATES_FOR_CREATE_GROUP.contains(importJob.getStatus())) {
            return asList(responseMessage()
                    .error(CREATE_GROUP_JOB_STATUS_MISMATCH)
                    .build());
        }
        return Collections.emptyList();
    }
}
