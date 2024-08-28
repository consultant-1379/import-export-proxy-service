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

import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;

/**
 * Service to get current values from DPS.
 */
public interface CurrentAttributeService {

    /**
     * Gets current attribute values from DPS.
     *
     * @param importOperation
     *            import operation.
     * @param isNonPersistentAttrRequired
     *            param to determine need for non-persistent attribute retrieval.
     * @return map of attributes if MO exists. Null if MO does not exist.
     */
    Map<String, Object> getCurrentAttributeValues(final ImportOperation importOperation, final boolean isNonPersistentAttrRequired);
}
