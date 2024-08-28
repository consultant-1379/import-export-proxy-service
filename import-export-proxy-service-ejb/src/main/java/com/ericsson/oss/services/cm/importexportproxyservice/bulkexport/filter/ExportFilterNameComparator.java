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

import java.text.Collator;
import java.util.Comparator;

import com.ericsson.oss.services.cm.export.api.ExportFilterWithDescription;

/**
 * ExportFilter comparator for sorting on name column/field.
 */
class ExportFilterNameComparator implements Comparator<ExportFilterWithDescription> {
    @Override
    public int compare(final ExportFilterWithDescription exportFilter1, final ExportFilterWithDescription exportFilter2) {
        final Collator collator = Collator.getInstance();
        return collator.compare(exportFilter1.getName(), exportFilter2.getName());
    }
}
