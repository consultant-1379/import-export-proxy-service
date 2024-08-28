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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;

/**
 * Response object for GET current Import Operations.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetAllImportOperationCurrentAttributesResponse extends CollectionResponse<ImportOperationAttributeDto> {

    GetAllImportOperationCurrentAttributesResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getAllImportOperationCurrentAttributesResponse() {
        return new Builder();
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class Builder extends CollectionResponseBuilder<ImportOperationAttributeDto, Builder> {
        @Override
        public GetAllImportOperationCurrentAttributesResponse build() {
            return new GetAllImportOperationCurrentAttributesResponse(this);
        }
    }
}
