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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.exception.handling;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.export.api.ExportResponseStatus;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.log.ExportServiceLog;

/**
 * Default implementation of {@link ExportServiceExceptionHandler}.
 */
public class ExportServiceExceptionHandlerImpl implements ExportServiceExceptionHandler {

    @Inject
    private ExportServiceLog logger;

    @Override
    public void handleError(final ExportResponseStatus cmResponse, final Exception exception, final String suggestedSolution, final int statusCode,
            final int errorCode) {
        final String exceptionMessage = logger.logAndCreateErrorMessageFromException(exception);
        cmResponse.setStatusCode(statusCode);
        cmResponse.setStatusMessage(exceptionMessage);
        cmResponse.setSolution(suggestedSolution);
        cmResponse.setErrorCode(errorCode);
    }

    @Override
    public void handleInternalException(final ExportResponseStatus cmResponse, final String statusMessage, final String suggestedSolution,
            final String exceptionOriginatedClass, final int statusCode, final int errorCode) {
        logger.logErrorMessage(statusCode, statusMessage, exceptionOriginatedClass);
        cmResponse.setStatusCode(statusCode);
        cmResponse.setStatusMessage(statusMessage);
        cmResponse.setSolution(suggestedSolution);
        cmResponse.setErrorCode(errorCode);
    }
}
