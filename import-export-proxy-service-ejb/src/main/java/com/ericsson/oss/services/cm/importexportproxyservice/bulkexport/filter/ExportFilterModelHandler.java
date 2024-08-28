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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.common.info.ModelVersionInfo;
import com.ericsson.oss.itpf.modeling.common.info.SimpleVersionInfo;
import com.ericsson.oss.itpf.modeling.common.info.XyzModelVersionInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.direct.DirectModelAccess;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;
import com.ericsson.oss.itpf.modeling.schema.gen.oss_common.UserExposureType;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.cm.export.api.ExportFilter;
import com.ericsson.oss.services.cm.export.api.ExportFilterWithDescription;
import com.ericsson.oss.services.exportservice.modeling.schema.gen.exs_exportfilter.ExportServiceFilterDefinition;
import com.ericsson.oss.services.exportservice.modeling.schema.gen.exs_exportfilter.PrimaryType;

/**
 * This class is used for get Filter model from model service.
 */
@Default
public class ExportFilterModelHandler {
    private static final String PREDEFINED_EXPORT_FILTER_URN = "/" + SchemaConstants.EXS_EXPORTFILTER + "/*/*/*";
    private static final String ENIQ_FILTERS_URN = "/" + SchemaConstants.EXS_EXPORTFILTER + "/EniqTopologyService/*/*";

    @Inject
    private ModelService modelService;

    @Inject
    private Logger logger;

    public List<PrimaryType> getModelList(final String namespace, final String name, final String version) {
        final DirectModelAccess dma = modelService.getDirectAccess();
        final ModelInfo modelInfo = getModelInfo(namespace, name, version);
        final ExportServiceFilterDefinition esfDef = dma.getAsJavaTree(modelInfo, ExportServiceFilterDefinition.class);
        final List<PrimaryType> models = new ArrayList<>();
        IntStream.range(0, esfDef.getNamespace().size())
                .forEach(index -> models.addAll(esfDef.getNamespace().get(index).getInclude()));
        return models;
    }

    private ModelInfo getModelInfo(final String namespace, final String name, final String version) {
        ModelInfo modelInfo = null;
        if (isVersionValid(version)) {
            modelInfo = new ModelInfo(SchemaConstants.EXS_EXPORTFILTER, namespace, name, version);
        } else {
            final ModelMetaInformation modelMetaInfo = modelService.getModelMetaInformation();
            modelInfo = modelMetaInfo.getLatestVersionOfModel(SchemaConstants.EXS_EXPORTFILTER, namespace, name);
        }
        return modelInfo;
    }

    public List<String> getManagedObjectsToFilter(final String namespace, final String name, final String version) {
        final List<String> mos = new ArrayList<>();
        final List<PrimaryType> ptList = getModelList(namespace, name, version);
        for (final PrimaryType pt : ptList) {
            mos.add(pt.getMoType());
        }
        return mos;
    }

    public boolean isExportFilterModelDeployed(final String namespace, final String name, final String version) {
        final ModelInfo exportModelInfo;
        final ModelMetaInformation modelMetaInfo;
        final boolean isModelDeployed;
        try {
            exportModelInfo = getModelInfo(namespace, name, version);
            modelMetaInfo = modelService.getModelMetaInformation();
            isModelDeployed = modelMetaInfo.isModelDeployed(exportModelInfo);
        } catch (final Exception modelServiceException) {
            logger.warn("Exception while accessing model service {}", modelServiceException);
            return false;
        }
        return isModelDeployed;
    }

    public List<ExportFilterWithDescription> getPredefinedExportFiltersWithDescriptions() {
        final List<ExportFilterWithDescription> predefinedFilters = new ArrayList<>();
        final ModelMetaInformation modelMetaInfo = modelService.getModelMetaInformation();
        final Collection<ModelInfo> filterModelInfos = modelMetaInfo.getModelsFromUrn(PREDEFINED_EXPORT_FILTER_URN);
        removeHiddenFilters(filterModelInfos);
        for (final ModelInfo filterModelInfo : filterModelInfos) {
            predefinedFilters.add(buildExportFilterWithDescription(filterModelInfo));
        }
        final ExportFilterComparator exportFilterComparator = new ExportFilterComparator();
        Collections.sort(predefinedFilters, exportFilterComparator);
        return predefinedFilters;
    }

    public List<ExportFilter> getPredefinedExportFilters() {
        final List<ExportFilter> predefinedFilters = new ArrayList<>();
        final ModelMetaInformation modelMetaInfo = modelService.getModelMetaInformation();
        final Collection<ModelInfo> filterModelInfos = modelMetaInfo.getModelsFromUrn(PREDEFINED_EXPORT_FILTER_URN);
        removeHiddenFilters(filterModelInfos);
        for (final ModelInfo filterModelInfo : filterModelInfos) {
            predefinedFilters.add(buildExportFilter(filterModelInfo));
        }
        return predefinedFilters;
    }

    private void removeHiddenFilters(final Collection<ModelInfo> filters) {
        final ModelMetaInformation modelMetaInfo = modelService.getModelMetaInformation();
        filters.removeAll(modelMetaInfo.getModelsFromUrn(ENIQ_FILTERS_URN));
        final DirectModelAccess dma = modelService.getDirectAccess();
        final Collection<ModelInfo> filtersToRemove = new ArrayList<>();
        for (final ModelInfo modelInfo : filters) {
            final ExportServiceFilterDefinition filterDefinition = dma.getAsJavaTree(modelInfo, ExportServiceFilterDefinition.class);
            if (UserExposureType.NEVER.equals(filterDefinition.getUserExposure())) {
                filtersToRemove.add(modelInfo);
            }
        }
        filters.removeAll(filtersToRemove);
    }

    private ExportFilterWithDescription buildExportFilterWithDescription(final ModelInfo filterModelInfo) {
        String version = "";
        final ModelVersionInfo versionInfo = filterModelInfo.getVersion();
        if (versionInfo.isSimpleVersion()) {
            final SimpleVersionInfo simpleVersionInfo = (SimpleVersionInfo) versionInfo;
            version = simpleVersionInfo.getVersion();
        } else if (versionInfo.isXyzVersion()) {
            final XyzModelVersionInfo xyzModelVersionInfo = (XyzModelVersionInfo) versionInfo;
            version = xyzModelVersionInfo.toString();
        }

        final DirectModelAccess dma = modelService.getDirectAccess();
        final ExportServiceFilterDefinition filterDefinition = dma.getAsJavaTree(filterModelInfo, ExportServiceFilterDefinition.class);
        final String description = filterDefinition.getDesc();
        return ExportFilterWithDescription.createFilterWithDescription(filterModelInfo.getNamespace(), filterModelInfo.getName(), version,
                description);
    }

    private ExportFilter buildExportFilter(final ModelInfo filterModelInfo) {
        String version = "";
        final ModelVersionInfo versionInfo = filterModelInfo.getVersion();
        if (versionInfo.isSimpleVersion()) {
            final SimpleVersionInfo simpleVersionInfo = (SimpleVersionInfo) versionInfo;
            version = simpleVersionInfo.getVersion();
        } else if (versionInfo.isXyzVersion()) {
            final XyzModelVersionInfo xyzModelVersionInfo = (XyzModelVersionInfo) versionInfo;
            version = xyzModelVersionInfo.toString();
        }

        return ExportFilter.createFilter(filterModelInfo.getNamespace(), filterModelInfo.getName(), version);
    }

    private boolean isVersionValid(final String version) {
        return version != null && !version.isEmpty();
    }
}
