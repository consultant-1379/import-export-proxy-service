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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobExecutionPolicy;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request for ImportJob creation.
 */
public class CreateImportJobRequest implements Request<CreateImportJobResponse> {
    private final String name;
    private final String userid;
    private final String configuration;
    private final JobValidationPolicy[] jobValidationPolicy;
    private final JobExecutionPolicy[] jobExecutionPolicy;

    CreateImportJobRequest(final Builder builder) {
        this.name = builder.name;
        this.userid = builder.userid;
        this.configuration = builder.configuration;
        this.jobExecutionPolicy = builder.jobExecutionPolicy;
        this.jobValidationPolicy = builder.jobValidationPolicy;
    }

    public String getName() {
        return name;
    }

    public String getUserid() {
        return userid;
    }

    public String getConfiguration() {
        return configuration;
    }

    public JobValidationPolicy[] getJobValidationPolicy() {
        return jobValidationPolicy;
    }

    public JobExecutionPolicy[] getJobExecutionPolicy() {
        return jobExecutionPolicy;
    }

    public static Builder createImportJobRequest() {
        return new Builder();
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class Builder {
        private String name;
        private String userid;
        private String configuration;
        private JobValidationPolicy[] jobValidationPolicy;
        private JobExecutionPolicy[] jobExecutionPolicy;

        public CreateImportJobRequest build() {
            return new CreateImportJobRequest(this);
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withUserid(final String userid) {
            this.userid = userid;
            return this;
        }

        public Builder withConfiguration(final String configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder withJobValidationPolicy(final JobValidationPolicy... jobValidationPolicy) {
            this.jobValidationPolicy = jobValidationPolicy;
            return this;
        }

        public Builder withJobExecutionPolicy(final JobExecutionPolicy... jobExecutionPolicy) {
            this.jobExecutionPolicy = jobExecutionPolicy;
            return this;
        }
    }
}
