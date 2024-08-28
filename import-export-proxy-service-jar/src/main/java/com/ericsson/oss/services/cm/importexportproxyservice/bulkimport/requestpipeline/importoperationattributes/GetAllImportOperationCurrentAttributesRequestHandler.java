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

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationCurrentAttributesResponse.getAllImportOperationCurrentAttributesResponse;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class for GET Import Operation Current Attributes request.
 */
@Handle(GetAllImportOperationCurrentAttributesRequest.class)
public class GetAllImportOperationCurrentAttributesRequestHandler extends GetAllImportOperationCurrentAttributesService
        implements RequestHandler<GetAllImportOperationCurrentAttributesRequest, GetAllImportOperationCurrentAttributesResponse> {
    @Override
    public GetAllImportOperationCurrentAttributesResponse handle(final GetAllImportOperationCurrentAttributesRequest request) {
        final List<ImportOperationAttributeDto> result = getAll(request.getCriteria(), true);
        return getAllImportOperationCurrentAttributesResponse()
                .withContent(result)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportOperationCurrentAttributesRequest>> validators() {
        return new ArrayList<>();
    }
}
