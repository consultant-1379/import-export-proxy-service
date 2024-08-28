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
 * Response object for GET Import Operations.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetAllImportOperationAttributesResponse extends CollectionResponse<ImportOperationAttributeDto> {

    GetAllImportOperationAttributesResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getAllImportOperationAttributesResponse() {
        return new Builder();
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class Builder extends CollectionResponseBuilder<ImportOperationAttributeDto, Builder> {
        @Override
        public GetAllImportOperationAttributesResponse build() {
            return new GetAllImportOperationAttributesResponse(this);
        }
    }
}
