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

import com.ericsson.oss.services.cm.export.api.ExportFilterWithDescription;

/**
 * A ChainedComparator implementation specifically for sorting ExportFilter.
 * ExportFilter has three fields, namespace, name and version. They need to be compared in that order.
 * The version field is sorted in descending order
 */
class ExportFilterComparator extends ChainedComparator<ExportFilterWithDescription> {
    /**
     * A default constructor providing ascending order comparison on namespace and name and a descending order comparison on version.
     */
    ExportFilterComparator() {
        super(new ExportFilterNamespaceComparator(), new ExportFilterNameComparator(), new ExportFilterVersionComparator());
    }
}
