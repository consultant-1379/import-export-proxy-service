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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobsummary;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobSummaryDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request object for ImportJobSummary.
 */
public class GetImportJobSummaryRequest implements Request<GetImportJobSummaryResponse> {
    private final Criteria<ImportJobSummaryDto> criteria;

    GetImportJobSummaryRequest(final Criteria<ImportJobSummaryDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportJobSummaryDto> getCriteria() {
        return criteria;
    }

    public static GetImportJobSummaryRequest getImportJobSummaryRequest(final Criteria<ImportJobSummaryDto> criteria) {
        return new GetImportJobSummaryRequest(criteria);
    }
}
