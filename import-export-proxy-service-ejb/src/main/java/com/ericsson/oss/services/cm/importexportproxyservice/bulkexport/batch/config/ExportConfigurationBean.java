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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.config;

import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;

/**
 * Export service configuration class that uses ServiceFramework configuration parameters.
 */
@ApplicationScoped
public class ExportConfigurationBean {

    /**
     * Default Unfiltered  Node Batch Size.
     */
    public static final Integer DEFAULT_BATCH_SIZE_UNFILTERED_EXPORT = 300_000;

    /**
     * Default value of ENIQ export Batch size.
     */
    static final int DEFAULT_BATCH_SIZE_ENIQ_EXPORT = 40000;

    /**
     * Default export jobs threshold.
     */
    static final int DEFAULT_MAX_JOBS = 20;

    /**
     * Default big export jobs threshold.
     */
    static final int DEFAULT_MAX_BIG_JOBS = 3;

    /**
     * Default small job node limit.
     */
    static final int DEFAULT_SMALL_JOB_NODE_LIMIT = 100;

    /**
     * Default export jobs threshold feature check.
     */
    static final boolean DEFAULT_MAX_JOBS_CHECK = true;
    /**
     * Default message receive timeout.
     */
    static final int DEFAULT_MSG_RECEIVE_TIMEOUT = 120;

    /**
     * Default value of export non synchronized nodes.
     */
    static final boolean DEFAULT_EXPORT_NON_SYNCHRONIZED_NODES = true;

    /**
     * Default value of export CPP Inventory Mos.
     */
    static final boolean DEFAULT_EXPORT_CPP_INVENTORY_MOS = false;

    /**
     * Default value of export CPP Inventory Mos.
     */
    static final boolean DEFAULT_EXPORT_ENUM_TRANSLATE = true;

    /**
     * Default value of ignore non persistent attributes for non synchronized nodes.
     */
    static final boolean DEFAULT_EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES = false;

    /**
     * Default value of export include ENM data 3GPP.
     */
    static final boolean DEFAULT_EXPORT_INCLUDE_ENM_DATA_3GPP = false;

    /**
     * Default value of export MOs in alphabetical order of FDN.
     */
    static final boolean DEFAULT_EXPORT_ORDER_MOS_BY_FDN = false;

    /**
     * Default minimum MO count for medium size node category.
     */
    static final int DEFAULT_MEDIUM_NODE_MINIMUM_MO_COUNT = 150_000;

    /**
     * Default minimum MO count for large size node category.
     */
    static final int DEFAULT_LARGE_NODE_MINIMUM_MO_COUNT = 300_000;

    /**
     * Default value of Filtered Extra LargeNode export Batch size.
     */
    static final int DEFAULT_BATCH_SIZE_FILTERED_NODE_EXPORT = 500;

    /**
     * Default value of enable ExtraLarge node Batching.
     */
    static final boolean DEFAULT_BATCH_EXPORT = true;

    /*
     * Default value of ExtraLarge node size.
     */
    static final int DEFAULT_NODESIZE_BATCHINGELIGIBLE = 500_000;

    /**
     * Valid enum String values.
     */
    private static final Set<String> VALID_BOOLEAN_STRINGS = new HashSet<>(Arrays.asList("true", "false"));
    private static final String CONFIG_PARAM_CHANGED_MESSAGE = "{} config param changed, old value = [{}], new value = [{}] ";
    private static final String CONFIG_PARAM_INJECTED_MESSAGE = "{} config param injected with value [{}]";
    private static final String CONFIG_PARAM_NOT_INJECTED_MESSAGE = "{} config param not injected, returning default value [{}]";

    /**
     * Max jobs configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = MAX_JOBS_CONFIG_PARAM)
    private String maxJobs;

    /**
     * Max big jobs configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = MAX_BIG_JOBS_CONFIG_PARAM)
    private String maxBigJobs;

    /**
     * Max nodes in a small job configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = SMALL_JOB_NODE_LIMIT)
    private String smallJobNodeLimit;

    /**
     * Max jobs check configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = MAX_JOBS_CHECK_CONFIG_PARAM)
    private String maxJobsCheck;

    /**
     * Max jobs configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = MSG_RECEIVE_TIMEOUT_CONFIG_PARAM)
    private String messageReceiveTimeout;

    /**
     * Export unsynchronized nodes configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = EXPORT_NON_SYNCHRONIZED_NODES_CONFIG_PARAM)
    private String exportNonSynchronizedNodes;

    /**
     * Export CPP Inventory Mos configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = EXPORT_CPP_INVENTORY_MOS_CONFIG_PARAM)
    private String exportCppInventoryMos;

    /**
     * Export default enum translate behavior injection.
     */
    @Inject
    @Configured(propertyName = EXPORT_ENUM_TRANSLATE_CONFIG_PARAM)
    private String exportEnumTranslate;

