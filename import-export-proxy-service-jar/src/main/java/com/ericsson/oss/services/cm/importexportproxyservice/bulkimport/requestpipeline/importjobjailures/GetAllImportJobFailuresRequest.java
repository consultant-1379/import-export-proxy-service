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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobjailures;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobFailureDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request class for GET ALL Import Job Failures.
 */
public class GetAllImportJobFailuresRequest implements Request<GetAllImportJobFailuresResponse> {
    private final Criteria<ImportJobFailureDto> criteria;

    GetAllImportJobFailuresRequest(final Criteria<ImportJobFailureDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportJobFailureDto> getCriteria() {
        return criteria;
    }

    public static GetAllImportJobFailuresRequest getAllImportJobFailuresRequest(final Criteria<ImportJobFailureDto> criteria) {
        return new GetAllImportJobFailuresRequest(criteria);
    }
}
