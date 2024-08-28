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

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport.GetImportJobExportDownloadResponse.getImportJobExportDownloadResponse;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.JobExport;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Request handler class to process file path retrieval for downloading exported file.
 */
@Handle(GetImportJobExportDownloadRequest.class)
public class GetImportJobExportDownloadRequestHandler implements RequestHandler<GetImportJobExportDownloadRequest,
        GetImportJobExportDownloadResponse> {
    @Inject
    private ImportJobPersistenceService jobPersistenceService;

    @Override
    public GetImportJobExportDownloadResponse handle(final GetImportJobExportDownloadRequest request) {
        final JobExport jobExport = jobPersistenceService.getJobExport(request.getImportJobId(), request.getJobExportId());
        return getImportJobExportDownloadResponse(jobExport == null ? null : jobExport.getFilePath());
    }

    @Override
    public Iterable<RequestValidator<GetImportJobExportDownloadRequest>> validators() {
        return null;
    }
}
