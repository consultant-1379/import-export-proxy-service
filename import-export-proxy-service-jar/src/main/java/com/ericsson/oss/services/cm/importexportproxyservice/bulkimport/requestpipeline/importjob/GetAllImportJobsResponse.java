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

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;

/**
 * Response object for GET Import Jobs.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetAllImportJobsResponse extends CollectionResponse<ImportJobDto> {

    GetAllImportJobsResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getAllImportJobsResponse() {
        return new Builder();
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class Builder extends CollectionResponseBuilder<ImportJobDto, Builder> {
        @Override
        public GetAllImportJobsResponse build() {
            return new GetAllImportJobsResponse(this);
        }
    }
}
