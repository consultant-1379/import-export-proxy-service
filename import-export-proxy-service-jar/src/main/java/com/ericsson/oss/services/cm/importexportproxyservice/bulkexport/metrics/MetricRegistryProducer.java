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

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.ApplicationMetrics;

/**
 * This class implements a Producer method for creating an instance of a {@code MetricRegistryWrapper}.
 * <p>
 * The CDI implementation automatically registers a {@code MetricRegistryWrapper} bean into the CDI
 * container to register any Metric instances produced.
 * That default {@code MetricRegistryWrapper} bean can be injected using
 * standard CDI type-safe resolution,
 * for example, by declaring an injected field.
 * <p>
 * It uses system properties to enable/disable a {@code JmxReporter} and a
 * a {@code CsvLoggerReporter}. Typically the user does not need to specify any
 * system property because the defaults (enable jmx reporter and
 * disable logger reporter) suit most of the applications.
 */
@ApplicationScoped
public class MetricRegistryProducer {

    private static final String ENABLE_JMX_REPORTER_SYSTEM_PROPERTY = "com.ericsson.oss.services.cm.cmconfig.metrics.jmxreporter.enable";
    private static final String ENABLE_LOGGER_REPORTER_SYSTEM_PROPERTY = "com.ericsson.oss.services.cm.cmconfig.metrics.csvreporter.enable";
    private static final String LOGGER_REPORTER_PROPERTY = "com.ericsson.oss.services.cm.cmconfig.metrics.csvreporter.logger";
    private static final String DEFAULT_LOGGER = "com.ericsson.oss.services.cm.cmconfig.service.metrics.log";
    private static final String LOGGER_INTERVAL_IN_MINUTES = "com.ericsson.oss.services.cm.cmconfig.metrics.csvreporter.interval.minutes";
    private static final int DEFAULT_INTERVAL = 5;

    private static final Logger logger = LoggerFactory.getLogger(MetricRegistryProducer.class);

    /**
     * Producer method.
     *
     * @return a instance of a {@code MetricRegistryWrapper}.
     */
    @Produces
    @ApplicationScoped
    @ApplicationMetrics
    public MetricRegistryWrapper produceMetricsRegistry() {
        logger.info("Creating a new instance of MetricRegistry ...");
        final MetricRegistry registry = new MetricRegistry();

        final JmxReporter jmxReporter = initJxmReporter(registry);

        final CsvLoggerReporter loggerReporter = initCsvLoggerReporter(registry);

        return new MetricRegistryWrapper(registry, jmxReporter, loggerReporter);
    }

    /**
     * Dispose method.
     *
     * @param metricRegistryWrapper
     *            object.
     */
    public void closeRegistry(@Disposes @ApplicationMetrics final MetricRegistryWrapper metricRegistryWrapper) {
        metricRegistryWrapper.shutdown();
        logger.info("MetricRegistryWrapper close : [{}]", metricRegistryWrapper);
    }

    private JmxReporter initJxmReporter(final MetricRegistry registry) {
        final String enableJmxReporterProp = System.getProperty(ENABLE_JMX_REPORTER_SYSTEM_PROPERTY);
        final boolean enableJmxReporter = enableJmxReporterProp != null ? Boolean.valueOf(enableJmxReporterProp) : true;
        if (enableJmxReporter) {
            final JmxReporter jmxReporter = JmxReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS).build();
            jmxReporter.start();
            logger.info("JmxReporter enabled : [{}]", jmxReporter);
            return jmxReporter;
        } else {
            return null;
        }
    }

    private CsvLoggerReporter initCsvLoggerReporter(final MetricRegistry registry) {
        final String enableLoggerReporterProp = System.getProperty(ENABLE_LOGGER_REPORTER_SYSTEM_PROPERTY);
        final boolean enableLoggerReporter = enableLoggerReporterProp != null ? Boolean.valueOf(enableLoggerReporterProp) : false;

        String loggerProp = System.getProperty(LOGGER_REPORTER_PROPERTY);
        loggerProp = loggerProp != null ? loggerProp : DEFAULT_LOGGER;

        final String loggerIntervalInMinutesProp = System.getProperty(LOGGER_INTERVAL_IN_MINUTES);
        final int loggerIntervalInMinutes = loggerIntervalInMinutesProp != null ? Integer.valueOf(loggerIntervalInMinutesProp) : DEFAULT_INTERVAL;

        if (enableLoggerReporter) {
            final CsvLoggerReporter loggerReporter = CsvLoggerReporter.forRegistry(registry).outputTo(LoggerFactory.getLogger(loggerProp))
                    .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
            loggerReporter.start(loggerIntervalInMinutes, TimeUnit.MINUTES);

            logger.info("CsvLoggerReporter enabled : [{}]", loggerReporter);
            return loggerReporter;
        } else {
            return null;
        }
    }
}
