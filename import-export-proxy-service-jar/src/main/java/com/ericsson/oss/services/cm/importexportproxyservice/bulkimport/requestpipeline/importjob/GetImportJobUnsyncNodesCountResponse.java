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

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * Response for Unsynchronized Nodes Count with count.
 */
public class GetImportJobUnsyncNodesCountResponse extends Response<Integer> {

    GetImportJobUnsyncNodesCountResponse(final Integer unsyncNodesCount) {
        super(unsyncNodesCount);
    }

    public static GetImportJobUnsyncNodesCountResponse getImportJobUnsyncNodesCountResponse(final Integer unsyncNodesCount) {
        return new GetImportJobUnsyncNodesCountResponse(unsyncNodesCount);
    }
}
