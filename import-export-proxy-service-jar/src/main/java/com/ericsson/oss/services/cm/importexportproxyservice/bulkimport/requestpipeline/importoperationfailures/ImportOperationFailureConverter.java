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

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationFailureDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationFailure;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

/**
 * Converter class for ImportJob.
 */
public class ImportOperationFailureConverter implements Converter<ImportOperationFailure, ImportOperationFailureDto> {

    public ImportOperationFailureDto convert(final ImportOperationFailure entity) {
        return ImportOperationFailureDto.importOperationFailure()
                .withId(entity.getId())
                .withOperationId(entity.getImportOperation() != null ? entity.getImportOperation().getId() : null)
                .withLineMumber(entity.getLineNumber())
                .withFailureReason(entity.getFailureCause())
                .build();
    }
}