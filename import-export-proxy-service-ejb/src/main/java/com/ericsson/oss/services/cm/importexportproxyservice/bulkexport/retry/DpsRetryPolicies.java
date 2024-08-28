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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.retry;

import java.util.concurrent.TimeUnit;

import javax.ejb.EJBException;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.neo4j.driver.transport.bolt.transaction.exception.GenericBoltDriverException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.config.ExportConfigurationBean;

/**
 * Provide policies to be used for DPS retry mechanism.
 */
public class DpsRetryPolicies {

    private static final int RETRY_ATTEMPTS = 3;
    private static final int WAIT_INTERVAL = 10;

    @Inject
    private ExportConfigurationBean exportConfiguration;

    @SuppressWarnings("unchecked")
    public RetryPolicy getRetryPolicy() {
        final RetryPolicy policy = RetryPolicy.builder()
                .attempts(RETRY_ATTEMPTS)
                .waitInterval(WAIT_INTERVAL, TimeUnit.SECONDS)
                .retryOn(new Class[] { EJBException.class, GenericBoltDriverException.class, DpsPersistenceException.class })
                .build();
        return policy;
    }

    public RuntimeException getException(final Exception e) {
        return propagate(e.getCause());
    }

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
