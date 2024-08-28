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

import org.slf4j.Logger;

import com.codahale.metrics.Timer;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.ApplicationMetrics;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Timed;

/**
 * CDI interceptor for the {@code Timed} annotation.
 * Intercept invocations of bean methods annotated with {@code Timed} annotation.
 * <p>
 * The CDI implementation automatically registers new Metric instances in the {@code MetricRegistryWrapper} registry
 * resolved for the CDI application.
 * <p>
 * A new metric is registered only the first time the method is intercepted for
 * the same metric name.
 */
@Interceptor
@Timed
public class TimedInterceptor {

    @Inject
    @ApplicationMetrics
    private MetricRegistryWrapper metricRegistryWrapper;

    @Inject
    private Logger logger;

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
        final Timed timedMethod = ctx.getMethod().getAnnotation(Timed.class);
        Timed timedClass = ctx.getTarget().getClass().getAnnotation(Timed.class);

        if (timedClass == null) {
            // Method invoked via proxy
            timedClass = ctx.getMethod().getDeclaringClass().getAnnotation(Timed.class);
        }

        if ((timedMethod != null) && (timedClass != null)) {
            return timeMethodAndClass(ctx, timedMethod, timedClass);
        } else if (timedMethod != null) {
            return timeMethodOrClassOnly(ctx, timedMethod);
        } else if (timedClass != null) {
            return timeMethodOrClassOnly(ctx, timedClass);
        }
        return ctx.proceed();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private Object timeMethodAndClass(final InvocationContext ctx, final Timed timedMethod, final Timed timedClass) throws Exception {
        final String methodMetricName = MetricNameUtil.forTimedMethod(ctx.getTarget().getClass(), ctx.getMethod(), timedMethod);
        final String classMetricName = MetricNameUtil.forTimedMethod(ctx.getTarget().getClass(), ctx.getMethod(), timedClass);
        final Timer methodTimer = metricRegistryWrapper.timer(methodMetricName);
        final Timer classTimer = metricRegistryWrapper.timer(classMetricName);

        final Timer.Context classTimerContext = classTimer.time();
        final Timer.Context methodTimerContext = methodTimer.time();
        try {
            return ctx.proceed();
        } finally {
            final long elapsedMethodTime = methodTimerContext.stop();
            final long elapsedTotalTime = classTimerContext.stop();
            logger.debug("{} completed in [{}] seconds", methodMetricName, elapsedMethodTime / 1.0e9);
            logger.debug("{} completed in [{}] seconds", classMetricName, elapsedTotalTime / 1.0e9);
        }
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private Object timeMethodOrClassOnly(final InvocationContext ctx, final Timed timedMethod) throws Exception {
        final String metricName = MetricNameUtil.forTimedMethod(ctx.getTarget().getClass(), ctx.getMethod(), timedMethod);
        return timeSingleMetric(ctx, metricName);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private Object timeSingleMetric(final InvocationContext ctx, final String metricName) throws Exception {
        final Timer timer = metricRegistryWrapper.timer(metricName);
        final Timer.Context context = timer.time();
        try {
            return ctx.proceed();
        } finally {
            final long elapsedTime = context.stop();
            logger.debug("{} completed in [{}] seconds", metricName, elapsedTime / 1.0e9);
        }
    }
}
