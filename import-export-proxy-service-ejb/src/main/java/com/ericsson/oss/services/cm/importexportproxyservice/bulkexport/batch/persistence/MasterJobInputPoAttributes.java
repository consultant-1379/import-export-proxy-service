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

import java.util.List;
import java.util.Map;

/**
 * Contains the MasterJobInputPo attributes needed when starting a slave job.
 */
public class MasterJobInputPoAttributes {
    private String exportType;
    private String configName;
    private Boolean enumTranslate;
    private Boolean prettyFormat;
    private Boolean exportCppInventoryMos;
    private Boolean exportNonSynchronizedNodes;
    private Boolean batchFilter;
    private List<Object> dataCategories;
    private Map<String, Object> predefinedFilterPo;
    private List<String> partitionedNodes;
    private Map<String, Object> nodeSizeCategoryCount;
    private Map<String, Object> userDefinedFilterPo;

    public String getExportType() {
        return exportType;
    }

    public void setExportType(final String exportType) {
        this.exportType = exportType;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(final String configName) {
        this.configName = configName;
    }

    public Boolean getEnumTranslate() {
        return enumTranslate;
    }

    public void setEnumTranslate(final Boolean enumTranslate) {
        this.enumTranslate = enumTranslate;
    }

    public Boolean getPrettyFormat() {
        return prettyFormat;
    }

    public void setPrettyFormat(final Boolean prettyFormat) {
        this.prettyFormat = prettyFormat;
    }

    public void setBatchFilter(final Boolean batchFilter) {
        this.batchFilter = batchFilter;
    }

    public Boolean getBatchFilter() {
        return batchFilter;
    }

    public Boolean getExportCppInventoryMos() {
        return exportCppInventoryMos;
    }

    public void setExportCppInventoryMos(final Boolean exportCppInventoryMos) {
        this.exportCppInventoryMos = exportCppInventoryMos;
    }

    public Boolean getExportNonSynchronizedNodes() {
        return exportNonSynchronizedNodes;
    }

    public void setExportNonSynchronizedNodes(final Boolean exportNonSynchronizedNodes) {
        this.exportNonSynchronizedNodes = exportNonSynchronizedNodes;
    }

    public List<Object> getDataCategories() {
        return dataCategories;
    }

    public void setDataCategories(final List<Object> dataCategories) {
        this.dataCategories = dataCategories;
    }

    public Map<String, Object> getPredefinedFilterPo() {
        return predefinedFilterPo;
    }

    public void setPredefinedFilterPo(final Map<String, Object> predefinedFilterPo) {
        this.predefinedFilterPo = predefinedFilterPo;
    }

    public List<String> getPartitionedNodes() {
        return partitionedNodes;
    }

    public void setPartitionedNodes(final List<String> partitionedNodes) {
        this.partitionedNodes = partitionedNodes;
    }

    public Map<String, Object> getNodeSizeCategoryCount() {
        return nodeSizeCategoryCount;
    }

    public void setNodeSizeCategoryCount(final Map<String, Object> nodeSizeCategoryCount) {
        this.nodeSizeCategoryCount = nodeSizeCategoryCount;
    }

    public Map<String, Object> getUserDefinedFilterPo() {
        return userDefinedFilterPo;
    }

    public void setUserDefinedFilterPo(final Map<String, Object> userDefinedFilterPo) {
        this.userDefinedFilterPo = userDefinedFilterPo;
    }
}
