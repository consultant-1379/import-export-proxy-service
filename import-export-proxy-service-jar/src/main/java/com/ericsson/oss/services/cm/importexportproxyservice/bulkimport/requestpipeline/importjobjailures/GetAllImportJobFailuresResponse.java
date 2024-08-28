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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobjailures;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobFailureDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;

/**
 * Response object for GET ALL ImportJobs Failures.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetAllImportJobFailuresResponse extends CollectionResponse<ImportJobFailureDto> {

    GetAllImportJobFailuresResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getAllImportJobFailuresResponse() {
        return new Builder();
    }

    /**
     * Builder for the GetAllImportJobFailuresResponse class.
     */
    public static class Builder extends CollectionResponseBuilder<ImportJobFailureDto, Builder> {
        @Override
        public GetAllImportJobFailuresResponse build() {
            return new GetAllImportJobFailuresResponse(this);
        }
    }
}
