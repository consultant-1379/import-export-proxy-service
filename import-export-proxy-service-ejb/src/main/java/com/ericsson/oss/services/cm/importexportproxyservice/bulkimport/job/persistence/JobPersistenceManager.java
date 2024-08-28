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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.persistence;

import static java.lang.String.format;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobType;
import com.ericsson.oss.services.cm.bulkimport.entities.Job;
import com.ericsson.oss.services.cm.bulkimport.util.FormatLogger;

/**
 * JobPersistenceManager can be used to retrieve and persist JOB related objects from database.
 */
public class JobPersistenceManager {

    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(JobPersistenceManager.class));

    @Inject
    @PersistenceContext(unitName = "importPersistenceUnit")
    EntityManager entityManager;

    /**
     * Creates job in Import database.
     *
     * @param job the job instance.
     * @return Job instance.
     */
    public Job createJob(final Job job) {
        LOGGER.trace("Creating job. Method name: {}", "createJob");
        entityManager.persist(job);
        LOGGER.trace("Job created with id = {}", job.getId());
        return job;
    }

    /**
     * Gets job identified by given job id.
     *
     * @param jobId the job identifier.
     * @return Job instance.
     */
    public Job getJob(final long jobId) {
        LOGGER.trace("Getting job with id = {}. Method name: {}", jobId, "getJob");
        return entityManager.find(Job.class, jobId);
    }

    /**
     * Finds all jobs with the given job type and status.
     *
     * @param jobType job type to filter.
     * @param status  job status to filter.
     * @return Job list of the job instances.
     */
    public List<Job> findAll(final JobType jobType, final String... status) {
        LOGGER.trace("Getting all jobs with type [{}] and status [{}]", jobType, status);
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Job> criteriaQuery = criteriaBuilder.createQuery(Job.class);
        final Root<Job> job = criteriaQuery.from(Job.class);
        criteriaQuery.select(job).where(
                criteriaBuilder.equal(
                        job.get("jobType"),
                        jobType.getType()
                ),
                job.get("status").in(status)
        );
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Refreshes or reloads the job entity from the database.
     *
     * @param jobId the job identifier.
     * @return Job instance.
     */
    public Job refreshJob(final long jobId) {
        LOGGER.trace("Getting job with jobId={}. Method name: {}", jobId, "refreshJob");
        final Job job = entityManager.find(Job.class, jobId);
        if (job == null) {
            return null;
        }
        try {
            entityManager.refresh(job);
            return job;
        } catch (final Exception unexpected) {
            LOGGER.warn(format("Refresh jobId=%s unexpected exception throws:%s", jobId, unexpected), unexpected);
            return null;
        }
    }
}
