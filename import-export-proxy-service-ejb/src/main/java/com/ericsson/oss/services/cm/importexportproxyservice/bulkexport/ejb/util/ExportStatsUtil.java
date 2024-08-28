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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.ejb.util;

import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.cm.export.api.ExportNodeResult.ExportNodeResultStatus;
import com.ericsson.oss.services.cm.export.transformer.api.ProcessResult;
import com.ericsson.oss.services.cm.export.transformer.api.TransformationStatus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;

/**
 * Utilities class for export to determine the Export statistics needed for the given export.
 */
public final class ExportStatsUtil {

    public static final String NOT_TRANSFORMED_NO_MOS_EXPORT_STATUS_MESSAGE = "no MOs to export.";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportStatsUtil.class);

    private ExportStatsUtil() {}

    /**
     * Provides common utility to NodeCounter and ExportStatusBean classes.
     *
     * @param failedTransformedCount
     *            number of failed process results detected for a node
     * @param notExportedCount
     *            number of not exported process results detected for a node
     * @param processResults
     *            List of ProcessResults or persistenceObjects for a node
     * @return true if a failed node is detected.
     */
    public static boolean isFailedExportNodeForGivenProcessResults(final int failedTransformedCount, final int notExportedCount,
            final List<ProcessResult> processResults) {
        if ((failedTransformedCount > 0) || (notExportedCount > 0)) {
            LOGGER.info("Number of failedTransformedCount: [{}] and notExportedCount: [{}]", failedTransformedCount, notExportedCount);
        }
        return (failedTransformedCount > 0) || ((notExportedCount > 0) && (notExportedCount == processResults.size()));
    }

    public static boolean isFailedExportNodeForGivenPersistenceObjects(final int failedTransformedCount, final int notExportedCount,
            final List<PersistenceObject> persistenceObjects) {
        return (failedTransformedCount > 0) || ((notExportedCount > 0) && (notExportedCount == persistenceObjects.size()));
    }

    public static boolean processResultContainsFailedStatusMessage(final ProcessResult processResult) {
        if ((processResult != null) && (processResult.getState() == TransformationStatus.NOT_TRANSFORMED)) {
            LOGGER.info("StatusMessage for NOT_TRANSFORMED nodes: [{}]", processResult.getStatusMessage());
        }
        return (processResult != null) && (processResult.getState() == TransformationStatus.NOT_TRANSFORMED)
                && processResult.getStatusMessage() != null && !processResult.getStatusMessage().isEmpty()
                && !processResult.getStatusMessage().contains(NOT_TRANSFORMED_NO_MOS_EXPORT_STATUS_MESSAGE);
    }

    public static boolean processResultContainsFileNameAndSuccessfulTransformation(final ProcessResult processResult) {
        return (processResult != null) && (processResult.getState() == TransformationStatus.TRANSFORMED);
    }

    public static int updateNumberOfMosExported(final List<ProcessResult> processResults) {
        int numberOfMosExported = 0;
        for (final ProcessResult processResult : processResults) {
            numberOfMosExported += processResult.getNumberOfMos();
        }
        return numberOfMosExported;
    }

    public static Map<String, List<ProcessResult>> groupProcessResultsIntoRootMos(final List<ProcessResult> processResults) {
        final ArrayListMultimap<String, ProcessResult> multimap = ArrayListMultimap.create();
        for (final ProcessResult processResult : processResults) {
            multimap.put(processResult.getNodeRootFdn(), processResult);
        }
        return Multimaps.asMap(multimap);
    }

    public static boolean persistenceObjectContainsFailedStatusMessage(final PersistenceObject nodeExportResult) {
        return (nodeExportResult != null)
                && ((String) nodeExportResult.getAttribute(ATTR_EXPORT_STATUS) == ExportNodeResultStatus.NOT_EXPORTED.toString())
                && !((String) nodeExportResult.getAttribute(ATTR_STATUS_MESSAGE)).isEmpty()
                && !((String) nodeExportResult.getAttribute(ATTR_STATUS_MESSAGE)).contains(NOT_TRANSFORMED_NO_MOS_EXPORT_STATUS_MESSAGE);
    }

    public static boolean persistenceObjectContainsFileNameAndSuccessfulTransformation(final PersistenceObject nodeExportResult) {
        return (nodeExportResult != null)
                && ((String) nodeExportResult.getAttribute(ATTR_EXPORT_STATUS)).equals(ExportNodeResultStatus.EXPORTED.toString())
                && ((String) nodeExportResult.getAttribute(ATTR_FILE_NAME) != null)
                && !((String) nodeExportResult.getAttribute(ATTR_FILE_NAME)).isEmpty();
    }

    public static Map<String, List<PersistenceObject>> groupPersistenceObjectsIntoRootMos(final List<PersistenceObject> persistenceObjects) {
        final ArrayListMultimap<String, PersistenceObject> multimap = ArrayListMultimap.create();
        for (final PersistenceObject persistenceObject : persistenceObjects) {
            multimap.put((String) persistenceObject.getAttribute(ATTR_NODE_ROOT_FDN), persistenceObject);
        }
        return Multimaps.asMap(multimap);
    }

}
