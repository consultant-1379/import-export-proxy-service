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

import static com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobFailurePersistenceService.importJobFailureCriteria;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobjailures.GetAllImportJobFailuresResponse.getAllImportJobFailuresResponse;
import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobFailureDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobFailurePersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobFailurePersistenceService.ImportJobFailureCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobFailure;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

import com.google.common.base.Function;

/**
 * Handler class for GET ImportJobs request.
 */
@Handle(GetAllImportJobFailuresRequest.class)
public class GetAllImportJobFailuresRequestHandler implements RequestHandler<GetAllImportJobFailuresRequest, GetAllImportJobFailuresResponse> {
    @Inject
    @Any
    Instance<RequestValidator<GetAllImportJobFailuresRequest>> validators;

    @Inject
    ImportJobFailureConverter failureConverter;

    @Inject
    ImportJobFailurePersistenceService failurePersistenceService;

    @Override
    public GetAllImportJobFailuresResponse handle(final GetAllImportJobFailuresRequest request) {
        final List<ImportJobFailureDto> result = getAll(request);
        return getAllImportJobFailuresResponse()
                .withContent(result)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportJobFailuresRequest>> validators() {
        return validators;
    }

    private List<ImportJobFailureDto> getAll(final GetAllImportJobFailuresRequest request) {
        final List<ImportJobFailureDto> result = new ArrayList<>();
        final Criteria<ImportJobFailure> criteria = criteria(request.getCriteria());
        final List<ImportJobFailure> failures = failurePersistenceService.getAll(criteria);
        result.addAll(transform(failures, new Function<ImportJobFailure, ImportJobFailureDto>() {
            @Override
            public ImportJobFailureDto apply(final ImportJobFailure failure) {
                return failureConverter.convert(failure);
            }
        }));
        return result;
    }

    Criteria<ImportJobFailure> criteria(final Criteria<ImportJobFailureDto> criteria) {
        final ImportJobFailureCriteria failureCriteria = importJobFailureCriteria();
        for (final Restriction<?> restriction : criteria.getRestrictions()) {
            final String attribute = restriction.getAttribute();
            if ("job.id".equalsIgnoreCase(attribute)) {
                failureCriteria.jobId.restriction(restriction.getOperator(), restriction.getArguments());
            }
        }
        failureCriteria.withPageParams(criteria.getOffset(), criteria.getLimit());
        if (criteria.isTotalCountRequired()) {
            failureCriteria.totalCountRequired();
        }
        return failureCriteria.build();
    }
}
