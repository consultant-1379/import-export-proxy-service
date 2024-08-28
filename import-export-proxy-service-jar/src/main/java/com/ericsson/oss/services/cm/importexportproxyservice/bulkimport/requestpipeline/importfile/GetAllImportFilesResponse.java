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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportFileDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;

/**
 * Response object for GET all Import Files Request.
 */
@SuppressWarnings("PMD.UseSingleton")
public class GetAllImportFilesResponse extends CollectionResponse<ImportFileDto> {

    GetAllImportFilesResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getAllImportFilesResponse() {
        return new Builder();
    }

    @SuppressWarnings({ "unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod" })
    public static class Builder extends CollectionResponseBuilder<ImportFileDto, Builder> {
        @Override
        public GetAllImportFilesResponse build() {
            return new GetAllImportFilesResponse(this);
        }
    }
}
