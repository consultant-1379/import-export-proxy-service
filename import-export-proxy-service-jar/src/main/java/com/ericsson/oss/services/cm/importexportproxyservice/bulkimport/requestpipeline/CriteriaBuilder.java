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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline;

import static com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.importOperationCriteria;

import javax.validation.constraints.NotNull;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.ImportOperationCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;

/**
 * Utility class to build an import operation criteria based on requested criteria attributes.
 */
public final class CriteriaBuilder {

    private CriteriaBuilder() {
    }

    public static Criteria<ImportOperation> buildCriteria(@NotNull final Criteria<?> criteria, final String... criteriaAttributes) {
        final ImportOperationCriteria operationCriteria = importOperationCriteria();
        criteria.getRestrictions().forEach(restriction -> {
            for (final String criteriaAttribute : criteriaAttributes) {
                if (criteriaAttribute.equalsIgnoreCase(restriction.getAttribute())) {
                    operationCriteria.addRestriction(ImportOperation.ID_FIELD, restriction.getOperator(), restriction.getArguments());
                }
            }
        });
        operationCriteria.withPageParams(criteria.getOffset(), criteria.getLimit());
        if (criteria.isTotalCountRequired()) {
            operationCriteria.totalCountRequired();
        }
        return operationCriteria.build();
    }
}