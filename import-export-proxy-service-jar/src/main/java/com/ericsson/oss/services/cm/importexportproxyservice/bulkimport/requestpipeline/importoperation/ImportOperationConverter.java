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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation;

import com.ericsson.oss.services.cm.bulkimport.api.domain.Breakpoint;
import com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus;
import com.ericsson.oss.services.cm.bulkimport.api.domain.OperationType;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.BreakpointV2;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.OperationTypeV2;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

/**
 * Converter class for ImportJob.
 */
public class ImportOperationConverter implements Converter<ImportOperation, ImportOperationDto> {

    @Override
    public ImportOperationDto convert(final ImportOperation entity) {
        return ImportOperationDto.importOperation()
                .withId(entity.getId())
                .withJobId(entity.getJobId())
                .withFdn(entity.getFdn())
                .withStatus(operationStatus(entity.getOperationStatus()))
                .withType(operationType(entity.getOperationType()))
                .withUpdateTime(entity.getTimeUpdate())
                .withBreakpoint(prepareBreakpoint(entity.getBreakpoint()))
                .build();
    }

    private OperationStatus operationStatus(final String operationStatus) {
        return OperationStatus.operationStatus(operationStatus);
    }

    private OperationType operationType(final OperationTypeV2 operationType) {
        return operationType != null
                ? OperationType.operationType(operationType.getMessage())
                : null;
    }

    private Breakpoint prepareBreakpoint(final BreakpointV2 breakpoint) {
        if (breakpoint != null && breakpoint.getType() != null) {
            return new Breakpoint(Breakpoint.Type.valueOf(breakpoint.getType().toString()), breakpoint.getId(), breakpoint.getDescription());
        }
        return null;
    }
}
