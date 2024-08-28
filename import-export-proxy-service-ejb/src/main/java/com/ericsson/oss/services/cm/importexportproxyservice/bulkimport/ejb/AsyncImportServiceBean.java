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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.ejb;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.async.bulkimport.api.AsyncImportService;
import com.ericsson.oss.services.cm.async.bulkimport.api.ProxyAsyncImportService;
import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportJobDetails;
import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportOperationDetails;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportRetryServiceSpecification;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportServiceSpecification;
import com.ericsson.oss.services.cm.bulkimport.exception.BulkImportException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.UnexpectedErrorException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.business.JobExistence;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.business.JobStatus;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util.FormatLogger;

/**
 * Implementation class for {@code AsyncImportServiceBean}. This class provides the asynchronous implementation for
 * {@link AsyncImportService}.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AsyncImportServiceBean implements AsyncImportService {
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(AsyncImportServiceBean.class));
    private static final String METHOD_NAME = "Method name: {}";

    @EServiceRef
    ProxyAsyncImportService importService;

    @Inject
    JobExistence jobExistence;

    @Inject
    JobStatus jobStatus;

    @Override
    public long bulkImport(final ImportServiceSpecification importServiceSpecification) {
        LOGGER.info("Performing a bulk import. " + METHOD_NAME, "bulkImport");
        return importService.bulkImport(importServiceSpecification);
    }

    @Override
    public ImportJobDetails getJobDetails(final long jobId) {
        LOGGER.info("Getting details of job with id = {}. " + METHOD_NAME, jobId, "getJobDetails");
        try {
            jobExistence.getJob(jobId);
            return jobStatus.getJobDetails(jobId);
        } catch (final BulkImportException e) {
            handleBulkImportException(e);
        } catch (final Exception e) {
            handleUnexpectedException(e);
        }
        return null;
    }

    @Override
    public List<ImportJobDetails> getAllJobDetails() {
        LOGGER.info("Getting all job details. " + METHOD_NAME, "getAllJobDetails");
        try {
            return jobStatus.getAllJobDetails(null, null);
        } catch (final BulkImportException e) {
            handleBulkImportException(e);
        } catch (final Exception e) {
            handleUnexpectedException(e);
        }
        return null;
    }

    @Override
    public List<ImportJobDetails> getAllJobDetails(final Date beginTime, final Date endTime) {
        LOGGER.info("Getting all job details with begin and end time filters. " + METHOD_NAME, "getAllJobDetails");
        try {
            return jobStatus.getAllJobDetails(beginTime, endTime);
        } catch (final BulkImportException e) {
            handleBulkImportException(e);
        } catch (final Exception e) {
            handleUnexpectedException(e);
        }
        return null;
    }

    @Override
    public List<ImportOperationDetails> getAllOperationDetailsForJob(final long jobId) {
        LOGGER.info("Getting all operation details for job with id = {}" + METHOD_NAME, jobId, "getAllImportOperationDetailsForJob");
        try {
            jobExistence.getJob(jobId);
            return jobStatus.getAllOperationDetailsForJob(jobId);
        } catch (final BulkImportException e) {
            handleBulkImportException(e);
        } catch (final Exception e) {
            handleUnexpectedException(e);
        }
        return null;
    }

    @Override
    public void retryBulkImport(final ImportRetryServiceSpecification importRetryServiceSpecification) {
        LOGGER.info("Performing a retry bulk import. " + METHOD_NAME, "retryBulkImport");
        importService.retryBulkImport(importRetryServiceSpecification);
    }

    @Override
    public void cancelBulkImport(final long jobId) {
        LOGGER.info("Performing cancellation of import job {} " + METHOD_NAME, jobId, "cancelBulkImport");
        importService.cancelBulkImport(jobId);
    }

    private void handleBulkImportException(final BulkImportException e) {
        LOGGER.debug("Expected exception thrown [{}], throwing BulkImportException", e.getMessage());
        throw new BulkImportException(e.getErrorCode(), e.getRawMessage());
    }

    private void handleUnexpectedException(final Exception e) {
        LOGGER.error("Unexpected error  " + e.getMessage(), e);
        final UnexpectedErrorException unexpectedErrorException = new UnexpectedErrorException(e.getMessage());
        throw new BulkImportException(unexpectedErrorException.getErrorCode(), unexpectedErrorException.getRawMessage());
    }
}
