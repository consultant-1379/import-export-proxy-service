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

package com.ericsson.oss.services.cm.importexportproxyservice.util;

import org.slf4j.Logger;

/**
 * FormatLogger class.
 */
public final class FormatLogger {

    private final Logger log;

    /**
     * FormatLogger default constructor, takes a Logger as a parameter.
     *
     * @param log Logger to be used by FormatLogger
     */
    public FormatLogger(final Logger log) {
        this.log = log;
    }

    /**
     * Return the logger.
     *
     * @return logger
     */
    public Logger getLogger() {
        return log;
    }

    /**
     * Log a message at level INFO according to the specified format and
     * arguments. Methods checks to ensure INFO level is enabled.
     *
     * @param format the format string
     * @param args   the arguments to be logged
     */
    public void info(final String format, final Object... args) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info(format, args);
        }
    }

    /**
     * Log a message at level DEBUG according to the specified format and
     * arguments. Methods checks to ensure DEBUG level is enabled.
     *
     * @param format the format string
     * @param args   the arguments to be logged
     */
    public void debug(final String format, final Object... args) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(format, args);
        }
    }

    /**
     * Log an exception at DEBUG with an accompanying message. Methods checks to ensure DEBUG level is enabled.
     * @param message accompanying message to the exception
     * @param throwable exception to be logged
     */
    public void debug(final String message, final Throwable throwable) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(message, throwable);
        }
    }

    /**
     * Log a message at level TRACE according to the specified format and
     * arguments. Methods checks to ensure TRACE level is enabled.
     *
     * @param format the format string
     * @param args   the arguments to be logged
     */
    public void trace(final String format, final Object... args) {
        if (getLogger().isTraceEnabled()) {
            getLogger().trace(format, args);
        }
    }

    /**
     * Log a message at level WARN according to the specified format and
     * arguments. Methods checks to ensure WARN level is enabled.
     *
     * @param format the format string
     * @param args   the arguments to be logged
     */
    public void warn(final String format, final Object... args) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(format, args);
        }
    }

    /**
     * Log a message at level ERROR according to the specified format and
     * arguments. ERROR logging is always enabled.
     *
     * @param format the format string
     * @param args   the arguments to be logged
     */
    public void error(final String format, final Object... args) {
        getLogger().error(format, args);
    }

    /**
     * Log an exception at level ERROR with an accompanying message. Methods checks to ensure ERROR level is enabled.
     * @param message accompanying message to the exception
     * @param throwable exception to be logged
     */
    public void error(final String message, final Throwable throwable) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(message, throwable);
        }
    }
}