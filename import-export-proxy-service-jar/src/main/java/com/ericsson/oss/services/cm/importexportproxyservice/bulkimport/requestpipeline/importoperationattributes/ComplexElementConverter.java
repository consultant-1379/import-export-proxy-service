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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes;

import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.LIST;
import static com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.importOperationAttributeDto;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.exception.model.NotDefinedInModelException;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeAttributeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.ComplexAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.ComplexAttributeListDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.attributes.ComplexAttributeV2;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.attributes.ComplexElementV2;

/**
 * Converter class for complex elements.
 */
class ComplexElementConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComplexElementConverter.class);

    @Inject
    ModelService modelService;

    public ComplexAttributeDto convert(final ComplexAttributeV2 attribute, final PrimaryTypeSpecification primaryTypeSpecification) {
        if (primaryTypeSpecification == null) {
            return convert(attribute);
        }
        final ComplexDataTypeSpecification complexDataSpec = getComplexTypeSpecification(primaryTypeSpecification, attribute.getName());
        if (complexDataSpec == null) {
            return convert(attribute);
        }
        final Collection<ComplexDataTypeAttributeSpecification> allAttrSpecs = complexDataSpec.getAllAttributeSpecifications();
        if (allAttrSpecs.stream().noneMatch(spec -> spec.isSensitive())) {
            return convert(attribute);
        }
        final Map<String, Object> value = new HashMap<>();
        for (final ComplexElementV2 complexElement : attribute.getValue()) {
            final Object object = getComplexElementValue(complexElement.getValue(),
                    complexDataSpec.getAttributeSpecification(complexElement.getName()).isSensitive());
            if (value.containsKey(complexElement.getName())) {
                if (value.get(complexElement.getName()) instanceof List) {
                    final List<Object> tmpList = (List<Object>) value.get(complexElement.getName());
                    tmpList.add(object);
                    value.put(complexElement.getName(), tmpList);
                } else {
                    final List<Object> attributeList = new ArrayList<>();
                    attributeList.add(value.get(complexElement.getName()));
                    attributeList.add(object);
                    value.put(complexElement.getName(), attributeList);
                }
            } else {
                value.put(complexElement.getName(), object);
            }
        }
        return new ComplexAttributeDto(importOperationAttributeDto()
                .withId(attribute.getId())
                .withName(attribute.getName())
                .withValue(value)
                .withLineNumber(attribute.getLineNumber())
                .withOperationId(attribute.getImportOperation().getId()));
    }

    public ComplexAttributeDto convert(final ComplexAttributeV2 attribute) {
        final Map<String, Object> value = new HashMap<>();
        for (final ComplexElementV2 complexElement : attribute.getValue()) {
            final Object object = getComplexElementValue(complexElement.getValue(), false);
            if (value.containsKey(complexElement.getName())) {
                if (value.get(complexElement.getName()) instanceof List) {
                    final List<Object> tmpList = (List<Object>) value.get(complexElement.getName());
                    tmpList.add(object);
                    value.put(complexElement.getName(), tmpList);
                } else {
                    final List<Object> attributeList = new ArrayList<>();
                    attributeList.add(value.get(complexElement.getName()));
                    attributeList.add(object);
                    value.put(complexElement.getName(), attributeList);
                }
            } else {
                value.put(complexElement.getName(), object);
            }
        }
        return new ComplexAttributeDto(importOperationAttributeDto()
                .withId(attribute.getId())
                .withName(attribute.getName())
                .withValue(value)
                .withLineNumber(attribute.getLineNumber())
                .withOperationId(attribute.getImportOperation().getId()));
    }

    public ComplexAttributeListDto convert(final ComplexAttributeDto first, final ComplexAttributeDto second) {
        final List<Map<String, Object>> value = new ArrayList();
        value.add(first.getValue());
        value.add(second.getValue());
        return new ComplexAttributeListDto(importOperationAttributeDto()
                .withId(first.getId())
                .withOperationId(first.getOperationId())
                .withName(first.getName())
                .withValue(value));
    }

    private Object getComplexElementValue(final Object value, final boolean mask) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return mask ? MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE : value;
        } else if (value instanceof Collection) {
            final Map<String, Object> entryMap = new HashMap<>();
            for (final Object element : (Collection) value) {
                final ComplexElementV2 complexElementV2 = (ComplexElementV2) element;
                if (complexElementV2.getValue() instanceof String) {
                    entryMap.put(complexElementV2.getName(), mask ? MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE : complexElementV2.getValue());
                } else {
                    entryMap.put(complexElementV2.getName(), "<attribute type not supported>");
                }
            }
            return entryMap;
        }
        LOGGER.warn("Unexpected attribute type {}", value.getClass().getName());
        return "<unknown attribute type>"; // TODO ?
    }

    public ComplexDataTypeSpecification getComplexTypeSpecification(final PrimaryTypeSpecification primaryTypeSpecification,
            final String attributeName) {
        try {
            final DataTypeSpecification dataTypeSpecification = primaryTypeSpecification.getAttributeSpecification(attributeName)
                    .getDataTypeSpecification();
            final DataType attributeDataType = dataTypeSpecification.getDataType();
            ModelInfo modelInfo = modelInfo = dataTypeSpecification.getReferencedDataType();
            if (attributeDataType == LIST) {
                final DataTypeSpecification innerDataTypeSpecification = dataTypeSpecification.getValuesDataTypeSpecification();
                modelInfo = innerDataTypeSpecification.getReferencedDataType();
            }
            return modelService.getTypedAccess().getEModelSpecification(new ModelInfo(SchemaConstants.OSS_CDT, modelInfo.getNamespace(),
                    modelInfo.getName(), modelInfo.getVersion().toString()), ComplexDataTypeSpecification.class);
        } catch (final NotDefinedInModelException e) {
            LOGGER.warn("Attribute [{}] is not defined in model, ignoring.", attributeName, e);
            return null;
        } catch (final Exception e) {
            LOGGER.warn("Exception occured while fetching Model for Attribute [{}], ignoring.", attributeName, e);
            return null;
        }
    }
}
