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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile.GetImportFileResponse.getImportFileResponse;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class for GET Import File for Request.
 */
@Handle(GetImportFileRequest.class)
public class GetImportFileRequestHandler implements RequestHandler<GetImportFileRequest, GetImportFileResponse> {

    @Inject
    @Any
    Instance<RequestValidator<GetImportFileRequest>> validators;

    @Inject
    ImportFileConverter transformer;

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Override
    public GetImportFileResponse handle(final GetImportFileRequest request) {
        final Long jobId = request.getFileId();
        final ImportJob entity = jobPersistence.getImportJob(jobId);
        if (entity == null) {
            return getImportFileResponse(null);
        }
        return getImportFileResponse(transformer.convert(entity));
    }

    @Override
    public Iterable<RequestValidator<GetImportFileRequest>> validators() {
        return validators;
    }
}
