
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

import static com.ericsson.oss.services.cm.bulkimport.api.dto.AdditionalJobInfoDto.additionalJobInfo;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjobadditionalinfo.GetAdditionalJobInfoResponse.getAdditionalJobInfoResponse;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * Handler class to fetch additional import job information.
 */
@Handle(GetAdditionalJobInfoRequest.class)
public class GetAdditionalJobInfoRequestHandler implements RequestHandler<GetAdditionalJobInfoRequest, GetAdditionalJobInfoResponse> {

    @Inject
    @Any
    Instance<RequestValidator<GetAdditionalJobInfoRequest>> validators;

    @Inject
    private ImportOperationPersistenceService operationPersistence;

    @Override
    public GetAdditionalJobInfoResponse handle(final GetAdditionalJobInfoRequest request) {
        final Long obsoleteOperationCount = operationPersistence.getObsoleteOperationCount(request.getJobId());
        return getAdditionalJobInfoResponse(additionalJobInfo().withObsoleteOperationCount(obsoleteOperationCount).build());
    }

    @Override
    public Iterable<RequestValidator<GetAdditionalJobInfoRequest>> validators() {
        return validators;
    }

}
