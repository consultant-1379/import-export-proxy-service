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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobadditionalinfo;

import com.ericsson.oss.services.cm.bulkimport.api.dto.AdditionalJobInfoDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;

/**
 * Class to construct response for additional import job information.
 */
public class GetAdditionalJobInfoResponse extends Response<AdditionalJobInfoDto> {

    GetAdditionalJobInfoResponse(final AdditionalJobInfoDto additionalJobInfoDto) {
        super(additionalJobInfoDto);
    }

    public static GetAdditionalJobInfoResponse getAdditionalJobInfoResponse(final AdditionalJobInfoDto additionalJobInfoDto) {
        return new GetAdditionalJobInfoResponse(additionalJobInfoDto);
    }

}
