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

import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.DEFAULT_USER_ID;
import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.EXECUTION_ERROR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.cm.export.api.ExportResponseStatus;

/**
 * {@code CmConfigHandlerLog} implementation for command recording.
 */
@Default
public class ExportServiceLogBean implements ExportServiceLog {
    private static final String COMPONENT_NAME = "export-service component";
    private static final String EXCEPTION_MESSAGE = "Exception thrown in %s";
    private static final String COMMA = ",";
    private static final String NAME_VALUE_FORMAT = "%s=%s" + COMMA;
    private static final String EVENT_TYPE = "eventtype";

    @Inject
    private SystemRecorder systemRecorder;

    @Inject
    private Logger logger;

    @Override
    public void logExportCommand(final String commandString, final ExportResponseStatus cmResponse, final Object... commandParameters) {
        systemRecorder.recordCommand(generateCommandLog(commandString, commandParameters), getCommandPhase(cmResponse.getStatusCode()),
                COMPONENT_NAME, cmResponse.getStatusMessage(), cmResponse.getStatusMessage());
    }

    @Override
    public void logExportCommand(final String commandString, final int statusCode, final String statusMessage, final Object... commandParameters) {
        systemRecorder.recordCommand(generateCommandLog(commandString, commandParameters), getCommandPhase(statusCode),
                COMPONENT_NAME, statusMessage, statusMessage);
    }

    @Override
    public void logErrorMessage(final int errorCode, final String errorMessage, final String resource) {
        logErrorMessage(DEFAULT_USER_ID, errorCode, errorMessage, resource);
    }

    @Override
    public void logErrorMessage(final String userId, final int errorCode, final String errorMessage, final String resource) {
        systemRecorder.recordError(userId, "error code: " + errorCode, ErrorSeverity.WARNING, COMPONENT_NAME, resource, errorMessage);
    }

    @Override
    public void logErrorMessage(final String errorId, final String resource, final Map<String, Object> additionalInformationAttributes) {
        logErrorMessage(DEFAULT_USER_ID, errorId, resource, additionalInformationAttributes);
    }

    @Override
    public void logErrorMessage(final String userId, final String errorId, final String resource,
            final Map<String, Object> additionalInformationAttributes) {
        final String additionalInformation = formatAdditionalInformation(additionalInformationAttributes);
        systemRecorder.recordError(userId, errorId, ErrorSeverity.WARNING, COMPONENT_NAME, resource, additionalInformation);
    }

    @Override
    public String logAndCreateErrorMessageFromException(final Exception exception) {
        return logAndCreateErrorMessageFromException(DEFAULT_USER_ID, exception);
    }

    @Override
    public String logAndCreateErrorMessageFromException(final String userId, final Exception exception) {
        String exInfo = exception.getMessage();
        if ((exInfo == null) || exInfo.isEmpty()) {
            exInfo = "Null exception message. StackTrace: " + Arrays.toString(exception.getStackTrace());
        }
        systemRecorder.recordError(userId, "error code: " + EXECUTION_ERROR, ErrorSeverity.WARNING, COMPONENT_NAME, "", exInfo);
        logger.error("Exception occurred: ", exception);
        return String.format(EXCEPTION_MESSAGE, COMPONENT_NAME);
    }

    @Override
    public void logEvent(final String userId, final String eventType, final String resource,
            final Map<String, Object> additionalInformationAttributes) {
        logEvent(eventType, resource, additionalInformationAttributes);
    }

    @Override
    public void logEvent(final String eventType, final String resource, final Map<String, Object> additionalInformationAttributes) {
        final String additionalInformation = formatAdditionalInformation(additionalInformationAttributes);
        systemRecorder.recordEvent(eventType, EventLevel.COARSE, COMPONENT_NAME, resource, additionalInformation);
    }

    @Override
    public void logEvent(final String eventType, final Map<String, Object> additionalInformationAttributes) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_TYPE, eventType);
        for (final Entry<String, Object> attribute : additionalInformationAttributes.entrySet()) {
            eventData.put(attribute.getKey(), attribute.getValue());
        }
        systemRecorder.recordEventData(eventType, eventData);
    }

    @Override
    public void logEvent(final String userId, final String eventType, final String resource, final String additionalInformation) {
        logEvent(eventType, resource, additionalInformation);
    }

    @Override
    public void logEvent(final String eventType, final String resource, final String additionalInformation) {
        systemRecorder.recordEvent(eventType, EventLevel.COARSE, COMPONENT_NAME, resource, additionalInformation);
    }

    @Override
    public void recordMrExecution(final String mrId) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put("MR", mrId);
        systemRecorder.recordEventData("MR.EXECUTION", eventData);
    }

    /**
     * Generates a string based on the command and its passed parameters to be used for logging purposes.
     *
     * @param commandString
     *            the command to be logged
     * @param commandParameters
     *            the parameters passed to that command
     * @return a string in the format of <code>commandString([commandParameter1,commandParameter2,...])</code>
     */
    protected static String generateCommandLog(final String commandString, final Object... commandParameters) {
        final StringBuilder commandLog = new StringBuilder();
        commandLog.append(commandString);
        commandLog.append("(");
        commandLog.append(Arrays.toString(commandParameters));
        commandLog.append(")");

        return commandLog.toString();
    }

    private static CommandPhase getCommandPhase(final int statusCode) {
        if (statusCode < 0) {
            return CommandPhase.FINISHED_WITH_ERROR;
        }
        return CommandPhase.FINISHED_WITH_SUCCESS;
    }

    private static String formatAdditionalInformation(final Map<String, Object> additionalInformationAttributes) {
        final StringBuilder additionInformation = new StringBuilder();
        for (final Entry<String, Object> attribute : additionalInformationAttributes.entrySet()) {
            additionInformation.append(String.format(NAME_VALUE_FORMAT, attribute.getKey(), attribute.getValue()));
        }
        return removeTrailingComma(additionInformation.toString());
    }

    private static String removeTrailingComma(final String additionalInformation) {
        if (!additionalInformation.isEmpty() && additionalInformation.endsWith(COMMA)) {
            return additionalInformation.substring(0, additionalInformation.length() - 1);
        } else {
            return additionalInformation;
        }
    }
}
