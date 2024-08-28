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

import java.util.Comparator;

import com.ericsson.oss.services.cm.export.api.ExportFilterWithDescription;

/**
 * ExportFilter comparator for sorting version column/field.
 */
class ExportFilterVersionComparator implements Comparator<ExportFilterWithDescription> {
    @Override
    public int compare(final ExportFilterWithDescription exportFilter1, final ExportFilterWithDescription exportFilter2) {
        final String[] version1SplitByFullStop = exportFilter1.getVersion().split("\\.");
        final String[] version2SplitByFullStop = exportFilter2.getVersion().split("\\.");
        final int version1Length = version1SplitByFullStop.length;
        final int version2Length = version2SplitByFullStop.length;
        if (version1Length != version2Length) {
            throw new IllegalArgumentException("Incompatible number of fields in versions being compared (version of length " + version1Length
                    + " and a version of length " + version2Length + ")");
        }
        try {
            for (int i = 0; i < version1Length; i++) {
                final Integer version1Int = Integer.parseInt(version1SplitByFullStop[i]);
                final Integer version2Int = Integer.parseInt(version2SplitByFullStop[i]);
                if (version1Int < version2Int) {
                    return 1;
                }
                if (version1Int > version2Int) {
                    return -1;
                }
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("One or both of the following versions is in an incorrect format: " + exportFilter1.getVersion()
                    + ", " + exportFilter2.getVersion());
        }
        return 0;
    }
}
