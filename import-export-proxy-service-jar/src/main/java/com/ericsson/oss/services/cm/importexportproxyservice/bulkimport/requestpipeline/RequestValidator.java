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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline;

import java.util.Collections;
import java.util.List;

/**
 * Request validation interface.
 *
 * @param <R> request
 */
public interface RequestValidator<R extends Request<?>> {
    List NO_ERRORS = Collections.EMPTY_LIST;

    List<ResponseMessage> validate(R request);
}
