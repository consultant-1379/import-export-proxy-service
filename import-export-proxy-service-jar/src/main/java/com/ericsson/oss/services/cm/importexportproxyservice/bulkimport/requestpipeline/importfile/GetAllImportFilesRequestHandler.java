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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile;

import static com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService.importJobCriteria;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile.GetAllImportFilesResponse.getAllImportFilesResponse;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportFileDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobPersistenceService.ImportJobCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;
import com.google.common.base.Function;
import com.google.common.base.Predicates;

/**
 * Handler class for GET all ImportFiles Request.
 */
@Handle(GetAllImportFilesRequest.class)
public class GetAllImportFilesRequestHandler implements RequestHandler<GetAllImportFilesRequest, GetAllImportFilesResponse> {
    private static final Long TOTAL_COUNT_NOT_REQUIRE = null;

    @Inject
    @Any
    Instance<RequestValidator<GetAllImportFilesRequest>> validators;

    @Inject
    ImportFileConverter transformer;

    @Inject
    ImportJobPersistenceService jobPersistence;

    @Override
    public GetAllImportFilesResponse handle(final GetAllImportFilesRequest request) {
        final List<ImportFileDto> result = getAll(request);
        final Long totalCount = totalCount(request);
        return getAllImportFilesResponse()
                .withContent(result)
                .withTotalCount(totalCount)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetAllImportFilesRequest>> validators() {
        return validators;
    }

    List<ImportFileDto> getAll(final GetAllImportFilesRequest request) {
        final List<ImportFileDto> result = new ArrayList<>();
        if (request.getCriteria().getLimit() != 0) {
            final Criteria<ImportJob> criteria = criteria(request.getCriteria());
            final List<ImportJob> jobs = jobPersistence.getAllJobs(criteria);
            final List<ImportFileDto> importFiles = transform(jobs, new Function<ImportJob, ImportFileDto>() {
                @Override
                public ImportFileDto apply(final ImportJob job) {
                    return transformer.convert(job);
                }
            });
            result.addAll(filter(importFiles, Predicates.<ImportFileDto>notNull()));
        }
        return result;
    }

    Long totalCount(final GetAllImportFilesRequest request) {
        if (!request.getCriteria().isTotalCountRequired()) {
            return TOTAL_COUNT_NOT_REQUIRE;
        }
        final Criteria<ImportJob> criteria = criteria(request.getCriteria());
        return jobPersistence.totalCount(criteria);
    }

    Criteria<ImportJob> criteria(final Criteria<ImportFileDto> criteria) {
        final ImportJobCriteria jobCriteria = importJobCriteria();
        for (final Restriction<?> restriction : criteria.getRestrictions()) {
            final String attribute = restriction.getAttribute();
            if ("id".equalsIgnoreCase(attribute)) {
                jobCriteria.id.restriction(restriction.getOperator(), (Object[]) restriction.getArguments());
            }
            if ("job.id".equalsIgnoreCase(attribute)) {
                jobCriteria.id.restriction(restriction.getOperator(), (Object[]) restriction.getArguments());
            }
        }
        jobCriteria.withSortParams(criteria.getSortBy(), criteria.getOrderBy());
        jobCriteria.withPageParams(criteria.getOffset(), criteria.getLimit());
        if (criteria.isTotalCountRequired()) {
            jobCriteria.totalCountRequired();
        }
        return jobCriteria.build();
    }
}
