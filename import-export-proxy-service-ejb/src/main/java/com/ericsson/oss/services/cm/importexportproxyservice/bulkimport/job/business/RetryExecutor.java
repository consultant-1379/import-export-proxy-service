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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.business;

import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.cm.bulkimport.util.FormatLogger;

/**
 * Retry Executor supports executing a function with retries.
 * <p>
 * Uses SFWK's RetryManager under the hood.
 */
public class RetryExecutor {
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(RetryExecutor.class));

    @Inject
    RetryManager retryManager;

    /**
     * Executes function with retries.
     */
    public <T> T execute(final RetryPolicy retryPolicy, final RetriableCommand<T> retriableCommand) {
        try {
            return retryManager.executeCommand(retryPolicy, retriableCommand);
        } catch (final RetriableCommandException e) {
            //Service framework's RetryManagerBean wraps exception into RetriableCommandException which need to be extracted
            LOGGER.debug("Retriable Command Exception", e);
            throw propagate(e.getCause());
        }
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private RuntimeException propagate(final Throwable throwable) {
        propagateIfInstanceOf(throwable, Error.class);
        propagateIfInstanceOf(throwable, RuntimeException.class);
        return new RuntimeException(throwable);
    }

    private <T extends Throwable> void propagateIfInstanceOf(final Throwable throwable, final Class<T> declaredType) throws T {
        if (throwable != null && declaredType.isInstance(throwable)) {
            throw declaredType.cast(throwable);
        }
    }
}
