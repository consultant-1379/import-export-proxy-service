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

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetImportJobUnsyncNodesCountResponse.getImportJobUnsyncNodesCountResponse;

import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * GetUnsyncImportNodesCountRequestHandler class to handle unsynchronized nodes count request.
 */
@Handle(GetImportJobUnsyncNodesCountRequest.class)
public class GetImportJobUnsyncNodesCountRequestHandler implements
        RequestHandler<GetImportJobUnsyncNodesCountRequest, GetImportJobUnsyncNodesCountResponse> {
    @Inject
    @Any
    Instance<RequestValidator<GetImportJobUnsyncNodesCountRequest>> validators;

    @Inject
    ImportNodesService importNodesService;

    @Override
    public GetImportJobUnsyncNodesCountResponse handle(final GetImportJobUnsyncNodesCountRequest request) {
        final Map<String, String> unsyncNodeInfo = importNodesService.getUnsyncNodeInfoForExecutedAndExecutionEligibleOperations(request.getJobId());
        return getImportJobUnsyncNodesCountResponse(unsyncNodeInfo.size());
    }

    @Override
    public Iterable<RequestValidator<GetImportJobUnsyncNodesCountRequest>> validators() {
        return validators;
    }
}