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

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * Builds response for download exported file path request.
 */
public class GetImportJobExportDownloadResponse extends Response<String> {
    GetImportJobExportDownloadResponse(final String filePath) {
        super(filePath);
    }

    public static GetImportJobExportDownloadResponse getImportJobExportDownloadResponse(final String filePath) {
        return new GetImportJobExportDownloadResponse(filePath);
    }
}
