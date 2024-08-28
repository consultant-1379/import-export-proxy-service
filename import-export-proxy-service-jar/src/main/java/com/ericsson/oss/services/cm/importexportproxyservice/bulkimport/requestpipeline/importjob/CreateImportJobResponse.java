/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * Response object for ImportJob creation.
 */
@SuppressWarnings("PMD.UseSingleton")
public class CreateImportJobResponse extends Response<ImportJobDto> {

    CreateImportJobResponse(final ImportJobDto importJob) {
        super(importJob);
    }

    public static CreateImportJobResponse createImportJobResponse(final ImportJobDto importJob) {
        return new CreateImportJobResponse(importJob);
    }
}
