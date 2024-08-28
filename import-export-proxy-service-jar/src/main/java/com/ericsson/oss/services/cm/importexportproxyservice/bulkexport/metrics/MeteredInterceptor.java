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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.codahale.metrics.Meter;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.ApplicationMetrics;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Metered;

/**
 * CDI interceptor for the {@code Metered} annotation.
 * Intercept invocations of bean methods annotated with {@code Metered} annotation.
 * <p>
 * The CDI implementation automatically registers new Metric instances in the {@code MetricRegistryWrapper} registry
 * resolved for the CDI application.
 * <p>
 * A new metric is registered only the first time the method is intercepted for
 * the same metric name.
 */
@Interceptor
@Metered
public class MeteredInterceptor {

    @Inject
    @ApplicationMetrics
    private MetricRegistryWrapper metricRegistry;

    /**
     * Interceptor method.
     *
     * @param ctx
     *            the invocation context
     * @return the return value of the method intercepted
     * @throws Exception
     *             occurred
     */
    @AroundInvoke
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        Metered metered = ctx.getMethod().getAnnotation(Metered.class);
        if (metered == null) {
            metered = ctx.getTarget().getClass().getAnnotation(Metered.class);
        }

        if (metered != null) {
            final String metricName = MetricNameUtil.forMeteredMethod(ctx.getTarget().getClass(), ctx.getMethod(), metered);
            final Meter meter = metricRegistry.meter(metricName);
            meter.mark();
        }
        return ctx.proceed();
    }
}
