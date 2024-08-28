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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;


import com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportServiceSpecification;

/**
 * Mapper for job validation policy to instance validation on/off and vice versa.
 */
@SuppressWarnings("squid:S2447")
public final class JobValidationPolicyMapper {
    private JobValidationPolicyMapper() {
    }

    public static JobValidationPolicy[] convert(final ImportServiceSpecification importServiceSpecification) {
        final JobValidationPolicy[] policies = new JobValidationPolicy[3];
        policies[0] = importServiceSpecification.isValidateInstances() ? JobValidationPolicy.INSTANCE_VALIDATION :
                JobValidationPolicy.NO_INSTANCE_VALIDATION;
        policies[1] = importServiceSpecification.isAuditSelected() ? JobValidationPolicy.ENABLE_AUDIT :
                JobValidationPolicy.DISABLE_AUDIT;
        policies[2] = importServiceSpecification.isValidateInNode() ? JobValidationPolicy.NODE_BASED_VALIDATION :
                JobValidationPolicy.NO_NODE_BASED_VALIDATION;
        return  policies;
    }

    public static Boolean isValidateInstances(final JobValidationPolicy... jobValidationPolicy) {
        if (jobValidationPolicy != null) {
            final List<JobValidationPolicy> jobValidationPolicyList = Arrays.asList(jobValidationPolicy);
            if (jobValidationPolicyList.contains(JobValidationPolicy.INSTANCE_VALIDATION)) {
                return true;
            } else if (jobValidationPolicyList.contains(JobValidationPolicy.NO_INSTANCE_VALIDATION)) {
                return false;
            }
        }
        return null;
    }

    public static Boolean isValidateInNode(final JobValidationPolicy... jobValidationPolicy) {
        if (ArrayUtils.isEmpty(jobValidationPolicy)) {
            return null;
        } else {
            return ArrayUtils.contains(jobValidationPolicy, JobValidationPolicy.NODE_BASED_VALIDATION) && !ArrayUtils
                    .contains(jobValidationPolicy, JobValidationPolicy.NO_NODE_BASED_VALIDATION);
        }
    }

    public static Boolean isAuditSelected(final JobValidationPolicy... jobValidationPolicy) {
        if (jobValidationPolicy != null) {
            final List<JobValidationPolicy> jobValidationPolicyList = Arrays.asList(jobValidationPolicy);
            if (jobValidationPolicyList.contains(JobValidationPolicy.ENABLE_AUDIT)) {
                return true;
            } else if (jobValidationPolicyList.contains(JobValidationPolicy.DISABLE_AUDIT)) {
                return false;
            }
        }
        return null;
    }
}