    /**
     * Export ignore non-persistent attributes for non-synchronized nodes configuration parameter injection.
     */
    @Inject
    @Configured(propertyName = EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES_CONFIG_PARAM)
    private Boolean ignoreNpaforNonSynchedNodes;

    @Inject
    @Configured(propertyName = EXPORT_INCLUDE_ENM_DATA_3GPP_CONFIG_PARAM)
    private Boolean includeEnmData3gpp;

    @Inject
    @Configured(propertyName = EXPORT_ORDER_MOS_BY_FDN_PARAM)
    private Boolean orderMosByFdn;

    @Inject
    @Configured(propertyName = EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM)
    private int[] nodeMinimumMoCounts;

    @Inject
    @Configured(propertyName = BATCH_SIZE_UNFILTERED_EXPORT)
    private Integer unfilteredExportBatchSize;

    @Inject
    @Configured(propertyName = BATCH_SIZE_FILTERED_NODE_EXPORT)
    private Integer filteredExportBatchSize;

    @Inject
    @Configured(propertyName = BATCH_SIZE_ENIQ_EXPORT)
    private Integer eniqExportBatchSize;

    @Inject
    @Configured(propertyName = BATCH_EXPORT)
    private Boolean exportBatch;

    @Inject
    @Configured(propertyName = NODESIZE_BATCHINGELIGIBLE)
    private Integer nodeSizeEligibleBatch;

    @Inject
    private Logger logger;

    public int getMaxJobs() {
        try {
            if (isStringNullOrEmpty(maxJobs)) {
                logger.info("maxJobs config param not injected, returning default value [{}]", DEFAULT_MAX_JOBS);
                return DEFAULT_MAX_JOBS;
            }
            final int configValue = Integer.parseInt(maxJobs);
            if (configValue <= 0) {
                logger.info("maxJobs config param not injected, returning default value [{}]", DEFAULT_MAX_JOBS);
                return DEFAULT_MAX_JOBS;
            }
            logger.debug("maxJobs config param injected with value [{}]", maxJobs);
            return configValue;
        } catch (final Exception exception) {
            logger.warn("maxJobs config param not injected, returning default value [{}]", DEFAULT_MAX_JOBS);
            return DEFAULT_MAX_JOBS;
        }
    }

    public int getMaxBigJobs() {
        try {
            if (isStringNullOrEmpty(maxBigJobs)) {
                logger.info("maxBigJobs config param not injected, returning default value [{}]", DEFAULT_MAX_BIG_JOBS);
                return DEFAULT_MAX_BIG_JOBS;
            }
            final int configValue = Integer.parseInt(maxBigJobs);
            final int allowedJobs = getMaxJobs();
            if (configValue <= 0 || configValue > allowedJobs) {
                logger.info("maxBigJobs config param [{}] is incorrect. It should be a positive integer and <= [{}]. Returning default value [{}]",
                        maxBigJobs, allowedJobs, DEFAULT_MAX_BIG_JOBS);
                return DEFAULT_MAX_BIG_JOBS;
            }
            logger.debug("maxBigJobs config param injected with value [{}]", maxBigJobs);
            return configValue;
        } catch (final Exception exception) {
            logger.warn("maxBigJobs config param not injected, returning default value [{}]", DEFAULT_MAX_BIG_JOBS);
            return DEFAULT_MAX_BIG_JOBS;
        }
    }

    public int getSmallJobNodeLimit() {
        try {
            if (isStringNullOrEmpty(smallJobNodeLimit)) {
                logger.info("smallJobNodeLimit config param not injected, returning default value [{}]", DEFAULT_SMALL_JOB_NODE_LIMIT);
                return DEFAULT_SMALL_JOB_NODE_LIMIT;
            }
            final int configValue = Integer.parseInt(smallJobNodeLimit);
            if (configValue <= 0) {
                logger.info("smallJobNodeLimit config param not injected, returning default value [{}]", DEFAULT_SMALL_JOB_NODE_LIMIT);
                return DEFAULT_SMALL_JOB_NODE_LIMIT;
            }
            logger.debug("smallJobNodeLimit config param injected with value [{}]", smallJobNodeLimit);
            return configValue;
        } catch (final Exception exception) {
            logger.warn("smallJobNodeLimit config param not injected, returning default value [{}]", DEFAULT_SMALL_JOB_NODE_LIMIT);
            return DEFAULT_SMALL_JOB_NODE_LIMIT;
        }
    }

