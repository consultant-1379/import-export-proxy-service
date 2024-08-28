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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage;

/**
 * Validation exception class.
 */
public class RequestValidationException extends ImportServiceException {
    public RequestValidationException(final List<ResponseMessage> messages) {
        super(messages);
    }

    public RequestValidationException(final ResponseMessage message) {
        super(Collections.singletonList(message));
    }
}
