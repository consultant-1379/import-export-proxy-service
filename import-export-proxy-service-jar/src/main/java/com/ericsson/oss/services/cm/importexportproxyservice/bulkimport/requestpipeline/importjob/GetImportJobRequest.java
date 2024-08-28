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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request object GET import job.
 */
public class GetImportJobRequest implements Request<GetImportJobResponse> {
    private final Long jobId;

    GetImportJobRequest(final Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public static GetImportJobRequest getImportJobRequest(final Long jobId) {
        return new GetImportJobRequest(jobId);
    }
}
