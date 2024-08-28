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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.userfilter;

import static com.ericsson.oss.services.cm.export.api.ExportServiceError.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.ericsson.oss.services.cm.export.api.ExportServiceError;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.validation.ValidationException;

/**
 * Stateless EJB to handle user filter file data.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class UserFilterFileHandlerBean {

    public String readUserFilterFile(final String filePath) {
        if ((filePath == null) || filePath.trim().isEmpty()) {
            throw createValidationException(USER_FILTER_FILE_NOT_SPECIFIED);
        }

        final Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw createValidationException(USER_FILTER_FILE_SPECIFIED_NOT_FOUND, filePath);
        }

        final byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(filePath));
        } catch (final IOException e) {
            throw createValidationException(USER_FILTER_FILE_NOT_READABLE, filePath);
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private ValidationException createValidationException(final ExportServiceError message, final Object... additionalInfo) {
        return new ValidationException(message.message(additionalInfo), message.solution(), message.code());
    }

}
