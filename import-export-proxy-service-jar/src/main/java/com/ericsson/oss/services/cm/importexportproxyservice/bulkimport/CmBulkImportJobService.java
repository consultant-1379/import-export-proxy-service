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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.CreateDpsGroupRequest.createDpsGroupRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetAllImportJobsRequest.getAllImportJobsRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetAllImportNodesRequest.getAllImportNodes;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetImportJobRequest.getImportJobRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetImportJobUnsyncNodesCountRequest.getImportJobUnsyncNodesCount;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobadditionalinfo.GetAdditionalJobInfoRequest.getAdditionalJobInfoRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport.GetImportJobExportDownloadRequest.getImportJobExportDownload;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport.GetImportJobExportStatusRequest.getImportJobExportStatus;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobsummary.GetImportJobSummaryRequest.getImportJobSummaryRequest;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.AdditionalJobInfoDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobFailureDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobSummaryDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestPipeline;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.CreateDpsGroupRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetAllImportJobsRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetAllImportNodesRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetImportJobRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.GetImportJobUnsyncNodesCountRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobadditionalinfo.GetAdditionalJobInfoRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport.GetImportJobExportDownloadRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobexport.GetImportJobExportStatusRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobjailures.GetAllImportJobFailuresRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobsummary.GetImportJobSummaryRequest;

/**
 * CmBulkImportJobService to preform REST requests.
 */
public class CmBulkImportJobService {
    @Inject
    RequestPipeline requestPipeline;

    public Response<ImportJobDto> getJob(final Long jobId) {
        final GetImportJobRequest request = getImportJobRequest(jobId);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportJobDto> getAllJobs(final Criteria<ImportJobDto> criteria) {
        final GetAllImportJobsRequest request = getAllImportJobsRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportJobSummaryDto> getJobSummary(final Criteria<ImportJobSummaryDto> criteria) {
        final GetImportJobSummaryRequest request = getImportJobSummaryRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportJobFailureDto> getAllJobFailures(final Criteria<ImportJobFailureDto> criteria) {
        final GetAllImportJobFailuresRequest request = GetAllImportJobFailuresRequest.getAllImportJobFailuresRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<String> getAllNodes(final Long jobId) {
        final GetAllImportNodesRequest request = getAllImportNodes(jobId);
        return requestPipeline.process(request);
    }

    public Response<Integer> getUnsyncNodesCount(final Long jobId) {
        final GetImportJobUnsyncNodesCountRequest request = getImportJobUnsyncNodesCount(jobId);
        return requestPipeline.process(request);
    }

    public Response<AdditionalJobInfoDto> getAdditionalJobInfo(final Long jobId) {
        final GetAdditionalJobInfoRequest request = getAdditionalJobInfoRequest().withJobId(jobId).build();
        return requestPipeline.process(request);
    }

    public Response<String> getJobExportStatus(final Long importJobId, final Long jobExportId) {
        final GetImportJobExportStatusRequest request = getImportJobExportStatus()
                .withImportJobId(importJobId)
                .withJobExportId(jobExportId)
                .build();
        return requestPipeline.process(request);
    }

    public Response<String> getExportFilePath(final Long importJobId, final Long jobExportId) {
        final GetImportJobExportDownloadRequest request = getImportJobExportDownload()
                .withImportJobId(importJobId)
                .withJobExportId(jobExportId)
                .build();
        return requestPipeline.process(request);
    }

    public Response<String> createDpsGroup(final Long jobId) {
        final CreateDpsGroupRequest request = createDpsGroupRequest()
                .withJobId(jobId)
                .build();
        return requestPipeline.process(request);
    }
}
