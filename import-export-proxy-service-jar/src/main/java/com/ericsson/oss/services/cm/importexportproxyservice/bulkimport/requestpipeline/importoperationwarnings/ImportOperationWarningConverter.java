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

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationWarningDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationFailure;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

/**
 * Converter class from import operation failure entity to import operation warning DTO.
 */
public class ImportOperationWarningConverter implements Converter<ImportOperationFailure, ImportOperationWarningDto> {

    public ImportOperationWarningDto convert(final ImportOperationFailure failureEntity) {
        return ImportOperationWarningDto.importOperationWarning()
                .withId(failureEntity.getId())
                .withLineNumber(failureEntity.getLineNumber())
                .withOperationId(failureEntity.getImportOperation() != null ? failureEntity.getImportOperation().getId() : null)
                .withWarningCause(failureEntity.getWarningCause())
                .build();
    }
}
