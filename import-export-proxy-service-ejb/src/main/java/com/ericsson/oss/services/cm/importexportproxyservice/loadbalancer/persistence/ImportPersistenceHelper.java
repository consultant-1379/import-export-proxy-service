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

package com.ericsson.oss.services.cm.importexportproxyservice.loadbalancer.persistence;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.cm.bulkimport.entities.ImportOperationStatus;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.BulkCmImportConfiguration;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.persistence.ImportPersistenceManagerBean;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util.FormatLogger;

/**
 * Database interaction class to process request towards Import database.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ImportPersistenceHelper {
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(ImportPersistenceHelper.class));

    @Inject
    private BulkCmImportConfiguration bulkCmImportConfiguration;

    @Inject
    private ImportPersistenceManagerBean importPersistenceManagerBean;

    public boolean isLargeJob(final long jobId, final ImportOperationStatus... importOperationStatus) {
        LOGGER.debug("Getting Count of operations for jobId {} with status {}. Method name: {}",
                jobId, importOperationStatus, "isLargeJob");
        final long operationCount = importPersistenceManagerBean.getImportOperationsCount(jobId, importOperationStatus);
        final long numberOfOperationsInLargeJob = bulkCmImportConfiguration.getCountOfOperationsInLargeJob();
        LOGGER.debug("Number of Operations in jobId: {}. Number of Operations to be considered as Large Job {}",
                operationCount, numberOfOperationsInLargeJob);
        return operationCount >= numberOfOperationsInLargeJob;
    }
}