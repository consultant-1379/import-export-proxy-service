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
 * Request class for GET all Import Operations.
 */
public class GetAllImportOperationAttributesRequest implements Request<GetAllImportOperationAttributesResponse> {
    private final Criteria<ImportOperationAttributeDto> criteria;

    GetAllImportOperationAttributesRequest(final Criteria<ImportOperationAttributeDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportOperationAttributeDto> getCriteria() {
        return criteria;
    }

    public static GetAllImportOperationAttributesRequest getAllImportOperationAttributesRequest(
            final Criteria<ImportOperationAttributeDto> criteria) {
        return new GetAllImportOperationAttributesRequest(criteria);
    }
}
