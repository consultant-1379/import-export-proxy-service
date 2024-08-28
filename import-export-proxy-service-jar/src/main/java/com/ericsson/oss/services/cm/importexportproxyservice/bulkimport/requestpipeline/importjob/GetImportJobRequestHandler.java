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

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetImportJobResponse.getImportJobResponse;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 *  * Handler class for GET Import File for Request.
 */
@Handle(GetImportJobRequest.class)
public class GetImportJobRequestHandler implements RequestHandler<GetImportJobRequest, GetImportJobResponse> {

    @Inject
    @Any
    Instance<RequestValidator<GetImportJobRequest>> validators;

    @Inject
    ImportJobConverter transformer;

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Override
    public GetImportJobResponse handle(final GetImportJobRequest request) {
        final Long jobId = request.getJobId();
        final ImportJob entity = jobPersistence.getImportJob(jobId);
        if (entity == null) {
            return getImportJobResponse(null);
        }
        return getImportJobResponse(transformer.convert(entity));
    }

    @Override
    public Iterable<RequestValidator<GetImportJobRequest>> validators() {
        return validators;
    }
}
