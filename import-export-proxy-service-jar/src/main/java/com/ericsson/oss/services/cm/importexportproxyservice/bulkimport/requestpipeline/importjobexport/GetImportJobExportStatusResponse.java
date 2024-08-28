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
 * Builds response for get export job status request.
 */
public class GetImportJobExportStatusResponse extends Response<String> {
    GetImportJobExportStatusResponse(final String status) {
        super(status);
    }

    public static GetImportJobExportStatusResponse getImportJobExportStatusResponse(final String status) {
        return new GetImportJobExportStatusResponse(status);
    }
}
