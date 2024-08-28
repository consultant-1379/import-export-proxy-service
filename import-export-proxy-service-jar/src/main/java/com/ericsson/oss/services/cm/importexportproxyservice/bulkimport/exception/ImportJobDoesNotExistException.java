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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception;

import static com.ericsson.oss.services.cm.bulkimport.constants.ImportErrorCodeConstants.IMPORT_JOB_DOES_NOT_EXIST_ERROR_CODE;

import com.ericsson.oss.services.cm.bulkimport.exception.BulkImportException;

/**
 * Signals that an attempt to cancel an import job failed as there is no import job existing in the system with the id provided by the user.
 */
public class ImportJobDoesNotExistException extends BulkImportException {

    private static final long serialVersionUID = -5665338804421911152L;
    private static final String IMPORT_JOB_DOES_NOT_EXIST_ERROR_MSG =
            "An import job with id {0} does not exist.";

    /**
     * The constructor.
     *
     * @param jobId Import job id
     *
     */
    public ImportJobDoesNotExistException(final long jobId) {
        super(IMPORT_JOB_DOES_NOT_EXIST_ERROR_CODE, formatMessage(IMPORT_JOB_DOES_NOT_EXIST_ERROR_MSG, String.valueOf(jobId)));
    }
}
