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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile.GetAllImportFilesRequest.getAllImportFilesRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile.GetImportFileRequest.getImportFileRequest;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportFileDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestPipeline;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile.GetAllImportFilesRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile.GetImportFileRequest;

/**
 * CmBulkImportFileService class to create import file.
 */
public class CmBulkImportFileService {
    @Inject
    RequestPipeline requestPipeline;

    public Response<ImportFileDto> getFile(final Long fileId) {
        final GetImportFileRequest request = getImportFileRequest(fileId);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportFileDto> getAllFiles(final Criteria<ImportFileDto> criteria) {
        final GetAllImportFilesRequest request = getAllImportFilesRequest(criteria);
        return requestPipeline.process(request);
    }
}
