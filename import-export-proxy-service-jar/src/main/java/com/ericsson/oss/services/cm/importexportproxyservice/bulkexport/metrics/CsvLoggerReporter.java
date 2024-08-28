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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * A reporter class for logging metrics values to a SLF4J {@code Logger} periodically,
 * similar to {@code Slf4jReporter} but this reporter uses a CSV format for an easy metrics plot.
 */
public class CsvLoggerReporter extends ScheduledReporter {

    private static final String SEPARATOR = ";";
    private final SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Logger logger;
    private final Marker marker;

    /**
     * Default constructor. It is recommended to use the Builder instead.
     *
     * @param registry
     *            MetricRegistry
     * @param logger
     *            the SLF4 Logger
     * @param marker
     *            the SLF4 marker
     * @param rateUnit
     *            the rate unit to use
     * @param durationUnit
     *            the duration unit.
     * @param filter
     *            to apply on the metrics.
     */
    protected CsvLoggerReporter(final MetricRegistry registry, final Logger logger, final Marker marker, final TimeUnit rateUnit,
            final TimeUnit durationUnit, final MetricFilter filter) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit);
        this.logger = logger;
        this.marker = marker;
    }

    /**
     * Returns a new {@code Builder} for {@code CsvLoggerReporter}.
     *
     * @param registry
     *            the registry to report
     * @return a {@code Builder} instance for a {@code CsvLoggerReporter}
     */
    public static Builder forRegistry(final MetricRegistry registry) {
        return new Builder(registry);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report(final SortedMap<String, Gauge> gauges, final SortedMap<String, Counter> counters,
            final SortedMap<String, Histogram> histograms, final SortedMap<String, Meter> meters, final SortedMap<String, Timer> timers) {
        for (final Entry<String, Gauge> entry : gauges.entrySet()) {
            logGauge(entry.getKey(), entry.getValue());
        }

        for (final Entry<String, Counter> entry : counters.entrySet()) {
            logCounter(entry.getKey(), entry.getValue());
        }

        for (final Entry<String, Histogram> entry : histograms.entrySet()) {
            logHistogram(entry.getKey(), entry.getValue());
        }

        for (final Entry<String, Meter> entry : meters.entrySet()) {
            logMeter(entry.getKey(), entry.getValue());
        }

        for (final Entry<String, Timer> entry : timers.entrySet()) {
            logTimer(entry.getKey(), entry.getValue());
        }
    }

    private void logTimer(final String name, final Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        log(name, timer.getCount(), convertDuration(snapshot.getMin()), convertDuration(snapshot.getMax()), convertDuration(snapshot.getMean()),
                convertDuration(snapshot.getStdDev()), convertDuration(snapshot.getMedian()), convertDuration(snapshot.get75thPercentile()),
                convertDuration(snapshot.get95thPercentile()), convertDuration(snapshot.get98thPercentile()),
                convertDuration(snapshot.get99thPercentile()), convertDuration(snapshot.get999thPercentile()), convertRate(timer.getMeanRate()),
                convertRate(timer.getOneMinuteRate()), convertRate(timer.getFiveMinuteRate()), convertRate(timer.getFifteenMinuteRate()),
                getRateUnit(), getDurationUnit());
    }

    private void logMeter(final String name, final Meter meter) {
        log(name, meter.getCount(), convertRate(meter.getMeanRate()), convertRate(meter.getOneMinuteRate()), convertRate(meter.getFiveMinuteRate()),
                convertRate(meter.getFifteenMinuteRate()), getRateUnit());
    }

    private void logHistogram(final String name, final Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        log(name, histogram.getCount(), snapshot.getMin(), snapshot.getMax(), snapshot.getMean(), snapshot.getStdDev(), snapshot.getMedian(),
                snapshot.get75thPercentile(), snapshot.get95thPercentile(), snapshot.get98thPercentile(), snapshot.get99thPercentile(),
                snapshot.get999thPercentile());
    }

    private void logCounter(final String name, final Counter counter) {
        log(name, counter.getCount());
    }

    @SuppressWarnings("rawtypes")
    private void logGauge(final String name, final Gauge gauge) {
        log(name, gauge.getValue());
    }

    @SuppressWarnings({ "squid:S3457", "squid:S2629" })
    private void log(final String metricName, final Object... values) {
        final StringBuilder record = new StringBuilder(metricName);
        record.append(SEPARATOR);
        record.append(localDateFormat.format(new Date()));
        for (final Object value : values) {
            record.append(SEPARATOR);
            record.append(value);
        }
        logger.info(marker, record.toString());
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }

    /**
     * A builder for {@code CsvLoggerReporter} instances.
     * <p>Defaults to logging to {@code metrics}, not using a marker, converting rates to events/second,
     * converting durations to milliseconds, and not filtering metrics.
     */
    public static final class Builder {
        private final MetricRegistry registry;
        private Logger logger;
        private Marker marker;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(final MetricRegistry registry) {
            this.registry = registry;
            logger = LoggerFactory.getLogger("metrics");
            marker = null;
            rateUnit = TimeUnit.SECONDS;
            durationUnit = TimeUnit.MILLISECONDS;
            filter = MetricFilter.ALL;
        }

        /**
         * Log metrics to the given logger.
         *
         * @param logger
         *            an SLF4J {@code Logger}
         * @return {@code this}
         */
        public Builder outputTo(final Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Mark all logged metrics with the given marker.
         *
         * @param marker
         *            an SLF4J {@code Marker}
         * @return {@code this}
         */
        public Builder markWith(final Marker marker) {
            this.marker = marker;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(final TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(final TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter
         *            a {@code MetricFilter}
         * @return {@code this}
         */
        public Builder filter(final MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@code CsvLoggerReporter} with the given properties.
         *
         * @return a {@code CsvLoggerReporter}
         */
        public CsvLoggerReporter build() {
            return new CsvLoggerReporter(registry, logger, marker, rateUnit, durationUnit, filter);
        }
    }
}
