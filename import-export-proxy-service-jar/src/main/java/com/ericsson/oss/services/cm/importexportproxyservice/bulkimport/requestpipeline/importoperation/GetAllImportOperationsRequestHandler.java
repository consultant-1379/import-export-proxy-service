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

import static java.util.Arrays.asList;

import static com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.importOperationCriteria;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation.GetAllImportOperationsResponse.getAllImportOperationsResponse;
import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.ImportOperationCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Handler class for GET ImportJobs request.
 */
@Handle(GetAllImportOperationsRequest.class)
public class GetAllImportOperationsRequestHandler implements RequestHandler<GetAllImportOperationsRequest, GetAllImportOperationsResponse> {
    private static final Long TOTAL_COUNT_NOT_REQUIRE = null;

    @Inject
    @Any
    Instance<RequestValidator<GetAllImportOperationsRequest>> validators;

    @Inject
    ImportOperationConverter operationConverter;

    @Inject
    ImportOperationPersistenceService operationPersistence;

    @Override
    public GetAllImportOperationsResponse handle(final GetAllImportOperationsRequest request) {
        final List<ImportOperationDto> result = getAll(request);
        final Long totalCount = totalCount(request);
        return getAllImportOperationsResponse()
                .withContent(result)
                .withTotalCount(totalCount)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportOperationsRequest>> validators() {
        return validators;
    }

    private List<ImportOperationDto> getAll(final GetAllImportOperationsRequest request) {
        final List<ImportOperationDto> result = new ArrayList<>();
        if (request.getCriteria().getLimit() != 0) {
            final Criteria<ImportOperation> criteria = criteria(request.getCriteria());
            final List<ImportOperation> jobs = operationPersistence.getAll(criteria);
            result.addAll(transform(jobs, new Function<ImportOperation, ImportOperationDto>() {
                @Override
                public ImportOperationDto apply(final ImportOperation job) {
                    return operationConverter.convert(job);
                }
            }));
        }
        return result;
    }

    Long totalCount(final GetAllImportOperationsRequest request) {
        if (!request.getCriteria().isTotalCountRequired()) {
            return TOTAL_COUNT_NOT_REQUIRE;
        }
        final Criteria<ImportOperation> criteria = criteria(request.getCriteria());
        return operationPersistence.totalCount(criteria);
    }

    Criteria<ImportOperation> criteria(final Criteria<ImportOperationDto> criteria) {
        final ImportOperationCriteria operationCriteria = importOperationCriteria();
        for (final Restriction<?> restriction : criteria.getRestrictions()) {
            final String attribute = restriction.getAttribute();
            if ("id".equalsIgnoreCase(attribute)) {
                operationCriteria.addRestriction(restriction);
            }
            if ("job.id".equalsIgnoreCase(attribute)) {
                operationCriteria.addRestriction(ImportOperation.JOB_ID_FIELD, restriction.getOperator(), restriction.getArguments());
            }
            if ("status".equalsIgnoreCase(attribute)) {
                final List<String> statuses = Lists.transform(asList(restriction.getArguments()),
                        new Function<Object, String>() {
                            @Override
                            public String apply(final Object status) {
                                return status != null ? ((OperationStatus) status).name() : null;
                            }
                        });
                final String[] arguments = statuses.toArray(new String[statuses.size()]);
                operationCriteria.addRestriction(ImportOperation.OPERATION_STATUS_FIELD, restriction.getOperator(), arguments);
            }
        }
        operationCriteria.withPageParams(criteria.getOffset(), criteria.getLimit());
        if (criteria.isTotalCountRequired()) {
            operationCriteria.totalCountRequired();
        }
        return operationCriteria.build();
    }
}
