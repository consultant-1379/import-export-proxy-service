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

import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.FILE_NOT_FOUND_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.FILE_SIZE_EXCEED_LIMIT_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.IMPORT_JOB_CANNOT_BE_CANCELLED_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.IMPORT_JOB_CANNOT_BE_DELETED_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.IMPORT_JOB_OPERATIONS_ALREADY_DEFINED_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.INVALID_BREAKPOINT_TYPE_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.INVALID_FDN_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.INVALID_INVOCATION_ID_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.INVALID_LIMIT_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.INVALID_OFFSET_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.INVALID_OPERATION_TYPE_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.INVALID_SCHEDULE_TIME_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.JOB_STATUS_MISMATCH_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.JOB_STATUS_MISMATCH_FOR_CREATE_DPS_GROUP_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.JOB_STATUS_MISMATCH_FOR_CREATE_OPERATIONS_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.JOB_STATUS_MISMATCH_FOR_GET_NODES_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.JOB_STATUS_MISMATCH_FOR_UNSYNC_NODES_COUNT_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.MISMATCHING_IMPORT_FILE_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.NEITHER_IMPORT_FILE_NOR_OPERATIONS_DEFINED_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.NO_ATTRIBUTES_SUPPLIED_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.NO_OPERATIONS_SUPPLIED_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.NO_OPERATIONS_TO_EXECUTE_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.NO_OPERATIONS_TO_RE_VALIDATE_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.OPERATIONS_COUNT_EXCEEDED_LIMIT_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.UNEXPECTED_ERROR_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.UNKNOWN_IMPORT_JOB_ID_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.UNSET_NOT_SUPPORTED_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.UNSUPPORTED_ATTRIBUTE_VALUE_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.UNSUPPORTED_FILE_FORMAT_ERROR_CODE;
import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.ZIP_FILE_ERROR_CODE;

/**
 * Errors class tor the CmBulk import service.
 */
