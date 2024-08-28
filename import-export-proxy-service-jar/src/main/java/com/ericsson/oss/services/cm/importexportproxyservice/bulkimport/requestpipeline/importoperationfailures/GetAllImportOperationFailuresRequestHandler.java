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

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CriteriaBuilder.buildCriteria;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationfailures.GetAllImportOperationFailuresResponse.getAllImportOperationFailuresResponse;
import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationFailureDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationFailure;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class for GET ImportJobs request.
 */
@Handle(GetAllImportOperationFailuresRequest.class)
public class GetAllImportOperationFailuresRequestHandler implements
        RequestHandler<GetAllImportOperationFailuresRequest, GetAllImportOperationFailuresResponse> {
    @Inject
    @Any
    Instance<RequestValidator<GetAllImportOperationFailuresRequest>> validators;

    @Inject
    ImportOperationFailureConverter failureConverter;

    @Inject
    ImportOperationPersistenceService operationPersistenceService;

    @Override
    public GetAllImportOperationFailuresResponse handle(final GetAllImportOperationFailuresRequest request) {
        final List<ImportOperationFailureDto> result = getAll(request);
        return getAllImportOperationFailuresResponse()
                .withContent(result)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportOperationFailuresRequest>> validators() {
        return validators;
    }

    private List<ImportOperationFailureDto> getAll(final GetAllImportOperationFailuresRequest request) {
        final List<ImportOperationFailureDto> result = new ArrayList<>();
        final Criteria<ImportOperation> criteria = buildCriteria(request.getCriteria(), "operation.id");
        final List<ImportOperation> operations = operationPersistenceService.getAll(criteria);
        operations.forEach(importOperation -> {
            final List<ImportOperationFailure> failures = importOperation.getFailures();
            if (failures != null && !failures.isEmpty()) {
                result.addAll(transform(failures, importOperationFailure -> failureConverter.convert(importOperationFailure)));
            }
        });
        return result;
    }
}
