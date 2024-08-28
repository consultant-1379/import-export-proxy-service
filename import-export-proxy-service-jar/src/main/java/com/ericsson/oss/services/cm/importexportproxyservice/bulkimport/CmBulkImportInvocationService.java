/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importinvocation.InvokeImportJobRequest.invokeImportJobRequest;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportInvocationDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestPipeline;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importinvocation.InvokeImportJobRequest;

/**
 * CmBulkImportInvocationService to invoke import job.
 */
public class CmBulkImportInvocationService {

    @Inject
    RequestPipeline requestPipeline;

    public Response<ImportInvocationDto> invokeJob(final ImportInvocationDto invocationDto) {
        final InvokeImportJobRequest request = invokeImportJobRequest()
                .withJobId(invocationDto.getJobId())
                .withJobInvocationFlow(invocationDto.getJobInvocationFlow())
                .withExecutionPolicy(invocationDto.getJobExecutionPolicy())
                .withJobValidationPolicy(invocationDto.getJobValidationPolicy())
                .withScheduleTime(invocationDto.getScheduleTime())
                .build();
        return requestPipeline.process(request);
    }
}
