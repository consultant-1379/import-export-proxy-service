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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.ejb;

import static com.ericsson.oss.services.cm.bulkimport.api.CmBulkImportCollectionResponse.importCollectionResponse;
import static com.ericsson.oss.services.cm.bulkimport.api.CmBulkImportResponse.importResponse;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.bulkimport.api.CmBulkImportCollectionResponse;
import com.ericsson.oss.services.cm.bulkimport.api.CmBulkImportResponse;
import com.ericsson.oss.services.cm.bulkimport.api.CmBulkImportResponse.StatusCode;
import com.ericsson.oss.services.cm.bulkimport.api.CmBulkImportService;
import com.ericsson.oss.services.cm.bulkimport.api.ProxyCmBulkImportService;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.AdditionalJobInfoDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportFileDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportInvocationDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobFailureDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobSummaryDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationFailureDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationWarningDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.operation.OperationDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportFileService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportJobService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportOperationService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * Implementation class for {@code CmBulkImportService}. This class provides the implementation for
 * {@link CmBulkImportService}.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CmBulkImportServiceBean implements CmBulkImportService {

    @EServiceRef
    ProxyCmBulkImportService importService;

    @Inject
    CmBulkImportJobService jobService;

    @Inject
    CmBulkImportFileService fileService;

    @Inject
    CmBulkImportOperationService operationService;

    @Override
    public CmBulkImportResponse<ImportJobDto> createJob(final ImportJobDto importJob) {
        return importService.createJob(importJob);
    }

    @Override
    public CmBulkImportResponse<ImportJobDto> getJob(final Long jobId) {
        final Response<ImportJobDto> response = jobService.getJob(jobId);
        return importResponse(response.getContent())
                .withStatusCode(response.getContent() != null ? StatusCode.OK : StatusCode.NOT_FOUND)
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportJobDto> getAllJobs(final Criteria<ImportJobDto> criteria) {
        final CollectionResponse<ImportJobDto> response = jobService.getAllJobs(criteria);
        final List<ImportJobDto> importJobs = response.getContent();
        return importCollectionResponse(importJobs)
                .withStatusCode(!importJobs.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportResponse<ImportInvocationDto> invokeJob(final ImportInvocationDto invocationDto) {
        return importService.invokeJob(invocationDto);
    }

    @Override
    public CmBulkImportResponse<ImportJobDto> cancelJob(final Long jobId) {
        return importService.cancelJob(jobId);
    }

    @Override
    public CmBulkImportResponse<ImportOperationDto> getOperation(final Long operationId) {
        final Response<ImportOperationDto> response = operationService.getOperation(operationId);
        return importResponse(response.getContent())
                .withStatusCode(response.getContent() != null ? StatusCode.OK : StatusCode.NOT_FOUND)
                .build();
    }

    @Override
    public CmBulkImportResponse<Void> deleteJob(final Long jobId) {
        return importService.deleteJob(jobId);
    }

    @Override
    public CmBulkImportResponse<Void> unsetInvocationSchedule(final Long jobId, final Long invocationId) {
        return importService.unsetInvocationSchedule(jobId, invocationId);
    }

    @Override
    public CmBulkImportCollectionResponse<ImportOperationDto> getAllOperations(final Criteria<ImportOperationDto> criteria) {
        final CollectionResponse<ImportOperationDto> response = operationService.getAllOperations(criteria);
        final List<ImportOperationDto> importJobs = response.getContent();
        return importCollectionResponse(importJobs)
                .withStatusCode(!importJobs.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportResponse<ImportFileDto> createFile(final ImportFileDto importFileDto) {
        return importService.createFile(importFileDto);
    }

    @Override
    public CmBulkImportResponse<ImportFileDto> getFile(final Long fileId) {
        final Response<ImportFileDto> response = fileService.getFile(fileId);
        return importResponse(response.getContent())
                .withStatusCode(response.getContent() != null ? StatusCode.OK : StatusCode.NOT_FOUND)
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportFileDto> getAllFiles(final Criteria<ImportFileDto> criteria) {
        final CollectionResponse<ImportFileDto> response = fileService.getAllFiles(criteria);
        final List<ImportFileDto> importFiles = response.getContent();
        return importCollectionResponse(importFiles)
                .withStatusCode(!importFiles.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportJobSummaryDto> getJobSummary(final Criteria<ImportJobSummaryDto> criteria) {
        final CollectionResponse<ImportJobSummaryDto> response = jobService.getJobSummary(criteria);
        final List<ImportJobSummaryDto> jobSummary = response.getContent();
        return importCollectionResponse(jobSummary)
                .withStatusCode(!jobSummary.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportOperationAttributeDto> getAllOperationAttributes(
            final Criteria<ImportOperationAttributeDto> criteria) {
        final CollectionResponse<ImportOperationAttributeDto> response = operationService.getAllOperationAttributes(criteria);
        final List<ImportOperationAttributeDto> attributes = response.getContent();
        return importCollectionResponse(attributes)
                .withStatusCode(!attributes.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportOperationAttributeDto>
            getAllOperationCurrentAttributes(final Criteria<ImportOperationAttributeDto> criteria) {
        final CollectionResponse<ImportOperationAttributeDto> response = operationService.getAllOperationCurrentAttributes(criteria);
        final List<ImportOperationAttributeDto> attributes = response.getContent();
        return importCollectionResponse(attributes)
                .withStatusCode(!attributes.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportOperationFailureDto> getAllOperationFailures(final Criteria<ImportOperationFailureDto> criteria) {
        final CollectionResponse<ImportOperationFailureDto> response = operationService.getAllOperationFailures(criteria);
        final List<ImportOperationFailureDto> failures = response.getContent();
        return importCollectionResponse(failures)
                .withStatusCode(!failures.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportJobFailureDto> getAllJobFailures(final Criteria<ImportJobFailureDto> criteria) {
        final CollectionResponse<ImportJobFailureDto> response = jobService.getAllJobFailures(criteria);
        final List<ImportJobFailureDto> failures = response.getContent();
        return importCollectionResponse(failures)
                .withStatusCode(!failures.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportCollectionResponse<String> getAllNodes(final Long jobId) {
        final CollectionResponse<String> response = jobService.getAllNodes(jobId);
        return importCollectionResponse(response.getContent())
                .withStatusCode(StatusCode.OK)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportResponse<String> createDpsGroup(final Long jobId) {
        final Response<String> response = jobService.createDpsGroup(jobId);
        return importResponse(response.getContent()).ok().build();
    }

    @Override
    public CmBulkImportResponse<Integer> getUnsyncNodesCount(final Long jobId) {
        final Response<Integer> response = jobService.getUnsyncNodesCount(jobId);
        return importResponse(response.getContent()).ok().build();
    }

    @Override
    public CmBulkImportResponse<Void> createImportOperations(final Long jobId, final List<OperationDto> list) {
        return importService.createImportOperations(jobId, list);
    }

    @Override
    public CmBulkImportCollectionResponse<ImportOperationWarningDto> getAllOperationsWarnings(final Criteria<ImportOperationWarningDto> criteria) {
        final CollectionResponse<ImportOperationWarningDto> response = operationService.getAllOperationWarnings(criteria);
        final List<ImportOperationWarningDto> warnings = response.getContent();
        return importCollectionResponse(warnings)
                .withStatusCode(!warnings.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .withTotalCount(response.getTotalCount())
                .build();
    }

    @Override
    public CmBulkImportResponse<AdditionalJobInfoDto> getAdditionalJobInfo(final Long jobId) {
        final Response<AdditionalJobInfoDto> response = jobService.getAdditionalJobInfo(jobId);
        return importResponse(response.getContent()).ok().build();
    }

    @Override
    public CmBulkImportCollectionResponse<ImportOperationAttributeDto>
            getAllOperationPersistentCurrentAttributes(final Criteria<ImportOperationAttributeDto> criteria) {
        final CollectionResponse<ImportOperationAttributeDto> response = operationService.getAllOperationPersistentCurrentAttributes(criteria);
        final List<ImportOperationAttributeDto> attributes = response.getContent();
        return importCollectionResponse(attributes)
                .withStatusCode(!attributes.isEmpty() ? StatusCode.OK : StatusCode.NO_CONTENT)
                .build();
    }

    @Override
    public CmBulkImportResponse<Long> exportJob(final Long importJobId) {
        return importService.exportJob(importJobId);
    }

    @Override
    public CmBulkImportResponse<String> getJobExportStatus(final Long importJobId, final Long jobExportId) {
        final String jobExportStatus = jobService.getJobExportStatus(importJobId, jobExportId).getContent();
        return importResponse(jobExportStatus)
                .withStatusCode(jobExportStatus == null ? StatusCode.NOT_FOUND : StatusCode.OK)
                .build();
    }

    @Override
    public CmBulkImportResponse<String> getExportFilePath(final Long importJobId, final Long jobExportId) {
        final String filePath = jobService.getExportFilePath(importJobId, jobExportId).getContent();
        return importResponse(filePath)
                .withStatusCode(filePath == null ? StatusCode.NOT_FOUND : StatusCode.OK)
                .build();
    }
}
