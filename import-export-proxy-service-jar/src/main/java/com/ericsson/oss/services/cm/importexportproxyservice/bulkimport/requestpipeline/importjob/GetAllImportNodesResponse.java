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

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;

/**
 * Response object for Get all import nodes.
 */
public class GetAllImportNodesResponse extends CollectionResponse<String> {

    GetAllImportNodesResponse(final Builder builder) {
        super(builder);
    }

    public static Builder getImportNodesResponse() {
        return new Builder();
    }

    /**
     * Collection Response Builder for get all import nodes.
     */
    public static class Builder extends CollectionResponseBuilder<String, Builder> {
        @Override
        public GetAllImportNodesResponse build() {
            return new GetAllImportNodesResponse(this);
        }
    }
}
