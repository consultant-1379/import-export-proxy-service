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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * An annotation requesting that a metric be injected or registered.
 * <p>
 * Given a field like this:
 *
 * <pre>
 * <code> {@literal @}Metric
 * public Histogram histogram;
 * </code>
 * </pre>
 *
 * <p>
 * A histogram of the field's type will be created and injected into managed
 * objects.
 * It will be up to the user to interact with the metric. This annotation
 * can be used on fields of type Meter, Timer, Counter, and Histogram.
 */
@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Metric {

    /**
     * The group of the metric as part of the metric name.
     *
     * @return the metric group
     */
    @Nonbinding
    String group() default "";

    /**
     * The name of the meter as part of the metric name.
     *
     * @return the metric name
     */
    @Nonbinding
    String name() default "";
}
