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
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.SimpleAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto.SimpleAttributeListDto;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.attributes.SimpleAttributeV2;

/**
 * Converter class for simple elements.
 */
class SimpleElementConverter {
    private static final String EMPTY_STRING = "";

    public SimpleAttributeDto convert(final SimpleAttributeV2 attribute, final boolean fillSensitiveAttr) {
        final String attrValue = fillSensitiveAttr ? MASKED_VALUE_FOR_SENSITIVE_ATTRIBUTE : namespaceSplitForValue(attribute.getValue());
        return new SimpleAttributeDto(importOperationAttributeDto()
                .withId(attribute.getId())
                .withOperationId(attribute.getImportOperation().getId())
                .withLineNumber(attribute.getLineNumber())
                .withName(attribute.getName())
                .withValue(attrValue)
                .withNamespace(attribute.getNamespace())
                .withDescription(attribute.getDescription()));
    }

    public ImportOperationAttributeDto.SimpleAttributeListDto convert(final SimpleAttributeDto first, final SimpleAttributeDto second) {
        final List value = new ArrayList();
        value.add(getValueAndNamespaceForList(first.getValue(), first.getNamespace()));
        value.add(getValueAndNamespaceForList(second.getValue(), second.getNamespace()));
        final List namespace = new ArrayList();
        namespace.add(first.getNamespace() != null ? first.getNamespace() : EMPTY_STRING);
        namespace.add(second.getNamespace() != null ? second.getNamespace() : EMPTY_STRING);
        return new SimpleAttributeListDto(importOperationAttributeDto()
                .withId(first.getId())
                .withOperationId(first.getOperationId())
                .withName(first.getName())
                .withValue(value)
                .withNamespace(namespace)
                .withDescription(null));
    }

    private static String namespaceSplitForValue(final String value) {
        if (value != null && value.contains("$$$")) {
            final String[] splitedValues =  value.split("\\$\\$\\$");
            if (splitedValues.length > 1) {
                return splitedValues[1];
            } else {
                return EMPTY_STRING;
            }
        }
        return value;
    }

    public String getValueAndNamespaceForList(final String value, final String namespace) {
        return namespace != null && !namespace.isEmpty() ? value
                + "[" + namespace + "]" : value;
    }
}
