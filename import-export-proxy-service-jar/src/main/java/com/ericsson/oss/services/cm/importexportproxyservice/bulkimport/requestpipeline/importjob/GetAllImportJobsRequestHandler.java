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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import static com.ericsson.oss.services.cm.bulkimport.api.criteria.ImportJobCriteria.CREATED;
import static com.ericsson.oss.services.cm.bulkimport.api.criteria.ImportJobCriteria.HAS_ERRORS;
import static com.ericsson.oss.services.cm.bulkimport.api.criteria.ImportJobCriteria.ID;
import static com.ericsson.oss.services.cm.bulkimport.api.criteria.ImportJobCriteria.NAME;
import static com.ericsson.oss.services.cm.bulkimport.api.criteria.ImportJobCriteria.USER_ID;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetAllImportJobsResponse.getAllImportJobsResponse;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService.ImportJobCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

/**
 * Handler class for GET ImportJobs request.
 */
@Handle(GetAllImportJobsRequest.class)
public class GetAllImportJobsRequestHandler implements RequestHandler<GetAllImportJobsRequest, GetAllImportJobsResponse> {
    private static final Long TOTAL_COUNT_NOT_REQUIRED = null;

    @Inject
    @Any
    Instance<RequestValidator<GetAllImportJobsRequest>> validators;

    @Inject
    ImportJobConverter transformer;

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Override
    public GetAllImportJobsResponse handle(final GetAllImportJobsRequest request) {
        final List<ImportJobDto> result = getAll(request);
        final Long totalCount = totalCount(request);
        return getAllImportJobsResponse()
                .withContent(result)
                .withTotalCount(totalCount)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportJobsRequest>> validators() {
        return validators;
    }

    List<ImportJobDto> getAll(final GetAllImportJobsRequest request) {
        final List<ImportJobDto> result = new ArrayList<>();
        final Criteria<ImportJobDto> requestCriteria = request.getCriteria();
        if (requestCriteria.getLimit() != 0) {
            final ImportJobCriteria criteria = criteria(requestCriteria);
            criteria.withSortParams(requestCriteria.getSortBy(), requestCriteria.getOrderBy());
            criteria.withPageParams(requestCriteria.getOffset(), requestCriteria.getLimit());
            final List<ImportJob> jobs = jobPersistence.getAllJobs(criteria.build());
            result.addAll(transformer.convert(jobs));
        }
        return result;
    }

    Long totalCount(final GetAllImportJobsRequest request) {
        final Criteria<ImportJobDto> requestCriteria = request.getCriteria();
        if (!requestCriteria.isTotalCountRequired()) {
            return TOTAL_COUNT_NOT_REQUIRED;
        }
        final ImportJobCriteria criteria = criteria(requestCriteria);
        return jobPersistence.totalCount(criteria.build());
    }

    private ImportJobCriteria criteria(final Criteria<ImportJobDto> requestCriteria) {
        return criteria(requestCriteria.getRestrictions());
    }

    private ImportJobCriteria criteria(final List<Restriction<?>> restrictions) {
        final ImportJobCriteria criteria = ImportJobPersistenceService.importJobCriteria();
        for (final Restriction<?> restriction : restrictions) {
            final String attribute = restriction.getAttribute();
            final Restriction.Operator operator = restriction.getOperator();
            final Object[] arguments = restriction.getArguments();
            final Restriction.RestrictionConfiguration configuration = restriction.getConfiguration();
            if (Restriction.Operator.ALL_OF == operator) {
                criteria.allOf(nestedCondition(arguments));
            } else if (Restriction.Operator.ANY_OF == operator) {
                criteria.anyOf(nestedCondition(arguments));
            } else if (ID.equalsIgnoreCase(attribute)) {
                criteria.id.restriction(operator, arguments);
            } else if (CREATED.equalsIgnoreCase(attribute)) {
                criteria.timeStart.restriction(operator, arguments);
            } else if (USER_ID.equalsIgnoreCase(attribute)) {
                criteria.username.restriction(operator, arguments);
            } else if (NAME.equalsIgnoreCase(attribute)) {
                criteria.jobName.restriction(operator, configuration, arguments);
            } else if (HAS_ERRORS.equalsIgnoreCase(attribute)) {
                criteria.operationStatus.restriction(operator, hasErrors(arguments));
            }
        }
        return criteria;
    }

    private String[] hasErrors(final Object[] arguments) {
        final String[] hasErrors = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            final String str = arguments[i].toString();
            if ("validation-errors".equalsIgnoreCase(str)) {
                hasErrors[i] = OperationStatus.INVALID.name();
            } else if ("execution-errors".equalsIgnoreCase(str)) {
                hasErrors[i] = OperationStatus.EXECUTION_ERROR.name();
            } else {
                hasErrors[i] = str.toUpperCase();
            }
        }
        return hasErrors;
    }

    private <T> ImportJobCriteria nestedCondition(final T[] arguments) {
        return criteria(asList(arguments));
    }

    private <T> List<Restriction<?>> asList(final T[] arguments) {
        return FluentIterable.from(arguments)
                .transform(new Function<T, Restriction<?>>() {
                    @Override
                    public Restriction<?> apply(final T argument) {
                        return argument instanceof Restriction ? (Restriction<?>) argument : null;
                    }
                })
                .filter(Predicates.notNull())
                .toList();
    }
}
