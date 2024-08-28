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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationfailures;

import java.util.List;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Validation class for ImportOperationFailures Request.
 */
public class GetAllImportOperationFailuresRequestValidator implements RequestValidator<GetAllImportOperationFailuresRequest> {

    @Override
    public List validate(final GetAllImportOperationFailuresRequest request) {
        return NO_ERRORS;
    }
}
