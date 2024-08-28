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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationwarnings;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationWarningDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request class for get all import operations warnings.
 */
public class GetAllImportOperationWarningsRequest implements Request<GetAllImportOperationWarningsResponse> {
    private final Criteria<ImportOperationWarningDto> criteria;

    GetAllImportOperationWarningsRequest(final Criteria<ImportOperationWarningDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportOperationWarningDto> getCriteria() {
        return criteria;
    }

    public static GetAllImportOperationWarningsRequest getAllImportOperationWarningsRequest(final Criteria<ImportOperationWarningDto> criteria) {
        return new GetAllImportOperationWarningsRequest(criteria);
    }
}
