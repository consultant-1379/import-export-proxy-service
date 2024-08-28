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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.ImportOperationCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;

/**
 * Class to handle the retrieval and transforming of current attributes.
 */
public class GetAllImportOperationCurrentAttributesService {
    @Inject
    ImportOperationPersistenceService operationPersistence;

    @Inject
    CurrentAttributeService currentAttributeService;

    protected List<ImportOperationAttributeDto> getAll(final Criteria<ImportOperationAttributeDto> requestCriteria,
            final boolean isNonPersistentAttrRequired) {
        final List<ImportOperationAttributeDto> result = new ArrayList<>();
        final Criteria<ImportOperation> criteria = criteria(requestCriteria);
        final List<ImportOperation> operations = operationPersistence.getAll(criteria);
        for (final ImportOperation operation : operations) {
            final Map<String, Object> currentAttributes = currentAttributeService.getCurrentAttributeValues(operation, isNonPersistentAttrRequired);

            if (currentAttributes == null) {
                continue;
            }
            for (final Map.Entry<String, Object> attribute : currentAttributes.entrySet()) {
                result.add(ImportOperationAttributeDto.importOperationAttributeDto()
                        .withName(attribute.getKey())
                        .withValue(attribute.getValue())
                        .withLineNumber(operation.getLineNumber())
                        .withOperationId(operation.getId()).build());
            }
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
