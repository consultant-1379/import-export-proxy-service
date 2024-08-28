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

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CriteriaBuilder.buildCriteria;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationwarnings.GetAllImportOperationWarningsResponse.getAllImportOperationWarningsResponse;
import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationWarningDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationFailure;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Request handler class for import operation warning request.
 */
@Handle(GetAllImportOperationWarningsRequest.class)
public class GetAllImportOperationWarningsRequestHandler implements
        RequestHandler<GetAllImportOperationWarningsRequest, GetAllImportOperationWarningsResponse> {
    @Inject
    @Any
    Instance<RequestValidator<GetAllImportOperationWarningsRequest>> validators;

    @Inject
    ImportOperationPersistenceService operationPersistence;

    @Inject
    ImportOperationWarningConverter warningConverter;

    @Override
    public Iterable<RequestValidator<GetAllImportOperationWarningsRequest>> validators() {
        return validators;
    }

    @Override
    public GetAllImportOperationWarningsResponse handle(final GetAllImportOperationWarningsRequest request) {
        final List<ImportOperationWarningDto> result = getAll(request);
        return getAllImportOperationWarningsResponse()
                .withContent(result)
                .build();
    }

    private List<ImportOperationWarningDto> getAll(final GetAllImportOperationWarningsRequest request) {
        final List<ImportOperationWarningDto> result = new ArrayList<>();
        final Criteria<ImportOperation> criteria = buildCriteria(request.getCriteria(), "operation.id");
        final List<ImportOperation> operations = operationPersistence.getAll(criteria);
        operations.forEach(importOperation -> {
            final List<ImportOperationFailure> failures = importOperation.getFailures();
            if (failures != null && !failures.isEmpty()) {
                result.addAll(transform(failures, importOperationFailure -> warningConverter.convert(importOperationFailure)));
            }
        });
        return result;
    }
}
