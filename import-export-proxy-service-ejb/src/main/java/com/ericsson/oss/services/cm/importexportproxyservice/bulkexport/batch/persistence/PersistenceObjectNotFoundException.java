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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.persistence;

/**
 * Exception thrown when PersistenceObject cannot be found in DPS for a given PoID.
 */
public class PersistenceObjectNotFoundException extends RuntimeException {

    public PersistenceObjectNotFoundException(final String message) {
        super(message);
    }
}
