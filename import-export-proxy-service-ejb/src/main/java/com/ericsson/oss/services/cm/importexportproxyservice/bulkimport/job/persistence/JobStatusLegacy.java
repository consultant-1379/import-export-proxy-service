/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.persistence;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.ericsson.oss.services.cm.async.bulkimport.job.status.AsyncBulkImportJobStatus;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus;

/**
 * Class to convert job status db field to {@link AsyncBulkImportJobStatus}.
 */
public final class JobStatusLegacy {
    private JobStatusLegacy() {
    }

    public static AsyncBulkImportJobStatus fromJobStatusInDb(final String status, final String failureReason) {
        AsyncBulkImportJobStatus asyncBulkImportJobStatus = null;
        if (JobStatus.CREATED.getStatus().equals(status) || JobStatus.PARSING.getStatus().equals(status)
                || JobStatus.VALIDATING.getStatus().equals(status) || JobStatus.EXECUTING.getStatus().equals(status)) {
            asyncBulkImportJobStatus = AsyncBulkImportJobStatus.IN_PROGRESS;
        } else if (JobStatus.PARSED.getStatus().equals(status) || JobStatus.VALIDATED.getStatus().equals(status)
                || JobStatus.EXECUTED.getStatus().equals(status) || JobStatus.AUDITED.getStatus().equals(status)) {
            if (isNullOrEmpty(failureReason)) {
                asyncBulkImportJobStatus = AsyncBulkImportJobStatus.COMPLETED;
            } else {
                asyncBulkImportJobStatus = AsyncBulkImportJobStatus.FAILED;
            }
        } else if (JobStatus.CANCELLING.getStatus().equals(status)) {
            asyncBulkImportJobStatus = AsyncBulkImportJobStatus.CANCELLING;
        } else if (JobStatus.CANCELLED.getStatus().equals(status)) {
            asyncBulkImportJobStatus = AsyncBulkImportJobStatus.CANCELLED;
        } else {
            asyncBulkImportJobStatus = AsyncBulkImportJobStatus.getJobStatus(status);
        }
        return asyncBulkImportJobStatus;
    }
}
