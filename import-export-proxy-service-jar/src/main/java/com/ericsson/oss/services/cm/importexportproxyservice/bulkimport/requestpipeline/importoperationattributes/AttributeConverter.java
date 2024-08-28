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

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;

/**
 * Abstract converter class for list of attributes.
 */
abstract class AttributeConverter {

    public List<ImportOperationAttributeDto> convert(final List attributes) {
        return convert(attributes, null);
    }

    public List<ImportOperationAttributeDto> convert(final List attributes, final PrimaryTypeSpecification primaryTypeSpecification) {
        final Map<String, ImportOperationAttributeDto> importOperationAttributeDtos = new HashMap<>();
        if (attributes != null) {
            for (final Object attribute : attributes) {
                addAttribute(importOperationAttributeDtos, attribute, primaryTypeSpecification);
            }
        }
        return newArrayList(importOperationAttributeDtos.values());
    }

    protected abstract void addAttribute(final Map<String, ImportOperationAttributeDto> importOperationAttributeDtos, final Object attribute,
            final PrimaryTypeSpecification primaryTypeSpecification);

}
