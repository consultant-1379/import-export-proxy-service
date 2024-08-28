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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.filter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for ExportFilter comparators.
 *
 * @param <T>
 *            The type to be sorted.
 */
class ChainedComparator<T> implements Comparator<T> {
    private final List<Comparator<T>> listComparators;

    @SafeVarargs
    ChainedComparator(final Comparator<T>... comparators) {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(final T item1, final T item2) {
        for (final Comparator<T> comparator : listComparators) {
            final int result = comparator.compare(item1, item2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