@SuppressWarnings({"checkstyle:MultipleStringLiterals"})
public enum CmBulkImportServiceErrors {
    UNKNOWN_IMPORT_JOB("unknown-import-job",
            UNKNOWN_IMPORT_JOB_ID_ERROR_CODE,
            "Unable to retrieve job details for job id: %s. Please check the job id."),
    IMPORT_FILE_ALREADY_DEFINED("import-file-already-defined",
            MISMATCHING_IMPORT_FILE_ERROR_CODE,
            "Import file already defined for the import job %s"),
    NEITHER_IMPORT_FILE_NOR_OPERATIONS_DEFINED("neither-import-file-nor-operations-defined",
            NEITHER_IMPORT_FILE_NOR_OPERATIONS_DEFINED_ERROR_CODE,
            "Neither Import file nor operations defined for the import job"),
    UNSUPPORTED_INVOCATION_FLOW("unsupported-invocation-flow",
            UNKNOWN_IMPORT_JOB_ID_ERROR_CODE,
            "The supplied invocation flow %s is not supported for import. Please specify a supported invocation flow."),
    JOB_STATUS_MISMATCH("job-status-mismatch",
            JOB_STATUS_MISMATCH_ERROR_CODE,
            "The job cannot be invoked because it does not have the correct status."),
    INVALID_USERID("invalid-user-id",
            UNEXPECTED_ERROR_ERROR_CODE,
            "The job can not be created because the user id is invalid -%s"),
    ERROR_READING_ZIP("error-reading-zip",
            ZIP_FILE_ERROR_CODE,
            "Error while reading zip file."),
    IMPORT_FILE_NOT_FOUND("import-file-not-found",
            FILE_NOT_FOUND_ERROR_CODE,
            "Import file not found."),
    INVALID_FILE_FORMAT("invalid-file-format",
            UNSUPPORTED_FILE_FORMAT_ERROR_CODE,
            "Supported file extensions are xml, csv, txt, zip."),
    FILE_SIZE_EXCEEDED("exceed-file-size",
            FILE_SIZE_EXCEED_LIMIT_ERROR_CODE,
            "The import file exceeds the allowed size limit (%s MB)."),
    NO_OPERATIONS_TO_EXECUTE("no-operations-to-execute",
            NO_OPERATIONS_TO_EXECUTE_ERROR_CODE,
            "There are no operations with VALID or EXECUTION_ERROR state to execute."),
    NO_OPERATIONS_TO_RE_VALIDATE("no-operations-to-re-validate",
            NO_OPERATIONS_TO_RE_VALIDATE_ERROR_CODE,
            "All operations are in EXECUTED state and cannot be re-validated."),
    INVALID_OFFSET_PARAMETER("invalid-offset-parameter", INVALID_OFFSET_ERROR_CODE, "Invalid offset parameter - %s"),
    INVALID_LIMIT_PARAMETER("invalid-limit-parameter", INVALID_LIMIT_ERROR_CODE, "Invalid limit parameter - %s"),
    CANCEL_JOB_STATUS_MISMATCH("job-status-mismatch",
            IMPORT_JOB_CANNOT_BE_CANCELLED_ERROR_CODE,
            "The job cannot be cancelled because it does not have the correct status."),
    DELETE_JOB_STATUS_MISMATCH("job-status-mismatch",
            IMPORT_JOB_CANNOT_BE_DELETED_ERROR_CODE,
            "The job cannot be deleted because it does not have the correct status. Current job status : '%s'"),
    CONTROVERSIAL_EXECUTION_FLOWS(
            "controversial-execution-flows",
            7050,
            "Controversial execution flows encountered."),
    CONTROVERSIAL_VALIDATION_FLOWS(
            "controversial-validation-flows",
            7051,
            "Controversial validation flows encountered."),
    INVALID_SCHEDULE_TIME("invalid-job-schedule-start-time",
            INVALID_SCHEDULE_TIME_ERROR_CODE,
            "Scheduled start time invalid. Scheduled time needs to be set at least %s minutes into the future"),
    UNSET_NOT_SUPPORTED("unschedule-not-supported",
            UNSET_NOT_SUPPORTED_ERROR_CODE,
            "Unsetting schedule time for the job id %s which was not scheduled, is not supported"),
    INVALID_INVOCATION_ID("unknown-invocation-id",
            INVALID_INVOCATION_ID_ERROR_CODE,
            "Invocation id %s is invalid. Please specify invocation id same as job id"),
    CANNOT_DELETE_SCHEDULED_JOB("cannot-delete-scheduled-job",
            IMPORT_JOB_CANNOT_BE_DELETED_ERROR_CODE,
            "Scheduled job cannot be deleted. Remove scheduled date/time from job and then delete"),
    GET_ALL_NODES_JOB_STATUS_MISMATCH("job-status-mismatch",
            JOB_STATUS_MISMATCH_FOR_GET_NODES_ERROR_CODE,
            "Cannot fetch nodes for this job, because job does not have correct status"),
    CREATE_GROUP_JOB_STATUS_MISMATCH("job-status-mismatch",
            JOB_STATUS_MISMATCH_FOR_CREATE_DPS_GROUP_ERROR_CODE,
            "Cannot create dps group for this job, because job does not have correct status"),
    GET_UNSYNC_NODES_COUNT_JOB_STATUS_MISMATCH("job-status-mismatch",
            JOB_STATUS_MISMATCH_FOR_UNSYNC_NODES_COUNT_ERROR_CODE,
            "Cannot fetch the count of unsynchronized nodes of this job, because job does not have correct status"),
    ERROR_CREATING_IMPORT_OPERATIONS("cannot-create-import-operations",
            UNSUPPORTED_ATTRIBUTE_VALUE_ERROR_CODE,
            "Unable to create import operations with reason : %s"),
    UNEXPECTED_ERROR("unexpected-error-occurred",
            UNEXPECTED_ERROR_ERROR_CODE, "Unexpected error occurred. Please try again."),
    NO_OPERATIONS_PROVIDED("no-operations-supplied",
            NO_OPERATIONS_SUPPLIED_ERROR_CODE,
            "No operations supplied for job id: %s"),
    INVALID_OPERATION_TYPE("invalid-operation-type",
            INVALID_OPERATION_TYPE_ERROR_CODE,
            "Invalid operation type: %s for operation: %s."),
    INVALID_JOB_STATUS("job-status-mismatch",
            JOB_STATUS_MISMATCH_FOR_CREATE_OPERATIONS_ERROR_CODE,
            "Operations could not be added to job because the job does not have correct status."),
    INVALID_FDN("invalid-fdn",
            INVALID_FDN_ERROR_CODE,
            "Invalid fdn: %s for operation: %s."),
    INVALID_BREAKPOINT_TYPE("invalid-breakpoint-type",
            INVALID_BREAKPOINT_TYPE_ERROR_CODE,
            "Invalid Breakpoint type: %s for operation: %s."),
    NO_ATTRIBUTES_PROVIDED("no-attributes-supplied",
            NO_ATTRIBUTES_SUPPLIED_ERROR_CODE,
            "No attributes supplied for operation: %s."),
    INVALID_OPERATIONS_LIMIT("operations-count-exceeded-limit",
            OPERATIONS_COUNT_EXCEEDED_LIMIT_ERROR_CODE,
            "Operations could not be added to job as operation count: %s exceeds the limit: %s."),
    IMPORT_JOB_OPERATIONS_ALREADY_DEFINED("operations-already-defined",
            IMPORT_JOB_OPERATIONS_ALREADY_DEFINED_ERROR_CODE,
            "Operations already defined for job id : %s.");
    private final String code;
    private final int internalCode;
    private final String messageTemplate;

    CmBulkImportServiceErrors(final String code, final int internalCode, final String messageTemplate) {
        this.code = code;
        this.internalCode = internalCode;
        this.messageTemplate = messageTemplate;
    }

    public String getCode() {
        return code;
    }

    public int getInternalCode() {
        return internalCode;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String message(final Object... args) {
        return String.format(messageTemplate, args);
    }
}
