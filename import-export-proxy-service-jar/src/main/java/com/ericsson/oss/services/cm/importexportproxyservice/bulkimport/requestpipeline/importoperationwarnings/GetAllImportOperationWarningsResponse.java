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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationwarnings;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationWarningDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;

/**
 * Import operation warning response builder class.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetAllImportOperationWarningsResponse extends CollectionResponse<ImportOperationWarningDto> {
    GetAllImportOperationWarningsResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getAllImportOperationWarningsResponse() {
        return new Builder();
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class Builder extends CollectionResponseBuilder<ImportOperationWarningDto, Builder> {
        @Override
        public GetAllImportOperationWarningsResponse build() {
            return new GetAllImportOperationWarningsResponse(this);
        }
    }
}
