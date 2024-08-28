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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobadditionalinfo;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request for fetching additional info of an import job.
 */
public class GetAdditionalJobInfoRequest implements Request<GetAdditionalJobInfoResponse> {
    private final Long jobId;

    GetAdditionalJobInfoRequest(final Builder builder) {
        jobId = builder.jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public static Builder getAdditionalJobInfoRequest() {
        return new Builder();
    }

    /**
     * Builder for additional job info request with jobId.
     */
    public static class Builder {
        private Long jobId;

        public GetAdditionalJobInfoRequest build() {
            return new GetAdditionalJobInfoRequest(this);
        }

        public Builder withJobId(final Long jobId) {
            this.jobId = jobId;
            return this;
        }
    }
}
