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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;
import com.ericsson.oss.services.cm.bulkimport.util.FormatLogger;

/**
 * Bean that uses configured parameters for Import Service.
 */
@ApplicationScoped
public class BulkCmImportConfiguration {

    private static final String COUNT_OF_OPERATIONS_IN_LARGE_JOB = "bulkCmImport_countOfOperationsInLargeJob";
    private static final String MINIMUM_DELAY_FOR_SCHEDULING = "bulkCmImport_minimumDelayForScheduling";

    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(BulkCmImportConfiguration.class));
    private static final String PARAMETER_CHANGE_MSG = "import-service config param [{}] changed, old value = [{}], new value = [{}]";

    @Inject
    @Configured(propertyName = COUNT_OF_OPERATIONS_IN_LARGE_JOB)
    protected Integer countOfOperationsInLargeJob;

    @Inject
    @Configured(propertyName = MINIMUM_DELAY_FOR_SCHEDULING)
    protected Integer minimumDelayForScheduling;

    /**
     * Get count of operations to decide whether import job is large or small.
     *
     * @return Count of operations to decide import job as large or small.
     */
    public int getCountOfOperationsInLargeJob() {
        return countOfOperationsInLargeJob;
    }

    /**
     * Get minimum delay in minutes with respect to current time, for scheduling an import job execution.
     *
     * @return minimum delay needed for scheduling an import job execution
     */
    public Integer getMinimumDelayForScheduling() {
        return minimumDelayForScheduling;
    }

    void numberOfOperationsListener(
            @Observes @ConfigurationChangeNotification(propertyName = COUNT_OF_OPERATIONS_IN_LARGE_JOB) final Integer newValue) {
        LOGGER.info(PARAMETER_CHANGE_MSG, COUNT_OF_OPERATIONS_IN_LARGE_JOB, countOfOperationsInLargeJob, newValue);
        countOfOperationsInLargeJob = newValue;
    }

    void minimumDelayForSchedulingChangeListener(
            @Observes @ConfigurationChangeNotification(propertyName = MINIMUM_DELAY_FOR_SCHEDULING) final Integer newValue) {
        LOGGER.info(PARAMETER_CHANGE_MSG, MINIMUM_DELAY_FOR_SCHEDULING, minimumDelayForScheduling, newValue);
        minimumDelayForScheduling = newValue;
    }

}
