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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importinvocation;

import java.util.Date;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobExecutionPolicy;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobInvocationFlow;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * InvokeImportJobRequest class to construct import job invocation request.
 */
public class InvokeImportJobRequest implements Request<InvokeImportJobResponse> {
    private final Long jobId;
    private final JobInvocationFlow jobInvocationFlow;
    private final JobValidationPolicy[] jobValidationPolicy;
    private final JobExecutionPolicy[] jobExecutionPolicy;
    private final Date scheduleTime;

    InvokeImportJobRequest(final Builder builder) {
        this.jobId = builder.jobId;
        this.jobInvocationFlow = builder.flow;
        this.jobExecutionPolicy = builder.jobExecutionPolicy;
        this.jobValidationPolicy = builder.jobValidationPolicy;
        this.scheduleTime = builder.scheduleTime;
    }

    public Long getJobId() {
        return jobId;
    }

    public JobInvocationFlow getJobInvocationFlow() {
        return jobInvocationFlow;
    }

    public JobValidationPolicy[] getJobValidationPolicy() {
        return jobValidationPolicy;
    }

    public JobExecutionPolicy[] getJobExecutionPolicy() {
        return jobExecutionPolicy;
    }

    public Date getScheduleTime() {
        return scheduleTime;
    }

    public static Builder invokeImportJobRequest() {
        return new Builder();
    }

    @Override public String toString() {
        return "InvokeImportJobRequest{"
                + "jobId=" + jobId
                + ", jobInvocationFlow=" + jobInvocationFlow
                + ", jobValidationPolicy=" + jobValidationPolicy
                + ", jobExecutionPolicy=" + jobExecutionPolicy
                + ", scheduleTime=" + scheduleTime
                + '}';
    }

    @SuppressWarnings({ "unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod" })
    public static class Builder {
        private Long jobId;
        private JobInvocationFlow flow;
        private JobValidationPolicy[] jobValidationPolicy;
        private JobExecutionPolicy[] jobExecutionPolicy;
        private Date scheduleTime;

        public InvokeImportJobRequest build() {
            return new InvokeImportJobRequest(this);
        }

        public Builder withJobId(final Long jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder withJobInvocationFlow(final JobInvocationFlow jobInvocationFlow) {
            this.flow = jobInvocationFlow;
            return this;
        }

        public Builder withJobValidationPolicy(final JobValidationPolicy... jobValidationPolicy) {
            this.jobValidationPolicy = jobValidationPolicy;
            return this;
        }

        public Builder withExecutionPolicy(final JobExecutionPolicy... jobExecutionPolicy) {
            this.jobExecutionPolicy = jobExecutionPolicy;
            return this;
        }

        public Builder withScheduleTime(final Date scheduleTime) {
            this.scheduleTime = scheduleTime;
            return this;
        }
    }
}
