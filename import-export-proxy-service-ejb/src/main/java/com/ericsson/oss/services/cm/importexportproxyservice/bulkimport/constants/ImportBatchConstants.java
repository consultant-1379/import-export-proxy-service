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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.constants;

/**
 * Constants used in pre-import copy.
 */
public final class ImportBatchConstants {

    /**
     * Constant to represent 'FileFormat' text.
     */
    public static final String FILE_FORMAT = "FileFormat";
    /**
     * Constant to represent 'FilePath' text.
     */
    public static final String FILE_PATH = "FilePath";
    /**
     * Constant to represent continue on error flag as key in job properties.
     */
    public static final String STOP_ON_ERROR_AS_VALUE = "FALSE";
    /**
     * ID of current import job.
     */
    public static final String JOB_ID = "jobId";
    /**
     * Source configuration nodes will be copied from.
     */
    public static final String SOURCE_CONFIGURATION = "Live";
    /**
     * Target configuration nodes will be copied to.
     */
    public static final String TARGET_CONFIGURATION = "targetConfiguration";
    /**
     * Constant to represent the transient property where edff block list is meant to be saved.
     */
    public static final String EDFF_BLOCK_LIST = "EdffBlockList";
    /**
     * Constant to represent the transient property where edff node list is meant to be saved.
     */
    public static final String EDFF_NODE_LIST = "EdffNodeList";
    /**
     * Constant to represent error handling level.
     */
    public static final String CONTINUE_ON_ERROR_LEVEL_AS_KEY = "ContinueOnErrorLevel";
    /**
     * Constant to sop on error handling.
     */
    public static final String STOP_ON_ERROR = "STOP";
    /**
     * Constant to represent Operation level Error handling.
     */
    public static final String CONTINUE_ON_ERROR_LEVEL_NODE = "NODE";
    /**
     * Constant to represent Operation level Error handling.
     */
    public static final String CONTINUE_ON_ERROR_LEVEL_OPERATION = "OPERATION";
    /**
     * Constant to represent skip instance validation.
     */
    public static final String VALIDATE_INSTANCES_AS_KEY = "VALIDATE_INSTANCES";
    /**
     * Constant to represent if the mode of execution is parallel.
     */
    public static final String IS_PARALLEL = "isParallel";
    /**
     * Constant to represent if audit is part of import job.
     */
    public static final String IS_AUDIT_SELECTED = "isAuditSelected";

    /**
     * Constant to represent unique id for export process of an import job.
     */
    public static final String IMPORT_JOB_EXPORT_ID = "importJobExportId";

    private ImportBatchConstants() {}
}
