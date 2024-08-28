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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.export.api.JobActionResponse;
import com.ericsson.oss.services.cm.export.api.JobsManager;
import com.ericsson.oss.services.cm.export.api.ProxyJobsManager;

/**
 * Implementation class for {@code ExportJobsManagerProcessorBean}. This class provides the implementation for
 * {@link JobsManager}.
 */
@Stateless
public class ExportJobsManagerBean implements JobsManager {

    @EServiceRef
    private ProxyJobsManager jobsManager;

    @Inject
    private Logger logger;

    @Override
    public JobActionResponse removeJob(final Long jobId) {
        logger.info("Export removeJob with jobId");
        return jobsManager.removeJob(jobId);
    }

    @Override
    public JobActionResponse removeJob(final String jobName) {
        logger.info("Export removeJob with jobName");
        return jobsManager.removeJob(jobName);
    }
}
