/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobExecutionPolicy.*;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobExecutionPolicy;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportServiceSpecification;
import com.ericsson.oss.services.cm.bulkimport.execution.UnsyncNodeExecPolicy;

/**
 * Mapper for job execution policy to ImportServiceSpecification fields and vica versa.
 */
public final class JobExecutionPolicyMapper {
    private JobExecutionPolicyMapper() {
    }

    public static JobExecutionPolicy[] convert(final ImportServiceSpecification importServiceSpecification) {
        return new JobExecutionPolicy[] {
                JobExecutionPolicy.executionPolicy(importServiceSpecification.getContinueOnErrorLevel()),
                importServiceSpecification.isParallelExecution() ? PARALLEL : SEQUENTIAL,
                JobExecutionPolicy.executionPolicy(importServiceSpecification.getUnsyncNodeExecPolicy().getUnsyncNodeExecPolicy())};
    }

    public static String getContinueOnErrorLevel(final JobExecutionPolicy... jobExecutionPolicies) {
        if (jobExecutionPolicies != null) {
            final List<JobExecutionPolicy> jobExecutionPoliciesList = Arrays.asList(jobExecutionPolicies);
            if (jobExecutionPoliciesList.contains(CONTINUE_ON_ERROR_NODE)) {
                return CONTINUE_ON_ERROR_NODE.getExecutionPolicy();
            } else if (jobExecutionPoliciesList.contains(CONTINUE_ON_ERROR_OPERATION)) {
                return CONTINUE_ON_ERROR_OPERATION.getExecutionPolicy();
            } else if (jobExecutionPoliciesList.contains(JobExecutionPolicy.STOP_ON_ERROR)) {
                return STOP_ON_ERROR.getExecutionPolicy();
            } else if (jobExecutionPoliciesList.contains(PARALLEL)) {
                //PARALLEL implies NODE
                return JobExecutionPolicy.CONTINUE_ON_ERROR_NODE.getExecutionPolicy();
            } else if (jobExecutionPoliciesList.contains(SEQUENTIAL)) {
                //SEQUENTIAL implies STOP
                return JobExecutionPolicy.STOP_ON_ERROR.getExecutionPolicy();
            }
        }
        return null;
    }

    public static Boolean isParallel(final JobExecutionPolicy... jobExecutionPolicies) {
        if (jobExecutionPolicies != null) {
            final List<JobExecutionPolicy> jobExecutionPoliciesList = Arrays.asList(jobExecutionPolicies);
            if (jobExecutionPoliciesList.contains(PARALLEL)) {
                return true;
            } else if (jobExecutionPoliciesList.contains(SEQUENTIAL)) {
                return false;
            } else if (jobExecutionPoliciesList.contains(STOP_ON_ERROR)) {
                //STOP implies SEQUENTIAL
                return false;
            } else if (jobExecutionPoliciesList.contains(CONTINUE_ON_ERROR_NODE) || jobExecutionPoliciesList.contains(CONTINUE_ON_ERROR_OPERATION)) {
                //NODE implies PARALLEL
                //OPERATION implies PARALLEL
                return true;
            }
        }
        return null;
    }

    public static UnsyncNodeExecPolicy getUnsyncNodeExecPolicy(final JobExecutionPolicy... jobExecutionPolicies) {
        if (jobExecutionPolicies != null) {
            final List<JobExecutionPolicy> jobExecutionPoliciesList = Arrays.asList(jobExecutionPolicies);
            if (jobExecutionPoliciesList.contains(JobExecutionPolicy.EXEC_UNSYNC_NODES)) {
                return UnsyncNodeExecPolicy.EXEC_UNSYNC_NODES;
            } else if (jobExecutionPoliciesList.contains(JobExecutionPolicy.SKIP_UNSYNC_NODES)) {
                return UnsyncNodeExecPolicy.SKIP_UNSYNC_NODES;
            }
        }
        return null;
    }
}
