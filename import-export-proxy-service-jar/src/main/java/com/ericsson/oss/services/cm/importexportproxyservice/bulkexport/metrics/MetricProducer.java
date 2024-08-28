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

import java.lang.reflect.Field;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.ApplicationMetrics;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Metric;

/**
 * This class implements {@code Produces} methods for the {@code Metric} annotation.
 */
public class MetricProducer {
    private static final Logger logger = LoggerFactory.getLogger(MetricProducer.class);

    @Inject
    @ApplicationMetrics
    private MetricRegistryWrapper metricRegistryWrapper;

    /**
     * Producer method for a {@code Timer} field annotated with {@code Metric}.
     *
     * @param ip
     *            the InjectionPoint
     * @return a Timer instance
     */
    @Produces
    @Metric
    public Timer produceTimer(final InjectionPoint ip) {
        logger.debug("creating timer for injectionpoint {}", ip);
        return metricRegistryWrapper.timer(getMetricNameFromInjectionPoint(ip));
    }

    private String getMetricNameFromInjectionPoint(final InjectionPoint ip) {
        final Metric annotation = ip.getAnnotated().getAnnotation(Metric.class);
        return MetricNameUtil.forInjectedMetricField(ip.getMember().getDeclaringClass(), (Field) ip.getMember(), annotation);
    }

    /**
     * Producer method for a {@code Meter} field annotated with {@code Metric}.
     *
     * @param ip
     *            the InjectionPoint
     * @return a Timer instance
     */
    @Produces
    @Metric
    public Meter produceMeter(final InjectionPoint ip) {
        logger.debug("creating meter for injectionpoint {}", ip);
        return metricRegistryWrapper.meter(getMetricNameFromInjectionPoint(ip));
    }

    /**
     * Producer method for a {@code Counter} field annotated with {@code Metric} .
     *
     * @param ip
     *            the InjectionPoint
     * @return a Timer instance
     */
    @Produces
    @Metric
    public Counter produceCounter(final InjectionPoint ip) {
        logger.debug("creating counter for injectionpoint {}", ip);
        return metricRegistryWrapper.counter(getMetricNameFromInjectionPoint(ip));
    }

    /**
     * Producer method for a {@code Histogram} field annotated with {@code Metric}.
     *
     * @param ip
     *            the InjectionPoint
     * @return a Timer instance
     */
    @Produces
    @Metric
    public Histogram produceHistogram(final InjectionPoint ip) {
        logger.debug("creating histogram for injectionpoint {}", ip);
        return metricRegistryWrapper.histogram(getMetricNameFromInjectionPoint(ip));
    }
}
