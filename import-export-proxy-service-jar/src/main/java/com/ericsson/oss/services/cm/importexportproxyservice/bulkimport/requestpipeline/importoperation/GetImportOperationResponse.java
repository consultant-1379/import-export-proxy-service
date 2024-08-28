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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * Response object for GET Import Operations.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetImportOperationResponse extends Response<ImportOperationDto> {
    GetImportOperationResponse(final ImportOperationDto importOperation) {
        super(importOperation);
    }

    public static GetImportOperationResponse getImportOperationResponse(final ImportOperationDto importOperation) {
        return new GetImportOperationResponse(importOperation);
    }
}
