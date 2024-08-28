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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobadditionalinfo;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors.UNKNOWN_IMPORT_JOB;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage.responseMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.NotFoundException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage;

/**
 * Validator class to validate the input data for get additional import job information.
 */
public class GetAdditionalJobInfoRequestValidator implements RequestValidator<GetAdditionalJobInfoRequest> {

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Override
    public List<ResponseMessage> validate(final GetAdditionalJobInfoRequest request) {
        final Long jobId = request.getJobId();
        final List<ResponseMessage> errors = new ArrayList<>();
        final ImportJob importJob = jobPersistence.getImportJob(jobId);
        errors.addAll(validateJobExistence(importJob, jobId));
        return errors;
    }

    @SuppressWarnings("unchecked")
    private List<ResponseMessage> validateJobExistence(final ImportJob importJob, final long jobId) {
        if (importJob == null) {
            throw new NotFoundException(responseMessage()
                    .error(UNKNOWN_IMPORT_JOB, jobId)
                    .build());
        }
        return Collections.emptyList();
    }

}
