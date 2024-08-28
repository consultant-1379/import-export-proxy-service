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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importinvocation;

import static com.ericsson.oss.services.cm.bulkimport.api.dto.ImportInvocationDto.importInvocation;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportInvocationDto;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportJobSpecification;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.JobExecutionPolicyMapper;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.JobValidationPolicyMapper;

/**
 * Convert Import job data to ImportInvocation data transfer object.
 */
public class ImportInvocationConverter implements Converter<ImportJob, ImportInvocationDto> {

    @Override
    public ImportInvocationDto convert(final ImportJob importJob) {
        final ImportJobSpecification jobSpecification = importJob.getJobSpecification();
        return importInvocation()
                .withJobId(importJob.getId())
                .withInvocationId(importJob.getId())
                .withFlow(jobSpecification.getJobInvocationFlow())
                .withJobValidationPolicy(JobValidationPolicyMapper.convert(jobSpecification))
                .withJobExecutionPolicy(JobExecutionPolicyMapper.convert(jobSpecification))
                .withScheduleTime(importJob.getScheduleTime())
                .build();
    }
}
