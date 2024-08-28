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

import com.ericsson.oss.services.cm.export.api.ExportResponseStatus;

/**
 * Exception handling in the export-service.
 */
public interface ExportServiceExceptionHandler {

    /**
     * Handles all exceptions other than {@code ValidationException}.
     *
     * @param cmResponse
     *            cmResponse
     * @param exception
     *            The {@code Exception} to be handled
     * @param suggestedSolution
     *            suggested solution
     * @param statusCode
     *            status code
     * @param errorCode
     *            error code
     */
    void handleError(final ExportResponseStatus cmResponse, final Exception exception, final String suggestedSolution, final int statusCode,
            final int errorCode);

    /**
     * Handles {@code ValidationException} and {@code TopologySearchServiceException}.
     *
     * @param cmResponse
     *            cmResponse
     * @param statusMessage
     *            status message
     * @param suggestedSolution
     *            suggested solution
     * @param exceptionOriginatedClass
     *            class where this exception occurred.
     * @param statusCode
     *            status code
     * @param errorCode
     *            error code
     */
    void handleInternalException(final ExportResponseStatus cmResponse, final String statusMessage, final String suggestedSolution,
            final String exceptionOriginatedClass, final int statusCode, final int errorCode);
}
