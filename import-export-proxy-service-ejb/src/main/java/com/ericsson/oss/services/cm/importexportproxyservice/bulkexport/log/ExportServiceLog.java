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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.log;

import java.util.Map;

import com.ericsson.oss.services.cm.export.api.ExportResponseStatus;

/**
 * Command logging.
 */
public interface ExportServiceLog {
    /**
     * Records the command and its status.
     *
     * @param commandString
     *            string representation of the command executed
     * @param cmResponse
     *            status result of the command
     * @param commandParameters
     *            parameters to the command executed
     */
    void logExportCommand(final String commandString, final ExportResponseStatus cmResponse, final Object... commandParameters);

    /**
     * Records the command and its status.
     *
     * @param commandString
     *            string representation of the command executed
     * @param statusCode
     *            status code result of the command
     * @param statusMessage
     *            status message result of the command
     * @param commandParameters
     *            parameters to the command executed
     */
    void logExportCommand(final String commandString, final int statusCode, final String statusMessage, final Object... commandParameters);

    /**
     * Records an error.
     *
     * @param errorCode
     *            error code
     * @param errorMessage
     *            error message to record
     * @param resource
     *            resource to record
     */
    void logErrorMessage(final int errorCode, final String errorMessage, final String resource);

    /**
     * Records an error.
     *
     * @param userId
     *            user ID
     * @param errorCode
     *            error code
     * @param errorMessage
     *            error message to record
     * @param resource
     *            resource to record
     */
    void logErrorMessage(final String userId, final int errorCode, final String errorMessage, final String resource);

    /**
     * Records an error.
     *
     * @param errorId
     *            Unique ID for the type of the error recorded
     * @param resource
     *            the entity directly affected by the event
     * @param additionalInformationAttributes
     *            additional attributes to record
     */
    void logErrorMessage(final String errorId, final String resource, final Map<String, Object> additionalInformationAttributes);

    /**
     * Records an error.
     *
     * @param userId
     *            user ID
     * @param errorId
     *            Unique ID for the type of the error recorded
     * @param resource
     *            the entity directly affected by the event
     * @param additionalInformationAttributes
     *            additional attributes to record
     */
    void logErrorMessage(final String userId, final String errorId, final String resource, final Map<String, Object> additionalInformationAttributes);

    /**
     * Records an error from an exception.
     *
     * @param exception
     *            {@code Exception} with the information to record
     * @return the error message string
     */
    String logAndCreateErrorMessageFromException(final Exception exception);

    /**
     * Records an error from an exception.
     *
     * @param userId
     *            user ID
     * @param exception
     *            {@code Exception} with the information to record
     * @return the error message string
     */
    String logAndCreateErrorMessageFromException(final String userId, final Exception exception);

    /**
     * Records an event.
     *
     * @param eventType
     *            Unique ID for the type of the event recorded
     * @param additionalInformationAttributes
     *            additional attributes to record
     */
    void logEvent(final String eventType, final Map<String, Object> additionalInformationAttributes);

    /**
     * Records an event.
     *
     * @param eventType
     *            Unique ID for the type of the event recorded
     * @param resource
     *            resource to record
     * @param additionalInformationAttributes
     *            additional attributes to record
     */
    void logEvent(final String eventType, final String resource, final Map<String, Object> additionalInformationAttributes);

    /**
     * Records an event.
     *
     * @param userId
     *            user ID
     * @param eventType
     *            Unique ID for the type of the event recorded
     * @param resource
     *            resource to record
     * @param additionalInformationAttributes
     *            additional attributes to record
     */
    void logEvent(final String userId, final String eventType, final String resource, final Map<String, Object> additionalInformationAttributes);

    /**
     * Records an event.
     *
     * @param eventType
     *            Unique ID for the type of the event recorded
     * @param resource
     *            resource to record
     * @param additionalInformation
     *            additional information
     */
    void logEvent(final String eventType, final String resource, final String additionalInformation);

    /**
     * Records an event.
     *
     * @param userId
     *            user ID
     * @param eventType
     *            Unique ID for the type of the event recorded
     * @param resource
     *            resource to record
     * @param additionalInformation
     *            additional information
     */
    void logEvent(final String userId, final String eventType, final String resource, final String additionalInformation);

    /**
     * Records MR execution. Should only be called once for execution of a given MR usecase.
     *
     * @param mrId
     *            The MR ID as given by the "MR-ID" field in Jira, for example "105 65-0334/64788"
     */
    void recordMrExecution(String mrId);
}
