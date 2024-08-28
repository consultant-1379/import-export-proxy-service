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

import static com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.SimpleAttributeDto;
import static com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.SimpleAttributeListDto;

import java.util.Map;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.exception.model.NotDefinedInModelException;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.attributes.SimpleAttributeV2;

/**
 * Converter class for simple attributes.
 */
class SimpleAttributeConverter extends AttributeConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeConverter.class);

    @Inject
    SimpleElementConverter simpleElementConverter;

    @SuppressWarnings("unchecked")
    protected void addAttribute(final Map<String, ImportOperationAttributeDto> processedAttributes, final Object newAttribute,
            final PrimaryTypeSpecification primaryTypeSpecification) {
        final SimpleAttributeV2 simpleAttribute = (SimpleAttributeV2) newAttribute;
        final SimpleAttributeDto newAttributeDto =
                simpleElementConverter.convert(simpleAttribute, isSensitiveAttribute(primaryTypeSpecification, simpleAttribute));
        final ImportOperationAttributeDto existingAttributeDto = processedAttributes.get(newAttributeDto.getName());
        if (existingAttributeDto == null) {
            processedAttributes.put(newAttributeDto.getName(), newAttributeDto);
        } else if (existingAttributeDto instanceof SimpleAttributeDto) {
            processedAttributes.remove(newAttributeDto.getName());
            processedAttributes.put(existingAttributeDto.getName(), simpleElementConverter.convert((SimpleAttributeDto) existingAttributeDto,
                    newAttributeDto));
        } else if (existingAttributeDto instanceof SimpleAttributeListDto) {
            ((SimpleAttributeListDto) existingAttributeDto).getValue().add(simpleElementConverter
                    .getValueAndNamespaceForList(newAttributeDto.getValue(), newAttributeDto.getNamespace()));
            ((SimpleAttributeListDto) existingAttributeDto).getNamespace().add(newAttributeDto.getNamespace());
        } else {
            //Not supported
        }
    }

    private boolean isSensitiveAttribute(final PrimaryTypeSpecification primaryTypeSpecification, final SimpleAttributeV2 simpleAttribute) {
        try {
            return primaryTypeSpecification != null && primaryTypeSpecification.getAttributeSpecification(simpleAttribute.getName()).isSensitive();
        } catch (final NotDefinedInModelException e) {
            LOGGER.warn("Attribute [{}] is not defined in model, ignoring.", simpleAttribute.getName(), e);
            return false;
        } catch (final Exception e) {
            LOGGER.warn("Exception occured while fetching Model for Attribute [{}], ignoring.", simpleAttribute.getName(), e);
            return false;
        }
    }
}