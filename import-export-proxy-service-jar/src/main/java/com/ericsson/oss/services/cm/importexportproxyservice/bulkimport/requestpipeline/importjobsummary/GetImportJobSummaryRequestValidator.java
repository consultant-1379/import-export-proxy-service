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

import java.util.List;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetAllImportJobsRequest;

/**
 * Validator for ImportJobSummary request.
 */
public class GetImportJobSummaryRequestValidator implements RequestValidator<GetAllImportJobsRequest> {

    @Override
    public List validate(final GetAllImportJobsRequest request) {
        return NO_ERRORS;
    }
}