    public boolean isMaxJobsCheck() {
        if (isStringNullOrEmpty(maxJobsCheck) || !isValidBooleanString(maxJobsCheck)) {
            logger.info("maxJobsCheck config param not injected, returning default value [{}]", DEFAULT_MAX_JOBS_CHECK);
            return DEFAULT_MAX_JOBS_CHECK;
        }
        logger.debug("maxJobsCheck config param injected with value [{}]", maxJobsCheck);
        return Boolean.valueOf(maxJobsCheck);
    }

    public int getMessageReceiveTimeout() {
        try {
            if (isStringNullOrEmpty(messageReceiveTimeout)) {
                logger.info("messageReceiveTimeout config param not injected, returning default value [{}]", DEFAULT_MSG_RECEIVE_TIMEOUT);
                return DEFAULT_MSG_RECEIVE_TIMEOUT;
            }
            final int configValue = Integer.parseInt(messageReceiveTimeout);
            if (configValue <= 0) {
                logger.info("messageReceiveTimeout config param not injected, returning default value [{}]", DEFAULT_MSG_RECEIVE_TIMEOUT);
                return DEFAULT_MSG_RECEIVE_TIMEOUT;
            }
            logger.debug("messageReceiveTimeout config param injected with value [{}]", messageReceiveTimeout);
            return configValue;
        } catch (final Exception exception) {
            logger.warn("messageReceiveTimeout config param not injected, returning default value [{}]", DEFAULT_MSG_RECEIVE_TIMEOUT);
            return DEFAULT_MSG_RECEIVE_TIMEOUT;
        }
    }

    public boolean isExportNonSynchronizedNodes() {
        if (isStringNullOrEmpty(exportNonSynchronizedNodes) || !isValidBooleanString(exportNonSynchronizedNodes)) {
            logger.info("exportNonSynchronizedNodes config param not injected, returning default value [{}]", DEFAULT_EXPORT_NON_SYNCHRONIZED_NODES);
            return DEFAULT_EXPORT_NON_SYNCHRONIZED_NODES;
        }
        logger.debug("exportNonSynchronizedNodes config param injected with value [{}]", exportNonSynchronizedNodes);
        return Boolean.valueOf(exportNonSynchronizedNodes);
    }

    public boolean isExportCppInventoryMos() {
        if (isStringNullOrEmpty(exportCppInventoryMos) || !isValidBooleanString(exportCppInventoryMos)) {
            logger.info("exportCppInventoryMos config param not injected, returning default value [{}]", DEFAULT_EXPORT_CPP_INVENTORY_MOS);
            return DEFAULT_EXPORT_CPP_INVENTORY_MOS;
        }
        logger.debug("exportCppInventoryMos config param injected with value [{}]", exportCppInventoryMos);
        return Boolean.valueOf(exportCppInventoryMos);
    }

    public boolean isExportEnumTranslate() {
        if (isStringNullOrEmpty(exportEnumTranslate) || !isValidBooleanString(exportEnumTranslate)) {
            logger.info("exportEnumTranslate config param not injected, returning default value [{}]", DEFAULT_EXPORT_ENUM_TRANSLATE);
            return DEFAULT_EXPORT_ENUM_TRANSLATE;
        }
        logger.debug("exportEnumTranslate config param injected with value [{}]", exportEnumTranslate);
        return Boolean.valueOf(exportEnumTranslate);
    }

