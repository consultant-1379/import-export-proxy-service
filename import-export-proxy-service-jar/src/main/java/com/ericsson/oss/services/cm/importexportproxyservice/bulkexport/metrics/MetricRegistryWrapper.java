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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * Lightweight wrapper implementation for the codehale metrics {@code MetricRegistry}.
 */
public class MetricRegistryWrapper {

    private final MetricRegistry metricRegistry;
    private final JmxReporter jmxReporter;
    private final CsvLoggerReporter loggerReporter;

    /**
     * Default constructor needed for CDI.
     */
    public MetricRegistryWrapper() {
        this(null, null, null);
    }

    public MetricRegistryWrapper(final MetricRegistry metricRegistry, final JmxReporter jmxReporter, final CsvLoggerReporter loggerReporter) {
        this.metricRegistry = metricRegistry;
        this.jmxReporter = jmxReporter;
        this.loggerReporter = loggerReporter;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public JmxReporter getJmxReporter() {
        return jmxReporter;
    }

    public CsvLoggerReporter getCsvLoggerReporter() {
        return loggerReporter;
    }

    /**
     * Creates or retrieve a {@code Timer} and registers it under the given name.
     *
     * @param name
     *            of the metric.
     * @return the Timer instance.
     */
    public Timer timer(final String name) {
        return metricRegistry.timer(name);
    }

    /**
     * Creates or retrieve a {@code Meter} and registers it under the given name.
     *
     * @param name
     *            of the metric.
     * @return the Meter instance.
     */
    public Meter meter(final String name) {
        return metricRegistry.meter(name);
    }

    /**
     * Creates or retrieve a {@code Counter} and registers it under the given name.
     *
     * @param name
     *            of the metric.
     * @return the Counter instance.
     */
    public Counter counter(final String name) {
        return metricRegistry.counter(name);
    }

    /**
     * Creates or retrieve a {@code Histogram} and registers it under the given name.
     *
     * @param name
     *            of the metric.
     * @return the Histogram instance.
     */
    public Histogram histogram(final String name) {
        return metricRegistry.histogram(name);
    }

    /**
     * Shutdown the related reporters of the registry.
     */
    public void shutdown() {
        if (jmxReporter != null) {
            jmxReporter.close();
        }
        if (loggerReporter != null) {
            loggerReporter.close();
        }
    }
}
