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

/**
 * Constants used for CmBulkImport service.
 */
public final class CmBulkImportConstants {
    /**
     * Constant to represent key to be used to retrieve current user from context API.
     */
    public static final String CONTEXT_PROPERTY_TOR_USER_ID = "X-Tor-UserID";

    public static final String CONTEXT_USER_ID = "context-user";

    private CmBulkImportConstants() {
    }
}
