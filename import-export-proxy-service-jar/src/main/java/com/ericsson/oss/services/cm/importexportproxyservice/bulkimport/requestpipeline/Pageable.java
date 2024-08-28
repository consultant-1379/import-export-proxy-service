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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline;

/**
 * Pageable interface.
 */
public interface Pageable {

    int DEFAULT_PAGE_OFFSET = 0;
    int DEFAULT_PAGE_LIMIT = Integer.MAX_VALUE;
    boolean DEFAULT_TOTAL_COUNT_REQUIRED = false;

    int getOffset();

    int getLimit();

    boolean isTotalCountRequired();
}
