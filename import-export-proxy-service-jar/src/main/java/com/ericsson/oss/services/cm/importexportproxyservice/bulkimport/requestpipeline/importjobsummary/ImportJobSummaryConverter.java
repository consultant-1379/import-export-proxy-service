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

import static com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus.EXECUTED;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus.EXECUTION_ERROR;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus.INVALID;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus.operationStatus;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.OperationType.operationType;
import static com.ericsson.oss.services.cm.bulkimport.api.dto.jobsummary.JobSummaryPerOperations.perOperationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.cm.bulkimport.api.domain.OperationStatus;
import com.ericsson.oss.services.cm.bulkimport.api.domain.OperationType;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobSummaryDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobOperationSummary;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

/**
 * Converter class fro ImportJobSummary.
 */
public class ImportJobSummaryConverter implements Converter<List<ImportJobOperationSummary>, List<ImportJobSummaryDto>> {
    @Override
    public List<ImportJobSummaryDto> convert(final List<ImportJobOperationSummary> summaries) {
        final Map<Long, EnumMap<OperationType, EnumMap<OperationStatus, Integer>>> groupedSummaries = group(summaries);
        final List<ImportJobSummaryDto> jobSummary = new ArrayList<>();
        for (final Map.Entry<Long, EnumMap<OperationType, EnumMap<OperationStatus, Integer>>> entry : groupedSummaries.entrySet()) {
            jobSummary.addAll(transform(entry.getKey(), entry.getValue()));
        }
        return jobSummary;
    }

    private Map<Long, EnumMap<OperationType, EnumMap<OperationStatus, Integer>>> group(final List<ImportJobOperationSummary> summaries) {
        final Map<Long, EnumMap<OperationType, EnumMap<OperationStatus, Integer>>> groupedSummaries = new HashMap();
        for (final ImportJobOperationSummary summary : summaries) {
            final Long jobId = summary.getJobId();
            if (!groupedSummaries.containsKey(jobId)) {
                groupedSummaries.put(jobId, new EnumMap<OperationType, EnumMap<OperationStatus, Integer>>(OperationType.class));
            }
            final EnumMap<OperationType, EnumMap<OperationStatus, Integer>> operationTypeSummary = groupedSummaries.get(jobId);
            final OperationType operationType = operationType(summary.getOperationType().name());
            if (!operationTypeSummary.containsKey(operationType)) {
                operationTypeSummary.put(operationType, new EnumMap<OperationStatus, Integer>(OperationStatus.class));
            }
            final EnumMap<OperationStatus, Integer> operationStatusSummary = operationTypeSummary.get(operationType);
            final OperationStatus operationStatus = operationStatus(summary.getOperationStatus());
            operationStatusSummary.put(operationStatus, summary.getCount());
        }
        return groupedSummaries;
    }

    private List<ImportJobSummaryDto> transform(final Long jobId, final EnumMap<OperationType, EnumMap<OperationStatus, Integer>> summaries) {
        final List<ImportJobSummaryDto> jobSummary = new ArrayList<>();
        for (final EnumMap.Entry<OperationType, EnumMap<OperationStatus, Integer>> entry : summaries.entrySet()) {
            final OperationType operationType = entry.getKey();
            final EnumMap<OperationStatus, Integer> summary = entry.getValue();
            jobSummary.add(perOperationType(operationType)
                    .withJobId(jobId)
                    .withParsed(getParsed(summary))
                    .withValid(getValid(summary))
                    .withValidationErrors(getValidationErrors(summary))
                    .withExecuted(getExecuted(summary))
                    .withExecutionErrors(getExecutionErrors(summary))
                    .build()
            );
        }
        return jobSummary;
    }

    private int getParsed(final EnumMap<OperationStatus, Integer> summary) {
        int parsed = 0;
        for (final EnumMap.Entry<OperationStatus, Integer> entry : summary.entrySet()) {
            parsed += entry.getValue();
        }
        return parsed;
    }

    private int getValid(final EnumMap<OperationStatus, Integer> summary) {
        int valid = 0;
        for (final EnumMap.Entry<OperationStatus, Integer> entry : summary.entrySet()) {
            switch (entry.getKey()) {
                case VALID:
                case EXECUTED:
                case EXECUTION_ERROR:
                case EXECUTION_SKIPPED:
                    valid += entry.getValue();
                    break;
                default:
                    break;
            }
        }
        return valid;
    }

    private int getValidationErrors(final EnumMap<OperationStatus, Integer> summary) {
        int invalid = 0;
        for (final EnumMap.Entry<OperationStatus, Integer> entry : summary.entrySet()) {
            if (INVALID == entry.getKey()) {
                invalid += entry.getValue();
            }
        }
        return invalid;
    }

    private int getExecuted(final EnumMap<OperationStatus, Integer> summary) {
        int executed = 0;
        for (final EnumMap.Entry<OperationStatus, Integer> entry : summary.entrySet()) {
            if (EXECUTED == entry.getKey()) {
                executed += entry.getValue();
            }
        }
        return executed;
    }

    private int getExecutionErrors(final EnumMap<OperationStatus, Integer> summary) {
        int executed = 0;
        for (final EnumMap.Entry<OperationStatus, Integer> entry : summary.entrySet()) {
            if (EXECUTION_ERROR == entry.getKey()) {
                executed += entry.getValue();
            }
        }
        return executed;
    }
}
