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

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request class for GET all persistent current attributes of import operations.
 */
public class GetAllImportOperationPersistentCurrentAttributesRequest implements Request<GetAllImportOperationPersistentCurrentAttributesResponse> {
    private final Criteria<ImportOperationAttributeDto> criteria;

    GetAllImportOperationPersistentCurrentAttributesRequest(final Criteria<ImportOperationAttributeDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportOperationAttributeDto> getCriteria() {
        return criteria;
    }

    public static GetAllImportOperationPersistentCurrentAttributesRequest getOperationPersistentCurrentAttributesRequest(
            final Criteria<ImportOperationAttributeDto> criteria) {
        return new GetAllImportOperationPersistentCurrentAttributesRequest(criteria);
    }
}
