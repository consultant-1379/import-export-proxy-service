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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportFileDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Request;

/**
 * Request class for GET all import Files.
 */
public class GetAllImportFilesRequest implements Request<GetAllImportFilesResponse> {
    private final Criteria<ImportFileDto> criteria;

    GetAllImportFilesRequest(final Criteria<ImportFileDto> criteria) {
        this.criteria = criteria;
    }

    public Criteria<ImportFileDto> getCriteria() {
        return criteria;
    }

    public static GetAllImportFilesRequest getAllImportFilesRequest(final Criteria<ImportFileDto> criteria) {
        return new GetAllImportFilesRequest(criteria);
    }
}
