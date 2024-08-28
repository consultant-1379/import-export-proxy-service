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

import com.ericsson.oss.services.cm.bulkimport.exception.BulkImportException;

/**
 * Exception class for unidentified/unknown/not handled error condition.
 */
public class UnexpectedErrorException extends BulkImportException {

    private static final long serialVersionUID = 7609543665774000784L;
    private static final int UNEXPECTED_ERROR_ERROR_CODE = 9999;
    private static final String UNEXPECTED_ERROR_MSG =
            "Internal Error:{0}. This is an unhandled system error, please check the error log for more details";

    /**
     * The constructor method.
     *
     * @param detailsMessage
     *            details message provided at origin of exception.
     */
    public UnexpectedErrorException(final String detailsMessage) {
        super(UNEXPECTED_ERROR_ERROR_CODE, formatMessage(UNEXPECTED_ERROR_MSG, detailsMessage));
    }

}
