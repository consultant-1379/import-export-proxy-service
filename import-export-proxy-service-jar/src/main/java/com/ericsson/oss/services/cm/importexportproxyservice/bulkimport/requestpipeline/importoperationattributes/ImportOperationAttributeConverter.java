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

import static com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.importOperationAttributeDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.exception.UnknownElementException;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.ComplexAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.ComplexAttributeListDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.SimpleAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.SimpleAttributeListDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

/**
 * Converter class for ImportOperation attributes.
 */
public class ImportOperationAttributeConverter implements Converter<ImportOperation, List<ImportOperationAttributeDto>> {

    static final Logger LOGGER = LoggerFactory.getLogger(ImportOperationAttributeConverter.class);

    @Inject
    SimpleAttributeConverter simpleAttributeConverter;

    @Inject
    ComplexAttributeConverter complexAttributeConverter;

    @Inject
    ModelService modelService;

    @Override
    public List<ImportOperationAttributeDto> convert(final ImportOperation operation) {
        LOGGER.debug("Converting importOperation [{}]", operation.getId());
        final List<ImportOperationAttributeDto> attributes = new ArrayList<>();
        if (operationModelInfoIsNull(operation)) {
            attributes.addAll(simpleAttributeConverter.convert(operation.getSimpleAttributes()));
            attributes.addAll(complexAttributeConverter.convert(operation.getComplexAttributes()));
        } else {
            final PrimaryTypeSpecification primaryTypeSpecification = resolvePrimaryTypeSpecification(operation);
            attributes.addAll(getProperlyTypedSimpleAttributes(operation, primaryTypeSpecification));
            attributes.addAll(getProperlyTypedComplexAttributes(operation, primaryTypeSpecification));
        }
        LOGGER.debug("Attributes converted for importOperation [{}], [{}] attributes found", operation.getId(), attributes.size());
        return sort(attributes);
    }

    private List<ImportOperationAttributeDto> sort(final List<ImportOperationAttributeDto> attributes) {
        Collections.sort(attributes, ID_COMPARATOR);
        return attributes;
    }

    @SuppressWarnings("checkstyle:declarationorder")
    static final Comparator<ImportOperationAttributeDto> ID_COMPARATOR = new Comparator<ImportOperationAttributeDto>() {
        @Override
        public int compare(final ImportOperationAttributeDto first, final ImportOperationAttributeDto second) {
            return first.getId().compareTo(second.getId());
        }
    };

    public List<ImportOperationAttributeDto> getProperlyTypedSimpleAttributes(final ImportOperation operation,
            final PrimaryTypeSpecification primaryTypeSpecification) {
        final List<ImportOperationAttributeDto> importOperationAttributeDtos =
                simpleAttributeConverter.convert(operation.getSimpleAttributes(), primaryTypeSpecification);
        final List<ImportOperationAttributeDto> listTypeAttributesWithSingleElement = new ArrayList<>();
        for (final ImportOperationAttributeDto importOperationAttributeDto : importOperationAttributeDtos) {
            if (importOperationAttributeDto instanceof SimpleAttributeDto
                    && isListDataType(primaryTypeSpecification, importOperationAttributeDto.getName())) {
                listTypeAttributesWithSingleElement.add(importOperationAttributeDto);
            }
        }
        for (final ImportOperationAttributeDto importOperationAttributeDto : listTypeAttributesWithSingleElement) {
            importOperationAttributeDtos.add(convertSimpleAttributeToListDto(importOperationAttributeDto));
        }
        importOperationAttributeDtos.removeAll(listTypeAttributesWithSingleElement);
        return importOperationAttributeDtos;
    }

    public List<ImportOperationAttributeDto> getProperlyTypedComplexAttributes(final ImportOperation operation,
            final PrimaryTypeSpecification primaryTypeSpecification) {
        final List<ImportOperationAttributeDto> importOperationAttributeDtos =
                complexAttributeConverter.convert(operation.getComplexAttributes(), primaryTypeSpecification);
        final List<ImportOperationAttributeDto> listTypeAttributesWithMultipleElements = new ArrayList<>();
        for (final ImportOperationAttributeDto importOperationAttributeDto : importOperationAttributeDtos) {
            if (importOperationAttributeDto instanceof ComplexAttributeDto
                    && isListDataType(primaryTypeSpecification, importOperationAttributeDto.getName())) {
                listTypeAttributesWithMultipleElements.add(importOperationAttributeDto);
            }
        }
        for (final ImportOperationAttributeDto importOperationAttributeDto : listTypeAttributesWithMultipleElements) {
            importOperationAttributeDtos.add(convertComplexAttributeToListDto(importOperationAttributeDto));
        }
        importOperationAttributeDtos.removeAll(listTypeAttributesWithMultipleElements);
        return importOperationAttributeDtos;
    }

    public PrimaryTypeSpecification resolvePrimaryTypeSpecification(final ImportOperation operation) {
        final ModelInfo modelInfo = getModelInfo(operation);
        return modelService.getTypedAccess().getEModelSpecification(modelInfo, PrimaryTypeSpecification.class);
    }

    public SimpleAttributeListDto convertSimpleAttributeToListDto(final ImportOperationAttributeDto simpleAttributeDto) {
        final List value = new ArrayList();
        value.add(simpleAttributeDto.getValue());
        return new SimpleAttributeListDto(importOperationAttributeDto()
                .withId(simpleAttributeDto.getId())
                .withOperationId(simpleAttributeDto.getOperationId())
                .withName(simpleAttributeDto.getName())
                .withValue(value));
    }

    public ComplexAttributeListDto convertComplexAttributeToListDto(final ImportOperationAttributeDto complexAttributeDto) {
        final List<Map<String, Object>> value = new ArrayList();
        value.add((Map<String, Object>) complexAttributeDto.getValue());
        return new ComplexAttributeListDto(importOperationAttributeDto()
                .withId(complexAttributeDto.getId())
                .withOperationId(complexAttributeDto.getOperationId())
                .withName(complexAttributeDto.getName())
                .withValue(value));
    }

    private ModelInfo getModelInfo(final ImportOperation operation) {
        return new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE, operation.getImportOperationModelInfo().getNamespace(),
                operation.getImportOperationModelInfo().getModelName(), operation.getImportOperationModelInfo().getVersion());
    }

    private boolean isListDataType(final PrimaryTypeSpecification primaryTypeSpecification, final String attributeName) {
        try {
            final DataTypeSpecification specification = primaryTypeSpecification.getAttributeSpecification(attributeName).getDataTypeSpecification();
            return DataType.LIST.equals(specification.getDataType());
        } catch (final UnknownElementException e) {
            LOGGER.debug("Model Resolution Exception is :", e);
            return false;
        }
    }

    private boolean operationModelInfoIsNull(final ImportOperation operation) {
        return operation.getImportOperationModelInfo() == null || operation.getImportOperationModelInfo().getModelName() == null
                || operation.getImportOperationModelInfo().getModelName().isEmpty();
    }
}