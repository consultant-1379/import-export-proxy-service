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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationfailures;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationFailureDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request class for GET ALL Import Operation Failures.
 */
public class GetAllImportOperationFailuresRequest implements Request<GetAllImportOperationFailuresResponse> {
    private final Criteria<ImportOperationFailureDto> criteria;

    GetAllImportOperationFailuresRequest(final Criteria<ImportOperationFailureDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportOperationFailureDto> getCriteria() {
        return criteria;
    }

    public static GetAllImportOperationFailuresRequest getAllImportOperationsRequest(final Criteria<ImportOperationFailureDto> criteria) {
        return new GetAllImportOperationFailuresRequest(criteria);
    }
}
