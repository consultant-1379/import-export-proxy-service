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
 * Request for creating Dps Group list of nodes in an import job having operations valid for execution and executed.
 */
public class CreateDpsGroupRequest implements Request<CreateDpsGroupResponse> {
    private final Long jobId;

    CreateDpsGroupRequest(final Builder builder) {
        jobId = builder.jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public static Builder createDpsGroupRequest() {
        return new Builder();
    }

    /**
     * Builder for create Dps Group request with jobId.
     */
    public static class Builder {
        private Long jobId;

        public CreateDpsGroupRequest build() {
            return new CreateDpsGroupRequest(this);
        }

        public Builder withJobId(final Long jobId) {
            this.jobId = jobId;
            return this;
        }
    }
}
