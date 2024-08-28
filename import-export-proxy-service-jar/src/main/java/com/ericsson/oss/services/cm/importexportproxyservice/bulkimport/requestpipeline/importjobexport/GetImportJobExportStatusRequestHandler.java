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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport.GetImportJobExportStatusResponse.getImportJobExportStatusResponse;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.JobExport;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Request handler class to process the get export job status request.
 */
@Handle(GetImportJobExportStatusRequest.class)
public class GetImportJobExportStatusRequestHandler implements RequestHandler<GetImportJobExportStatusRequest, GetImportJobExportStatusResponse> {

    @Inject
    private ImportJobPersistenceService jobPersistenceService;

    @Override
    public GetImportJobExportStatusResponse handle(final GetImportJobExportStatusRequest request) {
        final JobExport jobExport = jobPersistenceService.getJobExport(request.getImportJobId(), request.getJobExportId());
        return getImportJobExportStatusResponse(jobExport == null ? null : jobExport.getStatus());
    }

    @Override
    public Iterable<RequestValidator<GetImportJobExportStatusRequest>> validators() {
        return null;
    }
}
