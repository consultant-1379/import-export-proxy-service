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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * Response for create Dps Group with groupName.
 */
public class CreateDpsGroupResponse extends Response<String> {

    CreateDpsGroupResponse(final String groupName) {
        super(groupName);
    }

    public static CreateDpsGroupResponse createDpsGroupResponse(final String groupName) {
        return new CreateDpsGroupResponse(groupName);
    }
}
