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

import static com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto.importJob;

import java.util.Collection;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportJobDto;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportJobSpecification;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Converter class for ImportJob.
 */
public class ImportJobConverter implements Converter<ImportJob, ImportJobDto> {

    @Override
    public ImportJobDto convert(final ImportJob importJob) {
        final ImportJobSpecification jobSpecification = importJob.getJobSpecification();
        return importJob()
                .withId(importJob.getId())
                .withType(importJob.getJobType())
                .withUserid(importJob.getUserName())
                .withName(importJob.getJobName())
                .withCreated(importJob.getTimeStart())
                .withElapsedTime(importJob.getElapsedTimeSeconds())
                .withLastExecutionDate(importJob.getTimeEnd())
                .withLastValidationDate(importJob.getLastValidationTime())
                .withLastValidationStartDate(importJob.getLasttValidationStartTime())
                .withLastExecutionStartDate(importJob.getLastExecutionStartTime())
                .withConfiguration(jobSpecification.getConfiguration())
                .withJobValidationPolicy(JobValidationPolicyMapper.convert(jobSpecification))
                .withJobExecutionPolicy(JobExecutionPolicyMapper.convert(jobSpecification))
                .withStatus(importJob.getStatus())
                .withFailureReason(importJob.getErrorDescription())
                .withScheduleTime(importJob.getScheduleTime())
                .build();
    }

    public Collection<ImportJobDto> convert(final Collection<ImportJob> source) {
        return Collections2.transform(source, new Function<ImportJob, ImportJobDto>() {
            @Override
            public ImportJobDto apply(final ImportJob job) {
                return convert(job);
            }
        });
    }

}
