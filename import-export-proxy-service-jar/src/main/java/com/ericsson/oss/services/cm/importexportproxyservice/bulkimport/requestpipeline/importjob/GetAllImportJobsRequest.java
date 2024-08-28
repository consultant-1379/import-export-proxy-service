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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.PageableRequest;

/**
 * Request class for GET all import Jobs.
 */
public class GetAllImportJobsRequest extends PageableRequest<GetAllImportJobsResponse> {
    private final Criteria<ImportJobDto> criteria;

    GetAllImportJobsRequest(final Criteria<ImportJobDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportJobDto> getCriteria() {
        return criteria;
    }

    public static GetAllImportJobsRequest getAllImportJobsRequest(final Criteria<ImportJobDto> criteria) {
        return new GetAllImportJobsRequest(criteria);
    }
}
