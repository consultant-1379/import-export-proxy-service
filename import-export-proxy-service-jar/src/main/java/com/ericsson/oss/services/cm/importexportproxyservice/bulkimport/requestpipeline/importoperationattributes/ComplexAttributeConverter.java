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

import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.ComplexAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.ComplexAttributeListDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.attributes.ComplexAttributeV2;

/**
 * Converter class for complex attributes.
 */
class ComplexAttributeConverter extends AttributeConverter {

    @Inject
    ComplexElementConverter complexElementConverter;

    @Override
    @SuppressWarnings("unchecked")
    protected void addAttribute(final Map<String, ImportOperationAttributeDto> processedAttributes, final Object newAttribute,
            final PrimaryTypeSpecification primaryTypeSpecification) {
        final ComplexAttributeDto newAttributeDto = complexElementConverter.convert((ComplexAttributeV2) newAttribute, primaryTypeSpecification);
        final ImportOperationAttributeDto existingAttributeDto = processedAttributes.get(newAttributeDto.getName());
        if (existingAttributeDto == null) {
            processedAttributes.put(newAttributeDto.getName(), newAttributeDto);
        } else if (existingAttributeDto instanceof ComplexAttributeDto) {
            processedAttributes.remove(newAttributeDto.getName());
            processedAttributes.put(existingAttributeDto.getName(), complexElementConverter.convert((ComplexAttributeDto) existingAttributeDto,
                    newAttributeDto));
        } else if (existingAttributeDto instanceof ComplexAttributeListDto) {
            ((ComplexAttributeListDto) existingAttributeDto).getValue().add(newAttributeDto.getValue());
        }
    }
}
