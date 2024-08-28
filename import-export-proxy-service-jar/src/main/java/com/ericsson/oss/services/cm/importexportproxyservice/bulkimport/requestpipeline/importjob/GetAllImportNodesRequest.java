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

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request object for Get all import nodes.
 */
public class GetAllImportNodesRequest implements Request<GetAllImportNodesResponse> {
    private final Long jobId;

    GetAllImportNodesRequest(final Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public static GetAllImportNodesRequest getAllImportNodes(final Long jobId) {
        return new GetAllImportNodesRequest(jobId);
    }
}