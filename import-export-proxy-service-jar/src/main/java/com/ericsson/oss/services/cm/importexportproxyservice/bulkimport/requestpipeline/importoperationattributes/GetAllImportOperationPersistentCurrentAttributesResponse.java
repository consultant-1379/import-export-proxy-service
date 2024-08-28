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
 * Response object for GET all persistent current attributes of import operations.
 */
public class GetAllImportOperationPersistentCurrentAttributesResponse extends CollectionResponse<ImportOperationAttributeDto> {
    GetAllImportOperationPersistentCurrentAttributesResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getOperationPersistentCurrentAttributesResponse() {
        return new Builder();
    }

    @SuppressWarnings({ "checkstyle:JavadocType", "checkstyle:JavadocMethod" })
    public static class Builder extends CollectionResponseBuilder<ImportOperationAttributeDto, Builder> {
        @Override
        public GetAllImportOperationPersistentCurrentAttributesResponse build() {
            return new GetAllImportOperationPersistentCurrentAttributesResponse(this);
        }
    }
}