    public boolean isIgnoreNpaForNonSynchedNodes() {
        if (null == ignoreNpaforNonSynchedNodes) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES_CONFIG_PARAM,
                    DEFAULT_EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES);
            return DEFAULT_EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES;
        }
        logger.debug(CONFIG_PARAM_INJECTED_MESSAGE, EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES_CONFIG_PARAM, ignoreNpaforNonSynchedNodes);
        return ignoreNpaforNonSynchedNodes.booleanValue();
    }

    public boolean isEnmDataIncluded3gpp() {
        if (null == includeEnmData3gpp) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, EXPORT_INCLUDE_ENM_DATA_3GPP_CONFIG_PARAM, DEFAULT_EXPORT_INCLUDE_ENM_DATA_3GPP);
            return DEFAULT_EXPORT_INCLUDE_ENM_DATA_3GPP;
        }
        logger.debug(CONFIG_PARAM_INJECTED_MESSAGE, EXPORT_INCLUDE_ENM_DATA_3GPP_CONFIG_PARAM, includeEnmData3gpp);
        return includeEnmData3gpp.booleanValue();
    }

    public boolean isOrderMosByFdn() {
        if (null == orderMosByFdn) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, EXPORT_ORDER_MOS_BY_FDN_PARAM, DEFAULT_EXPORT_ORDER_MOS_BY_FDN);
            return DEFAULT_EXPORT_ORDER_MOS_BY_FDN;
        }
        logger.debug(CONFIG_PARAM_INJECTED_MESSAGE, EXPORT_ORDER_MOS_BY_FDN_PARAM, orderMosByFdn);
        return orderMosByFdn.booleanValue();
    }

    public int getMediumNodeMinimumMoCount() {
        if (null == nodeMinimumMoCounts) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, DEFAULT_MEDIUM_NODE_MINIMUM_MO_COUNT);
            return DEFAULT_MEDIUM_NODE_MINIMUM_MO_COUNT;
        }
        logger.debug("Returning {} param medium node minimum MO count value {}",
                EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, nodeMinimumMoCounts[0]);
        return nodeMinimumMoCounts[0];
    }

    public int getLargeNodeMinimumMoCount() {
        if (null == nodeMinimumMoCounts) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, DEFAULT_LARGE_NODE_MINIMUM_MO_COUNT);
            return DEFAULT_LARGE_NODE_MINIMUM_MO_COUNT;
        }
        logger.debug("Returning {} param large node minimum MO count value {}",
                EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, nodeMinimumMoCounts[1]);
        return nodeMinimumMoCounts[1];
    }

    public boolean getExportBatch() {
        if (null == exportBatch) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, BATCH_EXPORT, DEFAULT_BATCH_EXPORT);
            return DEFAULT_BATCH_EXPORT;
        }
        logger.debug("Returning {} param enable or disable of Batching Nodes {}", BATCH_EXPORT, exportBatch);
        return exportBatch.booleanValue();
    }

    public int getNodeSizeEligibleBatch() {
        if (null == nodeSizeEligibleBatch) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, NODESIZE_BATCHINGELIGIBLE, DEFAULT_NODESIZE_BATCHINGELIGIBLE);
            return DEFAULT_NODESIZE_BATCHINGELIGIBLE;
        }
        logger.debug("Returning {} param  Node size {}", NODESIZE_BATCHINGELIGIBLE, nodeSizeEligibleBatch);
        return nodeSizeEligibleBatch.intValue();
    }

    public int getEniqExportBatchSize(final int iteratorMoCount) {
        if (null == eniqExportBatchSize) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, BATCH_SIZE_ENIQ_EXPORT, DEFAULT_BATCH_SIZE_ENIQ_EXPORT);
            return DEFAULT_BATCH_SIZE_ENIQ_EXPORT;
        }

        if (eniqExportBatchSize > iteratorMoCount) {
            logger.info("{} param value is greater than iterator MO count: {}, returning default value: {}",
                    BATCH_SIZE_ENIQ_EXPORT, iteratorMoCount, DEFAULT_BATCH_SIZE_ENIQ_EXPORT);
            return DEFAULT_BATCH_SIZE_ENIQ_EXPORT;
        }
        logger.debug("Returning {} param filtered export batch size {}", BATCH_SIZE_ENIQ_EXPORT, eniqExportBatchSize);
        return eniqExportBatchSize.intValue();
    }

    public int getFilteredExportBatchSize(final int iteratorMoCount) {
        if (null == filteredExportBatchSize) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, BATCH_SIZE_FILTERED_NODE_EXPORT, DEFAULT_BATCH_SIZE_FILTERED_NODE_EXPORT);
            return DEFAULT_BATCH_SIZE_FILTERED_NODE_EXPORT;
        }

        if (filteredExportBatchSize > iteratorMoCount) {
            logger.info("{} param value is greater than iterator MO count: {}, returning default value: {}",
                    BATCH_SIZE_FILTERED_NODE_EXPORT, iteratorMoCount, DEFAULT_BATCH_SIZE_FILTERED_NODE_EXPORT);
            return DEFAULT_BATCH_SIZE_FILTERED_NODE_EXPORT;
        }
        logger.debug("Returning {} param filtered export batch size {}", BATCH_SIZE_FILTERED_NODE_EXPORT, filteredExportBatchSize);
        return filteredExportBatchSize.intValue();
    }

    public int getUnfilteredExportBatchSize(final int iteratorMoCount) {
        if (null == unfilteredExportBatchSize) {
            logger.info(CONFIG_PARAM_NOT_INJECTED_MESSAGE, BATCH_SIZE_UNFILTERED_EXPORT, DEFAULT_BATCH_SIZE_UNFILTERED_EXPORT);
            return DEFAULT_BATCH_SIZE_UNFILTERED_EXPORT;
        }
        if (unfilteredExportBatchSize > iteratorMoCount) {
            logger.info("{} config param is greater than iterator mo count {}, returning default batch size {}", BATCH_SIZE_UNFILTERED_EXPORT,
                    iteratorMoCount, DEFAULT_BATCH_SIZE_UNFILTERED_EXPORT);
            return DEFAULT_BATCH_SIZE_UNFILTERED_EXPORT;
        }
        logger.debug("Returning {} param batch size for unfiltered  nodes {}", BATCH_SIZE_UNFILTERED_EXPORT, unfilteredExportBatchSize);
        return unfilteredExportBatchSize.intValue();
    }

    public void listenForMaxJobsChanges(
            @Observes @ConfigurationChangeNotification(propertyName = MAX_JOBS_CONFIG_PARAM) final String value) {
        logger.info("maxJobs config param changed, old value = [{}], new value = [{}] ", maxJobs, value);
        maxJobs = value;
    }

    public void listenForMaxBigJobsChanges(
            @Observes @ConfigurationChangeNotification(propertyName = MAX_BIG_JOBS_CONFIG_PARAM) final String value) {
        logger.info("maxBigJobs config param changed, old value = [{}], new value = [{}] ", maxBigJobs, value);
        maxBigJobs = value;
    }

    public void listenForSmallJobNodeLimitChanges(
            @Observes @ConfigurationChangeNotification(propertyName = SMALL_JOB_NODE_LIMIT) final String value) {
        logger.info("smallJobNodeLimit config param changed, old value = [{}], new value = [{}] ", smallJobNodeLimit, value);
        smallJobNodeLimit = value;
    }

    public void listenForMaxJobsCheckChanges(
            @Observes @ConfigurationChangeNotification(propertyName = MAX_JOBS_CHECK_CONFIG_PARAM) final String value) {
        logger.info("maxJobsCheck config param changed, old value = [{}], new value = [{}] ", maxJobsCheck, value);
        maxJobsCheck = value;
    }

    public void listenForMessageReceiveTimeoutChanges(
            @Observes @ConfigurationChangeNotification(propertyName = MSG_RECEIVE_TIMEOUT_CONFIG_PARAM) final String value) {
        logger.info("messageReceiveTimeout config param changed, old value = [{}], new value = [{}] ", messageReceiveTimeout, value);
        messageReceiveTimeout = value;
    }

    public void listenForExportNonSynchronizedNodesChanges(
            @Observes @ConfigurationChangeNotification(propertyName = EXPORT_NON_SYNCHRONIZED_NODES_CONFIG_PARAM) final String value) {
        logger.info("exportNonSynchronizedNodes config param changed, old value = [{}], new value = [{}] ", exportNonSynchronizedNodes, value);
        exportNonSynchronizedNodes = value;
    }

    public void listenForExportCppInventoryMosChanges(
            @Observes @ConfigurationChangeNotification(propertyName = EXPORT_CPP_INVENTORY_MOS_CONFIG_PARAM) final String value) {
        logger.info("exportCppInventoryMos config param changed, old value = [{}], new value = [{}] ", exportCppInventoryMos, value);
        exportCppInventoryMos = value;
    }

    public void listenForExportEnumTranslateChanges(
            @Observes @ConfigurationChangeNotification(propertyName = EXPORT_ENUM_TRANSLATE_CONFIG_PARAM) final String value) {
        logger.info("exportEnumTranslate config param changed, old value = [{}], new value = [{}] ", exportEnumTranslate, value);
        exportEnumTranslate = value;
    }

    public void listenForIgnoreNpaforNonSynchedNodesChanges(
            @Observes @ConfigurationChangeNotification(propertyName = EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES_CONFIG_PARAM) final Boolean value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, EXPORT_IGNORE_NON_PERSISTENT_ATTRIBUTES_CONFIG_PARAM, ignoreNpaforNonSynchedNodes, value);
        ignoreNpaforNonSynchedNodes = value;
    }

    public void listenForIncludeEnmData3gppChanges(
            @Observes @ConfigurationChangeNotification(propertyName = EXPORT_INCLUDE_ENM_DATA_3GPP_CONFIG_PARAM) final Boolean value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, EXPORT_INCLUDE_ENM_DATA_3GPP_CONFIG_PARAM, includeEnmData3gpp, value);
        includeEnmData3gpp = value;
    }

    public void listenForOrderMosByFdnChanges(
            @Observes @ConfigurationChangeNotification(propertyName = EXPORT_ORDER_MOS_BY_FDN_PARAM) final Boolean value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, EXPORT_ORDER_MOS_BY_FDN_PARAM, orderMosByFdn, value);
        orderMosByFdn = value;
    }

    public void listenForExportBatchChanges(
            @Observes @ConfigurationChangeNotification(propertyName = BATCH_EXPORT) final Boolean value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, BATCH_EXPORT, exportBatch, value);
        exportBatch = value;
    }

    public void listenForNodeSizeEligibleBatchChanges(
            @Observes @ConfigurationChangeNotification(propertyName = NODESIZE_BATCHINGELIGIBLE) final Integer value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, NODESIZE_BATCHINGELIGIBLE, nodeSizeEligibleBatch, value);
        nodeSizeEligibleBatch = value;
    }

    public void listenForEniqExportBatchSizeChanges(
            @Observes @ConfigurationChangeNotification(propertyName = BATCH_SIZE_ENIQ_EXPORT) final Integer value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, BATCH_SIZE_ENIQ_EXPORT, eniqExportBatchSize, value);
        eniqExportBatchSize = value;
    }

    public void listenForFilteredExportBatchSizeChanges(
            @Observes @ConfigurationChangeNotification(propertyName = BATCH_SIZE_FILTERED_NODE_EXPORT) final Integer value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, BATCH_SIZE_FILTERED_NODE_EXPORT, filteredExportBatchSize, value);
        filteredExportBatchSize = value;
    }

    public void listenForUnfilteredExportBatchSizeChanges(
            @Observes @ConfigurationChangeNotification(propertyName = BATCH_SIZE_UNFILTERED_EXPORT) final Integer value) {
        logger.info(CONFIG_PARAM_CHANGED_MESSAGE, BATCH_SIZE_UNFILTERED_EXPORT, unfilteredExportBatchSize, value);
        unfilteredExportBatchSize = value;
    }

    public void listenForNodeSizeCategoryMinimumMoCountsChanges(
            @Observes @ConfigurationChangeNotification(propertyName = EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM) final int[] values) {
        if (values.length != 2) {
            logger.error("{} requires medium node minimum MO count, and large node minimum MO count values to be set: values = {}",
                    EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, values);
            return;
        }
        final int newMediumNodeMinimumMoCount = values[0];
        final int newLargeNodeMinimumMoCount = values[1];

        if (newMediumNodeMinimumMoCount < 1 || newLargeNodeMinimumMoCount < 1) {
            logger.error("{} medium node minimum MO count and large node minimum MO count values cannot be less than 1: values = {}",
                    EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, values);
        } else if (newMediumNodeMinimumMoCount > newLargeNodeMinimumMoCount) {
            logger.error("{} medium node minimum MO count value [{}] cannot be greater than large node minimum MO count value [{}]",
                    EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, newMediumNodeMinimumMoCount, newLargeNodeMinimumMoCount);
        } else {
            logger.info(CONFIG_PARAM_CHANGED_MESSAGE, EXPORT_NODE_SIZE_CATEGORY_MINIMUM_MO_COUNTS_PARAM, nodeMinimumMoCounts, values);
            nodeMinimumMoCounts = values;
        }
    }

    private boolean isValidBooleanString(final String booleanString) {
        return VALID_BOOLEAN_STRINGS.contains(booleanString.toLowerCase());
    }

    /**
     * TODO move this to export-common when it's established https://jira-nam.lmera.ericsson.se/browse/TORF-276995
     */
    private static boolean isStringNullOrEmpty(final String str) {
        return null == str || str.trim().isEmpty();
    }
}
