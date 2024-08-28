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

import static com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.importOperationCriteria;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationAttributesResponse.getAllImportOperationAttributesResponse;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.ImportOperationCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class for GET Import Operation Attributes request.
 */
@Handle(GetAllImportOperationAttributesRequest.class)
public class GetAllImportOperationAttributesRequestHandler
        implements RequestHandler<GetAllImportOperationAttributesRequest, GetAllImportOperationAttributesResponse> {
    @Inject
    ImportOperationAttributeConverter attributeConverter;

    @Inject
    ImportOperationPersistenceService operationPersistence;

    @Override
    public GetAllImportOperationAttributesResponse handle(final GetAllImportOperationAttributesRequest request) {
        final List<ImportOperationAttributeDto> result = getAll(request);
        return getAllImportOperationAttributesResponse()
                .withContent(result)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportOperationAttributesRequest>> validators() {
        return new ArrayList<>();
    }

    private List<ImportOperationAttributeDto> getAll(final GetAllImportOperationAttributesRequest request) {
        final List<ImportOperationAttributeDto> result = new ArrayList<>();
        final Criteria<ImportOperation> criteria = criteria(request.getCriteria());
        final List<ImportOperation> operations = operationPersistence.getAll(criteria);
        for (final ImportOperation operation : operations) {
            final List<ImportOperationAttributeDto> attributes = attributeConverter.convert(operation);
            result.addAll(attributes);
        }
        return result;
    }

    Criteria<ImportOperation> criteria(final Criteria<ImportOperationAttributeDto> criteria) {
        final ImportOperationCriteria operationCriteria = importOperationCriteria();
        for (final Restriction<?> restriction : criteria.getRestrictions()) {
            final String attribute = restriction.getAttribute();
            if ("operation.id".equalsIgnoreCase(attribute)) {
                operationCriteria.addRestriction(ImportOperation.ID_FIELD, restriction.getOperator(), restriction.getArguments());
            }
        }
        return operationCriteria.build();
    }
}
