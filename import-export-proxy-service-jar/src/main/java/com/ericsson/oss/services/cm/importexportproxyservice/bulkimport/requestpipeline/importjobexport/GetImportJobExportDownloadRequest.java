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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Import Job export file download file path request class.
 */
public class GetImportJobExportDownloadRequest implements Request<GetImportJobExportDownloadResponse> {

    private final Long importJobId;
    private final Long jobExportId;

    public GetImportJobExportDownloadRequest(final Builder builder) {
        importJobId = builder.importJobId;
        jobExportId = builder.jobExportId;
    }

    public Long getImportJobId() {
        return importJobId;
    }

    public Long getJobExportId() {
        return jobExportId;
    }

    public static Builder getImportJobExportDownload() {
        return new Builder();
    }

    /**
     * Builder class.
     */
    public static class Builder {
        private Long importJobId;
        private Long jobExportId;

        public Builder withImportJobId(final Long importJobId) {
            this.importJobId = importJobId;
            return this;
        }

        public Builder withJobExportId(final Long jobExportId) {
            this.jobExportId = jobExportId;
            return this;
        }

        public GetImportJobExportDownloadRequest build() {
            return new GetImportJobExportDownloadRequest(this);
        }
    }
}
