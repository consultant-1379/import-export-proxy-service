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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobsummary;

import static com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.importOperationCriteria;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobsummary.GetImportJobSummaryResponse.getImportJobSummaryResponse;

import java.util.List;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobSummaryDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportJobSummaryPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService.ImportOperationCriteria;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobOperationSummary;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class for ImportJobSummary request.
 */
@Handle(GetImportJobSummaryRequest.class)
public class GetImportJobSummaryRequestHandler implements RequestHandler<GetImportJobSummaryRequest, GetImportJobSummaryResponse> {

    @Inject
    @Any
    Instance<RequestValidator<GetImportJobSummaryRequest>> validators;
    @Inject
    ImportJobSummaryPersistenceService summaryPersistence;
    @Inject
    ImportJobSummaryConverter jobSummaryConverter;

    @Override
    public GetImportJobSummaryResponse handle(final GetImportJobSummaryRequest request) {
        final List<ImportJobOperationSummary> summaries = summaryPersistence.getPerOperationType(criteria(request.getCriteria()));
        final List<ImportJobSummaryDto> jobSummaries = jobSummaryConverter.convert(summaries);
        return getImportJobSummaryResponse()
                .withContent(jobSummaries)
                .build();
    }

    @Override
    public Iterable<RequestValidator<GetImportJobSummaryRequest>> validators() {
        return validators;
    }

    Criteria<ImportOperation> criteria(final Criteria<ImportJobSummaryDto> criteria) {
        final ImportOperationCriteria importOperationCriteria = importOperationCriteria();
        for (final Restriction<?> restriction : criteria.getRestrictions()) {
            final String attribute = restriction.getAttribute();
            if ("job.id".equalsIgnoreCase(attribute)) {
                importOperationCriteria.addRestriction(ImportOperation.JOB_ID_FIELD, restriction.getOperator(), restriction.getArguments());
            }
        }
        return importOperationCriteria.build();
    }
}
