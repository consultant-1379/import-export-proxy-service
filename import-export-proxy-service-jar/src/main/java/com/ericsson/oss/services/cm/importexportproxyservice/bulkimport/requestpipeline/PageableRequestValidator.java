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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage.responseMessage;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors;

/**
 * Validator for Pageable Request.
 */
public class PageableRequestValidator {

    public List<ResponseMessage> validate(final Pageable request) {
        final List<ResponseMessage> validationMessages = new ArrayList<>();
        if (request.getOffset() < 0) {
            validationMessages.add(responseMessage()
                    .error(CmBulkImportServiceErrors.INVALID_OFFSET_PARAMETER, request.getOffset())
                    .build());
        }
        if (request.getLimit() < 0) {
            validationMessages.add(responseMessage()
                    .error(CmBulkImportServiceErrors.INVALID_LIMIT_PARAMETER, request.getLimit())
                    .build());
        }
        return validationMessages;
    }
}
