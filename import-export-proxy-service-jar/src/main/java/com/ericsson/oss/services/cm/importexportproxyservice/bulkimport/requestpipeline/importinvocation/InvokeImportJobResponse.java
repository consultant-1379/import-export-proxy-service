/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importinvocation;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportInvocationDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * InvokeImportJobRequest class to construct import job invocation request.
 */
@SuppressWarnings("PMD.UseSingleton")
public class InvokeImportJobResponse extends Response<ImportInvocationDto> {

    InvokeImportJobResponse(final ImportInvocationDto invocation) {
        super(invocation);
    }

    public static InvokeImportJobResponse invokeImportJobResponse(final ImportInvocationDto invocation) {
        return new InvokeImportJobResponse(invocation);
    }
}
