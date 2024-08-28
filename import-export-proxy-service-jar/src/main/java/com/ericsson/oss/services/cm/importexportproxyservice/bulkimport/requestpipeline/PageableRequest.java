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

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;

/**
 * Pageable Request abstract class.
 *
 * @param <T> t response type
 */
public abstract class PageableRequest<T extends Response<?>> implements Request<T>, Pageable {

    public abstract Criteria<?> getCriteria();

    @Override
    public int getOffset() {
        return getCriteria() != null ? getCriteria().getOffset() : Pageable.DEFAULT_PAGE_OFFSET;
    }

    @Override
    public int getLimit() {
        return getCriteria() != null ? getCriteria().getLimit() : Pageable.DEFAULT_PAGE_LIMIT;
    }

    @Override
    public boolean isTotalCountRequired() {
        return getCriteria() != null ? getCriteria().isTotalCountRequired() : Pageable.DEFAULT_TOTAL_COUNT_REQUIRED;
    }
}
