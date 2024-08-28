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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.business;

import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobType.jobType;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobType;
import com.ericsson.oss.services.cm.bulkimport.entities.Job;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.ImportJobDoesNotExistException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.persistence.JobPersistenceManager;

import com.google.common.base.Preconditions;

/**
 * JobExistence can be used to assure job is created.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class JobExistence {
    public static final JobType[] ANY_JOB_TYPE = {};
    static final Logger LOGGER = LoggerFactory.getLogger(JobExistence.class);

    int retryIntervalMs = 3000;
    int retryAttempts = 10;

    @Inject
    JobPersistenceManager jobPersistenceManager;

    @Inject
    RetryExecutor retryExecutor;

    public int getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Get Job from persistence or form cache if the task has already fetched from persistence.
     *
     * @param jobId job id
     * @return Job instance
     * @throws ImportJobDoesNotExistException exception thrown if the job does not exist.
     */
    public Job getJob(final Long jobId) {
        return getJob(jobId, JobType.IMPORT);
    }

    /**
     * Get Job from persistence or form cache if the task has already fetched from persistence.
     *
     * @param jobId    job id
     * @param jobTypes expected job type
     * @return Job instance
     * @throws ImportJobDoesNotExistException exception thrown if the job does not exist.
     */
    public Job getJob(final Long jobId, final JobType... jobTypes) {
        Preconditions.checkArgument(jobTypes != null);
        final Job job = jobPersistenceManager.getJob(jobId);
        if (job == null || !hasType(job, jobTypes)) {
            throw new ImportJobDoesNotExistException(jobId);
        }
        return job;
    }

    /**
     * Refresh(fetch) Job from job persistence.
     *
     * @param jobId job id
     * @return Job instance
     * @throws ImportJobDoesNotExistException exception thrown if the job does not exist.
     */
    public Job refreshJob(final Long jobId) {
        return refreshJob(jobId, JobType.IMPORT);
    }

    /**
     * Refresh(fetch) Job from job persistence.
     *
     * @param jobId    job id
     * @param jobTypes expected job type
     * @return Job instance
     * @throws ImportJobDoesNotExistException exception thrown if the job does not exist.
     */
    public Job refreshJob(final Long jobId, final JobType... jobTypes) {
        Preconditions.checkArgument(jobTypes != null);
        final Job job = jobPersistenceManager.refreshJob(jobId);
        if (job == null) {
            LOGGER.info("Job:[{}] doesn't exist", jobId);
            throw new ImportJobDoesNotExistException(jobId);
        } else if (!hasType(job, jobTypes)) {
            LOGGER.info("For job:[{}] unexpected jobType:[{}], expected:[{}]", jobId, job.getJobType(), jobTypes);
            throw new ImportJobDoesNotExistException(jobId);
        }
        return job;
    }

    private boolean hasType(final Job job, final JobType... types) {
        if (types.length == 0) {
            return true;
        }
        final JobType jobType = jobType(job.getJobType());
        for (final JobType type : types) {
            if (type == jobType) {
                return true;
            }
        }
        return false;
    }
}
