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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.persistence.helper;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportJobDetails;
import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportOperationDetails;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportServiceSpecification;
import com.ericsson.oss.services.cm.bulkimport.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.entities.ImportOperationStatus;
import com.ericsson.oss.services.cm.bulkimport.entities.Job;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.persistence.ImportPersistenceManagerBean;

/**
 * {@code Wrapper} wrapper for ImportPersistenceManagerBean.
 */
public class PersistenceJobGetter {

    @Inject
    ImportPersistenceManagerBean importPersistenceManagerBean;

    public List<ImportOperation> getImportOperationsForJob(final long jobId) {
        return importPersistenceManagerBean.getAllImportOperations(jobId);
    }

    public List<ImportOperation> getAllImportOperations(final long jobId, final ImportOperationStatus... importOperationStatus) {
        return importPersistenceManagerBean.getAllImportOperations(jobId, importOperationStatus);
    }

    public List<ImportOperation> getAllImportOperations(final List<Long> importOperationIds) {
        return importPersistenceManagerBean.getAllImportOperations(importOperationIds);
    }

    public List<ImportJobDetails> getJobDetails(final Long jobId) {
        return importPersistenceManagerBean.getJobDetails(jobId);
    }

    public ImportServiceSpecification getImportServiceSpecification(final long jobId) {
        return importPersistenceManagerBean.getImportServiceSpecification(jobId);
    }

    public ImportOperation getImportOperation(final Long importOperationId) {
        return importPersistenceManagerBean.getImportOperation(importOperationId);
    }

    public ImportOperation getImportOperationWithAttributesLoaded(final Long importOperationId) {
        return importPersistenceManagerBean.getImportOperationWithAttributesLoaded(importOperationId);
    }

    public List<ImportJobDetails> getAllJobDetails(final Date beginTime, final Date endTime) {
        return importPersistenceManagerBean.getAllJobDetails(beginTime, endTime);
    }

    public List<ImportOperationDetails> getAllImportOperationDetailsForJob(final long jobId) {
        return importPersistenceManagerBean.getAllImportOperationDetailsForJob(jobId);
    }

    public long getImportOperationsCount(final long jobId, final ImportOperationStatus... importOperationStatus) {
        return importPersistenceManagerBean.getImportOperationsCount(jobId, importOperationStatus);
    }

    public long getAllAttemptedOperationInCurrentInvocation(final long jobId, final Timestamp invocationStartTime) {
        return importPersistenceManagerBean.getAllAttemptedOperationInCurrentInvocation(jobId, invocationStartTime);
    }

    public long getUniqueNeIdsCount(final long jobId, final Timestamp invocationStartTime) {
        return importPersistenceManagerBean.getUniqueNeIdsCount(jobId, invocationStartTime);
    }

    public Map<String, Integer> getOperationCountByType(final long jobId) {
        return importPersistenceManagerBean.getOperationCountByType(jobId);
    }

    public Map<String, Integer> getOperationCountByStatus(final long jobId) {
        return importPersistenceManagerBean.getOperationCountByStatus(jobId);
    }

    public List<ImportOperation> getImportOperationsPartition(final long jobId, final int offset,
            final int limit, final ImportOperationStatus... importOperationStatus) {
        return importPersistenceManagerBean.getImportOperationsPartition(jobId, offset, limit, importOperationStatus);
    }

    public Job getJob(final long jobId) {
        return importPersistenceManagerBean.getJob(jobId);
    }

}
