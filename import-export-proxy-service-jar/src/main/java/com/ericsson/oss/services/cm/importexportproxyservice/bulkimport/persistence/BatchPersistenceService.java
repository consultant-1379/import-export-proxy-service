/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.persistence;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Persistence service for the ImportJob entity.
 */
public class BatchPersistenceService {

    private static final String GET_JOB_INSTANCE_BATCH_STATUS_FROM_JOB_ID = "SELECT BATCHSTATUS FROM JOB_EXECUTION WHERE JOBEXECUTIONID = ?";

    @Inject
    @PersistenceContext(unitName = "batchJobRepository")
    EntityManager entityManager;

    public String getBatchJobStatusFromJobId(final Long jobId) {
        final Query query = entityManager.createNativeQuery(GET_JOB_INSTANCE_BATCH_STATUS_FROM_JOB_ID);
        query.setParameter(1, jobId);
        final String result = (String) query.getSingleResult();
        return result;
    }

}