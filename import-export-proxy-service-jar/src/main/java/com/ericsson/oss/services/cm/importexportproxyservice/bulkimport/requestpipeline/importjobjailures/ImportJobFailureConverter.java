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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobFailureType;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobFailureDto;
import com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobFailure;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobOperation;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

/**
 * Converter class for ImportJob.
 */
public class ImportJobFailureConverter implements Converter<ImportJobFailure, ImportJobFailureDto> {

    public ImportJobFailureDto convert(final ImportJobFailure entity) {
        return ImportJobFailureDto.importJobFailure()
                .withId(entity.getId())
                .withJobId(entity.getJobOperation() != null ? entity.getJobOperation().getJobId() : null)
                .withFailureType(getFailureType(entity.getJobOperation()))
                .withLineMumber(entity.getLineNumber())
                .withFailureReason(entity.getFailureCause())
                .build();
    }

    private JobFailureType getFailureType(final ImportJobOperation operation) {
        if (operation != null && isNotBlank(operation.getOperationType())) {
            final String operationType = operation.getOperationType().trim();
            if (ImportConstants.VIOLATION_TYPE_SCHEMA_VALIDATION.equalsIgnoreCase(operationType)) {
                return JobFailureType.SCHEMA_VALIDATION;
            } else if (ImportConstants.VIOLATION_TYPE_SYNTAX_VALIDATION.equalsIgnoreCase(operationType)) {
                return JobFailureType.SYNTAX_VALIDATION;
            }
        }
        return null;
    }
}