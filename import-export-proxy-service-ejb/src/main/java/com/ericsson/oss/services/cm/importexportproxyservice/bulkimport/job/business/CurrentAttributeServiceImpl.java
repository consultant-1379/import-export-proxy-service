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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.business;

import static com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.ReadBehavior.FROM_DELEGATE;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.LIST;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.CURRENT_VALUE_FOR_NON_PERSISTENT_ATTRIBUTE;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.CURRENT_VALUE_FOR_OBSOLETE_ATTRIBUTE;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.exception.model.NotDefinedInModelException;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.LifeCycleState;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeAttributeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.exception.UnknownElementException;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.attributes.ComplexAttributeV2;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.modelresolution.ModelServiceHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.parser.attributes.AttributeType;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.CurrentAttributeService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util.FormatLogger;

/**
 * Service implementation to get current values from DPS.
 */
@Stateless
public class CurrentAttributeServiceImpl implements CurrentAttributeService {
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(CurrentAttributeServiceImpl.class));

    @EServiceRef
    DataPersistenceService dps;

    @Inject
    ModelServiceHandler modelServiceHandler;

    @Override
    public Map<String, Object> getCurrentAttributeValues(final ImportOperation operation, final boolean isNonPersistentAttrRequired) {
        final Map<String, Object> currentAttributes = new HashMap<>();
        final DataBucket dataBucket = dps.getLiveBucket();
        final ManagedObject managedObject = dataBucket.findMoByFdn(operation.getFdn());
        if (managedObject == null) {
            LOGGER.debug("Managed object [{}] not found, returning null", operation.getFdn());
            return null;
        }
        final PrimaryTypeSpecification primaryTypeSpecification = getPrimaryTypeSpecification(operation);
        currentAttributes.putAll(populateSimpleCurrentAttributes(operation, managedObject, primaryTypeSpecification, isNonPersistentAttrRequired));
        currentAttributes.putAll(populateComplexCurrentAttributes(operation, managedObject, primaryTypeSpecification, isNonPersistentAttrRequired));
        return currentAttributes;
    }

    private Map<String, Object> populateSimpleCurrentAttributes(final ImportOperation operation, final ManagedObject managedObject,
            final PrimaryTypeSpecification primaryTypeSpecification, final boolean isNonPersistentAttrRequired) {
        final Map<String, Object> currentAttr = new HashMap<>();
        operation.getSimpleAttributes().forEach(simpleAttribute -> {
            final String attributeName = simpleAttribute.getName();
            if (isObsolete(simpleAttribute.getLifeCycleState())) {
                LOGGER.trace("Get current value of attribute {} is skipped as it's lifecycle state is OBSOLETE.", attributeName);
                currentAttr.put(attributeName, CURRENT_VALUE_FOR_OBSOLETE_ATTRIBUTE);
            } else if (isNonPersistantAttribute(primaryTypeSpecification, attributeName, isNonPersistentAttrRequired)) {
                LOGGER.trace("Get current value of attribute {} is skipped as the attribute is non-persistent.", attributeName);
                currentAttr.put(attributeName, CURRENT_VALUE_FOR_NON_PERSISTENT_ATTRIBUTE);
            } else {
                try {
                    if (primaryTypeSpecification != null && primaryTypeSpecification.getAttributeSpecification(attributeName).isSensitive()) {
                        LOGGER.trace("Attribute is sensitive", attributeName);
                        currentAttr.put(attributeName, MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE);
                    } else {
                        LOGGER.trace("Get value of attribute [{}]", attributeName);
                        if (simpleAttribute.getNamespace() != null && !simpleAttribute.getNamespace().isEmpty()) {
                            currentAttr.put(attributeName, applyConversionForEnumAttribute(managedObject.getAttribute(attributeName)));
                        } else {
                            currentAttr.put(attributeName, managedObject.getAttribute(attributeName));
                        }
                    }
                } catch (final NotDefinedInModelException e) {
                    LOGGER.warn("Attribute [{}] is not defined in model, ignoring.", attributeName, e);
                } catch (final Exception e) {
                    LOGGER.warn("Unexpected Exception occured while fetching Attribute [{}], ignoring.", attributeName, e);
                }
            }
        });
        return currentAttr;
    }

    @SuppressWarnings("unchecked")
    private void applySensitiveRuleOrGoDefault(final ComplexAttributeV2 complexAttribute,
            final PrimaryTypeSpecification primaryTypeSpecification,
            final ImportOperation importOperation,
            final ManagedObject managedObject,
            final Map<String, Object> currentAttr) {
        final String attributeName = complexAttribute.getName();
        final Object attributeCurrentValue = managedObject.getAttribute(attributeName);
        if (primaryTypeSpecification == null) {
            currentAttr.put(attributeName, attributeCurrentValue);
            return;
        }
        try {
            final DataTypeSpecification dataTypeSpecification = primaryTypeSpecification.getAttributeSpecification(attributeName)
                    .getDataTypeSpecification();
            final DataType attributeDataType = dataTypeSpecification.getDataType();
            ModelInfo modelInfo = modelInfo = dataTypeSpecification.getReferencedDataType();
            if (attributeDataType == LIST) {
                final DataTypeSpecification innerDataTypeSpecification = dataTypeSpecification.getValuesDataTypeSpecification();
                modelInfo = innerDataTypeSpecification.getReferencedDataType();
            }
            final ComplexDataTypeSpecification complexDataSpec = modelServiceHandler.getComplexTypeSpecification(modelInfo);
            final Collection<ComplexDataTypeAttributeSpecification> allAttrSpecs = complexDataSpec.getAllAttributeSpecifications();
            if (allAttrSpecs.stream().noneMatch(spec -> spec.isSensitive())) {
                currentAttr.put(attributeName, attributeCurrentValue);
                return;
            }
            final Map<String, Object> complexAttrElements = (Map<String, Object>) attributeCurrentValue;
            complexAttrElements.entrySet().forEach(entry -> {
                if (complexDataSpec.getAttributeSpecification(entry.getKey()).isSensitive()) {
                    complexAttrElements.put(entry.getKey(), MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE);
                }
            });
            currentAttr.put(attributeName, complexAttrElements);
        } catch (final NotDefinedInModelException e) {
            LOGGER.warn("Attribute [{}] is not defined in model, ignoring.", attributeName, e);
            return;
        } catch (final Exception e) {
            LOGGER.warn("Exception occured while fetching Model for Attribute [{}], ignoring.", attributeName, e);
            return;
        }
    }

    private Map<String, Object> populateComplexCurrentAttributes(final ImportOperation operation, final ManagedObject managedObject,
            final PrimaryTypeSpecification primaryTypeSpecification, final boolean isNonPersistentAttrRequired) {
        final Map<String, Object> currentAttr = new HashMap<>();
        operation.getComplexAttributes().forEach(complexAttribute -> {
            final String attributeName = complexAttribute.getName();
            if (isObsolete(complexAttribute.getLifeCycleState())) {
                LOGGER.trace("Get current value of attribute {} is skipped as it's lifecycle state is OBSOLETE.", attributeName);
                currentAttr.put(attributeName, CURRENT_VALUE_FOR_OBSOLETE_ATTRIBUTE);
            } else if (isNonPersistantAttribute(primaryTypeSpecification, attributeName, isNonPersistentAttrRequired)) {
                LOGGER.trace("Get current value of attribute {} is skipped as the attribute is non-persistent.", attributeName);
                currentAttr.put(attributeName, CURRENT_VALUE_FOR_NON_PERSISTENT_ATTRIBUTE);
            } else {
                try {
                    applySensitiveRuleOrGoDefault(complexAttribute, primaryTypeSpecification, operation, managedObject, currentAttr);
                } catch (final NotDefinedInModelException e) {
                    LOGGER.warn("Attribute [{}] is not defined in model, ignoring.", attributeName, e);
                } catch (final Exception e) {
                    LOGGER.warn("Exception occured while fetching Model for Attribute [{}], ignoring.", attributeName, e);
                }
            }
        });
        return currentAttr;
    }

    private PrimaryTypeSpecification getPrimaryTypeSpecification(final ImportOperation operation) {
        if (null != getModelInfo(operation)) {
            return modelServiceHandler.getPrimaryTypeSpecification(getModelInfo(operation));
        }
        return null;
    }

    private boolean isObsolete(final String lifeCycleState) {
        return LifeCycleState.OBSOLETE.getName().equals(lifeCycleState);
    }

    private boolean isNonPersistantAttribute(final PrimaryTypeSpecification primaryTypeSpecification, final String attributeName,
            final boolean isNonPersistentAttrRequired) {
        if (isNonPersistentAttrRequired || primaryTypeSpecification == null) {
            return false;
        }
        try {
            return FROM_DELEGATE.equals(primaryTypeSpecification.getAttributeSpecification(attributeName).getReadBehavior());
        } catch (final UnknownElementException exception) {
            LOGGER.debug("Unknown attribute type exception", exception);
        }
        return false;
    }

    private ModelInfo getModelInfo(final ImportOperation operation) {
        if (operation.getImportOperationModelInfo() == null || operation.getImportOperationModelInfo().getModelName() == null
                || operation.getImportOperationModelInfo().getModelName().isEmpty()) {
            return null;
        } else {
            return new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE, operation.getImportOperationModelInfo().getNamespace(),
                    operation.getImportOperationModelInfo().getModelName(), operation.getImportOperationModelInfo().getVersion());
        }
    }

    private Object applyConversionForEnumAttribute(final Object attributeValue) {
        final List<String> result = new ArrayList<>();
        if (attributeValue != null && AttributeType.LIST.equals(AttributeType.getTypeFromString(attributeValue.toString()))) {
            final String[] parsedList = extractStringBetweenChars(attributeValue.toString(), '[', ']').split(",");
            for (final String value : parsedList) {
                result.add(namespaceSplitForEnum(value.trim()));
            }
            return result;
        } else if (attributeValue != null && AttributeType.BASIC.equals(AttributeType.getTypeFromString(attributeValue.toString()))) {
            return namespaceSplitForEnum(attributeValue.toString());
        } else {
            return attributeValue;
        }
    }

    private String extractStringBetweenChars(final String inputString, final char startChar, final char finalChar) {
        final int from = inputString.indexOf(startChar) + 1;
        final int to = inputString.lastIndexOf(finalChar) == -1
                ? inputString.length()
                : inputString.lastIndexOf(finalChar);
        return inputString.substring(from, to);
    }

    private static String namespaceSplitForEnum(final String value) {
        if (value != null && value.contains("$$$")) {
            final String[] splitedValues = value.split("\\$\\$\\$");
            if (splitedValues.length > 1) {
                return splitedValues[1] + "[" + splitedValues[0] + "]";
            } else {
                return "";
            }
        }
        return value;
    }

}
