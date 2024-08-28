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
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.codahale.metrics.MetricRegistry;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Metered;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Metric;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Timed;

/**
 * Utility class for creating metrics names base on metric group-names or either
 * class-method names.
 */
public final class MetricNameUtil {
    private MetricNameUtil() {}

    /**
     * Create the metric name for a Timed annotation.
     *
     * @param klass
     *            to use in the name
     * @param method
     *            to use in the name
     * @param annotation
     *            to use in the name
     * @return the metric name
     */
    public static String forTimedMethod(final Class<?> klass, final Method method, final Timed annotation) {
        return MetricRegistry.name(chooseGroup(annotation.group(), klass), chooseName(annotation.name(), method));
    }

    /**
     * Create the metric name for a Metered annotation.
     *
     * @param klass
     *            to use in the name
     * @param method
     *            to use in the name
     * @param annotation
     *            to use in the name
     * @return the metric name
     */
    public static String forMeteredMethod(final Class<?> klass, final Method method, final Metered annotation) {
        return MetricRegistry.name(chooseGroup(annotation.group(), klass), chooseName(annotation.name(), method));
    }

    /**
     * Create the metric name for an injected metric field.
     *
     * @param klass
     *            to use in the name
     * @param field
     *            to use in the name
     * @param annotation
     *            to use in the name
     * @return the metric name
     */
    public static String forInjectedMetricField(final Class<?> klass, final Field field, final Metric annotation) {
        return MetricRegistry.name(chooseGroup(annotation.group(), klass), chooseName(annotation.name(), field));
    }

    private static String chooseGroup(final String group, final Class<?> klass) {
        if ((group == null) || group.isEmpty()) {
            return klass.getName();
        }
        return group;
    }

    private static String chooseName(final String name, final Member field) {
        if ((name == null) || name.isEmpty()) {
            return field.getName();
        }
        return name;
    }
}
