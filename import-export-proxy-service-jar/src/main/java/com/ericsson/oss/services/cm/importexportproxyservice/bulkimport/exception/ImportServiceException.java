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

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.ResponseMessage;

/**
 * Base ImportService exception class.
 */
public class ImportServiceException extends RuntimeException {
    private static final long serialVersionUID = 6026646436802631433L;

    private final List<ResponseMessage> messages;

    public ImportServiceException(final List<ResponseMessage> messages) {
        this.messages = messages;
    }

    public List<ResponseMessage> getMessages() {
        return messages != null ? Collections.unmodifiableList(messages) : null;
    }
}
