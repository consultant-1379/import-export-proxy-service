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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobExecutionPolicy.*;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy.DISABLE_AUDIT;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy.ENABLE_AUDIT;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy.INSTANCE_VALIDATION;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy.NODE_BASED_VALIDATION;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy.NO_INSTANCE_VALIDATION;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy.NO_NODE_BASED_VALIDATION;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage.responseMessage;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.oss.services.cm.bulkimport.api.domain.JobExecutionPolicy;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobValidationPolicy;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage;
import com.google.common.collect.Collections2;

/**
 * Validation class for ImportImportRequest creation.
 */
public class CreateImportJobRequestValidator implements RequestValidator<CreateImportJobRequest> {

    private static final List<JobExecutionPolicy>
            EXEC_ERROR_POLICIES = Arrays.asList(STOP_ON_ERROR, CONTINUE_ON_ERROR_NODE, CONTINUE_ON_ERROR_OPERATION);
    private static final List<JobExecutionPolicy> EXEC_THREAD_POLICIES = Arrays.asList(SEQUENTIAL, PARALLEL);
    private static final List<JobExecutionPolicy> EXEC_UNSYNC_NODES_POLICIES = Arrays.asList(EXEC_UNSYNC_NODES, SKIP_UNSYNC_NODES);
    private static final List<JobValidationPolicy> VALIDATION_INSTANCE_POLICIES = Arrays.asList(INSTANCE_VALIDATION, NO_INSTANCE_VALIDATION);
    private static final List<JobValidationPolicy> VALIDATION_NODE_BASED_POLICIES = Arrays.asList(NODE_BASED_VALIDATION, NO_NODE_BASED_VALIDATION);
    private static final List<JobValidationPolicy> VALIDATION_AUDIT_POLICIES = Arrays.asList(ENABLE_AUDIT, DISABLE_AUDIT);

    @Override
    public List<ResponseMessage> validate(final CreateImportJobRequest request) {
        final List<ResponseMessage> errors = new ArrayList<>();
        errors.addAll(validateUserIsSet(request.getUserid()));
        errors.addAll(validateNoControversialExecutionPolicies(request.getJobExecutionPolicy()));
        errors.addAll(validateNoControversialValidationPolicies(request.getJobValidationPolicy()));
        return errors;
    }

    private static List<ResponseMessage> validateUserIsSet(final String userId) {
        final List<ResponseMessage> errors = new ArrayList<>();
        if (StringUtils.isBlank(userId)) {
            errors.add(responseMessage()
                    .error(CmBulkImportServiceErrors.INVALID_USERID, userId)
                    .build());
        }
        return errors;
    }

    public static List<ResponseMessage> validateNoControversialExecutionPolicies(final JobExecutionPolicy... jobExecutionPolicies) {
        final List<ResponseMessage> errors = new ArrayList<>();
        if (jobExecutionPolicies != null) {
            final Set<JobExecutionPolicy> executionPolicySet = new HashSet<>(Arrays.asList(jobExecutionPolicies));
            if (filterErrorPolicies(executionPolicySet).size() > 1) {
                errors.add(responseMessage()
                        .error(CmBulkImportServiceErrors.CONTROVERSIAL_EXECUTION_FLOWS).build());
            }
            if (filterThreadPolicies(executionPolicySet).size() > 1) {
                errors.add(responseMessage()
                        .error(CmBulkImportServiceErrors.CONTROVERSIAL_EXECUTION_FLOWS).build());
            }
            if (executionPolicySet.contains(PARALLEL) && executionPolicySet.contains(STOP_ON_ERROR)) {
                errors.add(responseMessage()
                        .error(CmBulkImportServiceErrors.CONTROVERSIAL_EXECUTION_FLOWS).build());
            }
            if (filterUnsyncNodeExecPolicies(executionPolicySet).size() > 1) {
                errors.add(responseMessage()
                        .error(CmBulkImportServiceErrors.CONTROVERSIAL_EXECUTION_FLOWS).build());
            }
        }
        return errors;
    }

    public static List<ResponseMessage> validateNoControversialValidationPolicies(final JobValidationPolicy... jobValidationPolicies) {
        final List<ResponseMessage> errors = new ArrayList<>();
        if (jobValidationPolicies != null) {
            final Set<JobValidationPolicy> validationPolicySet = new HashSet<>(Arrays.asList(jobValidationPolicies));
            if (filterInstanceValidationPolicies(validationPolicySet).size() > 1) {
                errors.add(responseMessage()
                        .error(CmBulkImportServiceErrors.CONTROVERSIAL_VALIDATION_FLOWS).build());
            }
            if (filterNodeBasedValidationPolicies(validationPolicySet).size() > 1) {
                errors.add(responseMessage()
                        .error(CmBulkImportServiceErrors.CONTROVERSIAL_VALIDATION_FLOWS).build());
            }
            if (filterAuditValidationPolicies(validationPolicySet).size() > 1) {
                errors.add(responseMessage()
                        .error(CmBulkImportServiceErrors.CONTROVERSIAL_VALIDATION_FLOWS).build());
            }
        }
        return errors;
    }

    private static Collection<JobExecutionPolicy> filterErrorPolicies(final Collection<JobExecutionPolicy> jobExecutionPolicyList) {
        return Collections2.filter(jobExecutionPolicyList, EXEC_ERROR_POLICIES::contains);
    }

    private static Collection<JobExecutionPolicy> filterThreadPolicies(final Collection<JobExecutionPolicy> jobExecutionPolicyList) {
        return Collections2.filter(jobExecutionPolicyList, EXEC_THREAD_POLICIES::contains);
    }

    private static Collection<JobExecutionPolicy> filterUnsyncNodeExecPolicies(final Collection<JobExecutionPolicy> jobExecutionPolicyList) {
        return Collections2.filter(jobExecutionPolicyList, EXEC_UNSYNC_NODES_POLICIES::contains);
    }

    private static Collection<JobValidationPolicy> filterInstanceValidationPolicies(final Collection<JobValidationPolicy> jobValidationPolicyList) {
        return Collections2.filter(jobValidationPolicyList, VALIDATION_INSTANCE_POLICIES::contains);
    }

    private static Collection<JobValidationPolicy> filterNodeBasedValidationPolicies(final Collection<JobValidationPolicy> jobValidationPolicyList) {
        return Collections2.filter(jobValidationPolicyList, VALIDATION_NODE_BASED_POLICIES::contains);
    }

    private static Collection<JobValidationPolicy> filterAuditValidationPolicies(final Collection<JobValidationPolicy> jobValidationPolicyList) {
        return Collections2.filter(jobValidationPolicyList, VALIDATION_AUDIT_POLICIES::contains);
    }

}