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

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetAllImportNodesResponse.getImportNodesResponse;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class for Get all import nodes request.
 */
@Handle(GetAllImportNodesRequest.class)
public class GetAllImportNodesRequestHandler implements RequestHandler<GetAllImportNodesRequest, GetAllImportNodesResponse> {

    @Inject
    @Any
    Instance<RequestValidator<GetAllImportNodesRequest>> validators;

    @Inject
    ImportNodesService importNodesService;

    @Override
    public GetAllImportNodesResponse handle(final GetAllImportNodesRequest request) {
        final List<String> nodeNames = importNodesService.getExecutedAndExecutionEligibleNodes(request.getJobId());
        return getImportNodesResponse()
                .withContent(nodeNames)
                .withTotalCount((long) nodeNames.size())
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportNodesRequest>> validators() {
        return validators;
    }
}
