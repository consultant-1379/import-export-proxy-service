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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationwarnings;

import java.util.List;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Request validator class for import operation warning request.
 */
public class GetAllImportOperationWarningsRequestValidator implements RequestValidator<GetAllImportOperationWarningsRequest> {
    @Override
    public List validate(final GetAllImportOperationWarningsRequest request) {
        return NO_ERRORS;
    }
}
