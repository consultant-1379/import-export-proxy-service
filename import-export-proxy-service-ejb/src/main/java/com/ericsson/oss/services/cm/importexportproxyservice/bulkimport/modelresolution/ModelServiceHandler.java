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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.modelresolution;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.InheritanceQualifier;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.MimMappedTo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.Target;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeVersionInformation;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.cm.bulkimport.util.FormatLogger;

/**
 * Class to handle information about models, and model content using {@link ModelService}.
 */
public class ModelServiceHandler {
    private static final String METHOD_NAME = "Method name: {}";
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(ModelServiceHandler.class));

    @Inject
    ModelService modelService;

    /**
     * Gets Model info using namespace and version of specified Managed Object for a given Managed Object type.
     *
     * @param moType              model managed object type
     * @param parentManagedObject parent managed object
     * @return {@link ModelInfo} model info
     */
    public ModelInfo getModelInfoFromNamespaceOfParentManagedObject(final String moType, final ManagedObject parentManagedObject) {
        //TODO: Check how is this different from the algorithm implemented in ModelServiceUtil
        ModelServiceHandler.LOGGER.debug(METHOD_NAME, "getModelInfoFromNamespaceOfParentManagedObject");
        ModelServiceHandler.LOGGER.trace("Argument values: moType = {}, parentManagedObject fully-distinguished-name = {}", moType,
                parentManagedObject.getFdn());
        return getModelInfoForSpecificNamespaceAndVersion(moType, parentManagedObject.getNamespace(), parentManagedObject.getVersion());
    }

    /**
     * Gets primary type specification for a given Model Info.
     *
     * @param modelInfo model info
     * @return {@link PrimaryTypeSpecification} object containing the meta-data for a modeled primary type
     */
    public PrimaryTypeSpecification getPrimaryTypeSpecification(final ModelInfo modelInfo) {
        ModelServiceHandler.LOGGER.debug(METHOD_NAME, "getPrimaryTypeSpecification");
        return modelService.getTypedAccess().getEModelSpecification(modelInfo, PrimaryTypeSpecification.class);
    }

    /**
     * Gets primary type specification for a given Model Info and Node Info combination. It is possible that similar model info (namespace, version
     * and mo type combination) be applicable for multiple node types.
     *
     * @param modelInfo model info
     * @param neType node type
     * @param ossModelIdentity ossModelIdentity of the NE.
     * @return {@link PrimaryTypeSpecification} object containing the meta-data for a modeled primary type
     */
    public PrimaryTypeSpecification getPrimaryTypeSpecification(final ModelInfo modelInfo, final String neType, final String ossModelIdentity) {
        ModelServiceHandler.LOGGER.debug(METHOD_NAME, "getPrimaryTypeSpecification");
        final Target target = new Target(TargetTypeInformation.CATEGORY_NODE, neType, null, ossModelIdentity);
        return modelService.getTypedAccess().getEModelSpecification(modelInfo, PrimaryTypeSpecification.class, target);
    }

    /**
     * Gets hierarchical primary type specification for a given Model Info.
     *
     * @param modelInfo model info
     * @return {@link HierarchicalPrimaryTypeSpecification} object containing the meta-data for a modeled hierarchical primary type
     */
    public HierarchicalPrimaryTypeSpecification getHierarchicalPrimaryTypeSpecification(final ModelInfo modelInfo) {
        return modelService.getTypedAccess().getEModelSpecification(modelInfo, HierarchicalPrimaryTypeSpecification.class);
    }

    /**
     * Gets hierarchical primary type specification for a given Model Info and node Info.
     *
     * @param modelInfo model info
     * @param neType node type
     * @param ossModelIdentity ossModelIdentity of the NE.
     * @return {@link HierarchicalPrimaryTypeSpecification} object containing the meta-data for a modeled hierarchical primary type
     */
    public HierarchicalPrimaryTypeSpecification getHierarchicalPrimaryTypeSpecification(final ModelInfo modelInfo,
            final String neType, final String ossModelIdentity) {
        final Target target = new Target(TargetTypeInformation.CATEGORY_NODE, neType, null, ossModelIdentity);
        return modelService.getTypedAccess().getEModelSpecification(modelInfo, HierarchicalPrimaryTypeSpecification.class, target);
    }

    /**
     * Gets the complex data type specification for a given Model Info.
     *
     * @param modelInfo the Model Info representing the complex data type
     * @return {@link ComplexDataTypeSpecification} object containing the meta-data for a modeled complex type
     */
    public ComplexDataTypeSpecification getComplexTypeSpecification(final ModelInfo modelInfo) {
        ModelServiceHandler.LOGGER.debug(METHOD_NAME, "getComplexTypeSpecification");
        return modelService.getTypedAccess().getEModelSpecification(
                new ModelInfo(SchemaConstants.OSS_CDT, modelInfo.getNamespace(), modelInfo.getName(), modelInfo.getVersion().toString()),
                ComplexDataTypeSpecification.class);
    }

    /**
     * Gets the Enum data type specification for a given Model Info.
     *
     * @param modelInfo the Model Info representing the Enum data type
     * @return {@link EnumDataTypeSpecification} object containing the meta-data for a modeled enum type
     */
    public EnumDataTypeSpecification getEnumDataTypeSpecification(final ModelInfo modelInfo) {
        ModelServiceHandler.LOGGER.debug(METHOD_NAME, "getEnumDataTypeSpecification");
        return modelService.getTypedAccess().getEModelSpecification(modelInfo, EnumDataTypeSpecification.class);
    }

    public Collection<MimMappedTo> getMimsMappedTo(final String ossModelIdentity, final String neType) {
        final TargetTypeInformation targetTypeInformation = modelService.getTypedAccess().getModelInformation(TargetTypeInformation.class);
        final TargetTypeVersionInformation targetTypeVersionInformation = targetTypeInformation
                .getTargetTypeVersionInformation(TargetTypeInformation.CATEGORY_NODE, neType);
        return targetTypeVersionInformation.getMimsMappedTo(ossModelIdentity);
    }

    private ModelInfo getModelInfoForSpecificNamespaceAndVersion(final String moType, final String namespace, final String namespaceVersion) {
        ModelServiceHandler.LOGGER.debug(METHOD_NAME, "getModelInfoForSpecificNamespaceAndVersion");
        ModelServiceHandler.LOGGER.trace("Argument values: moType = {}, namespace = {}, namespace version = {}", moType, namespace,
                namespaceVersion);
        final String typedModelUrn = getTypedModelUrn(moType, namespace, namespaceVersion);
        final ModelMetaInformation modelMetaInformation = modelService.getModelMetaInformation();
        final Collection<ModelInfo> latestModels = modelMetaInformation.getLatestModelsFromUrn(typedModelUrn);
        final Iterator<ModelInfo> iterator = latestModels.iterator();
        if (iterator.hasNext()) {
            final ModelInfo latestModel = iterator.next();
            try {
                final PrimaryTypeSpecification primaryTypeSpecification = getPrimaryTypeSpecification(latestModel);
                if (primaryTypeSpecification.getInheritanceQualifier() == InheritanceQualifier.ABSTRACT) {
                    return iterator.next();
                } else {
                    return latestModel;
                }
            } catch (final NoSuchElementException exception) {
                ModelServiceHandler.LOGGER.trace("Only abstract model found", exception);
                return null;
            }
        }
        return null;
    }

    private String getTypedModelUrn(final String type, final String namespace, final String namespaceVersion) {
        ModelServiceHandler.LOGGER.debug(METHOD_NAME, "getTypedModelUrn");
        ModelServiceHandler.LOGGER.trace("Argument values: type = {}, namespace = {}, namespaceVersion = {}", type, namespace, namespaceVersion);
        final String namespaceFilter = namespace == null ? "*" : namespace;
        final String versionFilter = namespaceVersion == null ? "*" : namespaceVersion;
        return "/" + SchemaConstants.DPS_PRIMARYTYPE + "/" + namespaceFilter + "/" + type + "/" + versionFilter;
    }
}
