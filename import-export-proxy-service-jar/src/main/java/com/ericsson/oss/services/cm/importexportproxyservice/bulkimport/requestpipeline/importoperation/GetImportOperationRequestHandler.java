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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation.GetImportOperationResponse.getImportOperationResponse;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class for GET ImportJobs request.
 */
@Handle(GetImportOperationRequest.class)
public class GetImportOperationRequestHandler implements RequestHandler<GetImportOperationRequest, GetImportOperationResponse> {
    @Inject
    @Any
    Instance<RequestValidator<GetImportOperationRequest>> validators;

    @Inject
    ImportOperationConverter operationConverter;

    @Inject
    ImportOperationPersistenceService operationPersistence;

    @Override
    public GetImportOperationResponse handle(final GetImportOperationRequest request) {
        final Long operationId = request.getOperationId();
        final ImportOperation entity = operationPersistence.getRequired(operationId);
        if (entity == null) {
            return getImportOperationResponse(null);
        }
        return getImportOperationResponse(operationConverter.convert(entity));
    }

    @Override
    public Iterable<RequestValidator<GetImportOperationRequest>> validators() {
        return validators;
    }
}
