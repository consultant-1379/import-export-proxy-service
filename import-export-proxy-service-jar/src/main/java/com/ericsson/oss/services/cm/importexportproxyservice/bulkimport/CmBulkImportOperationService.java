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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport;

import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation.GetAllImportOperationsRequest.getAllImportOperationsRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationAttributesRequest.getAllImportOperationAttributesRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationCurrentAttributesRequest.getAllImportOperationCurrentAttributesRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationPersistentCurrentAttributesRequest.getOperationPersistentCurrentAttributesRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationfailures.GetAllImportOperationFailuresRequest.getAllImportOperationsRequest;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationwarnings.GetAllImportOperationWarningsRequest.getAllImportOperationWarningsRequest;

import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationAttributeDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationFailureDto;
import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportOperationWarningDto;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.CollectionResponse;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestPipeline;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Response;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation.GetAllImportOperationsRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperation.GetImportOperationRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationAttributesRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationCurrentAttributesRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationattributes.GetAllImportOperationPersistentCurrentAttributesRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationfailures.GetAllImportOperationFailuresRequest;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importoperationwarnings.GetAllImportOperationWarningsRequest;

/**
 * CmBulkImportOperationService class to preform requests.
 */
public class CmBulkImportOperationService {
    @Inject
    RequestPipeline requestPipeline;

    public Response<ImportOperationDto> getOperation(final Long operationId) {
        final GetImportOperationRequest request = GetImportOperationRequest.getImportOperationRequest(operationId);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportOperationDto> getAllOperations(final Criteria<ImportOperationDto> criteria) {
        final GetAllImportOperationsRequest request = getAllImportOperationsRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportOperationAttributeDto> getAllOperationAttributes(final Criteria<ImportOperationAttributeDto> criteria) {
        final GetAllImportOperationAttributesRequest request = getAllImportOperationAttributesRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportOperationAttributeDto> getAllOperationCurrentAttributes(final Criteria<ImportOperationAttributeDto> criteria) {
        final GetAllImportOperationCurrentAttributesRequest request = getAllImportOperationCurrentAttributesRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportOperationAttributeDto>
            getAllOperationPersistentCurrentAttributes(final Criteria<ImportOperationAttributeDto> criteria) {
        final GetAllImportOperationPersistentCurrentAttributesRequest request = getOperationPersistentCurrentAttributesRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportOperationFailureDto> getAllOperationFailures(final Criteria<ImportOperationFailureDto> criteria) {
        final GetAllImportOperationFailuresRequest request = getAllImportOperationsRequest(criteria);
        return requestPipeline.process(request);
    }

    public CollectionResponse<ImportOperationWarningDto> getAllOperationWarnings(final Criteria<ImportOperationWarningDto> criteria) {
        final GetAllImportOperationWarningsRequest request = getAllImportOperationWarningsRequest(criteria);
        return requestPipeline.process(request);
    }
}
