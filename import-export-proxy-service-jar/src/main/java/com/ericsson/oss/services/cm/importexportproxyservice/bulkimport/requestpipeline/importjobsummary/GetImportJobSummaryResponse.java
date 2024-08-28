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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobsummary;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobSummaryDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;

/**
 * GetImportJobSummaryResponse.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetImportJobSummaryResponse extends CollectionResponse<ImportJobSummaryDto> {

    GetImportJobSummaryResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getImportJobSummaryResponse() {
        return new Builder();
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class Builder extends CollectionResponseBuilder<ImportJobSummaryDto, Builder> {
        @Override
        public GetImportJobSummaryResponse build() {
            return new GetImportJobSummaryResponse(this);
        }
    }
}
