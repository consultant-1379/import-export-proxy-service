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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.ejb;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.bulkimport.api.ImportService;
import com.ericsson.oss.services.cm.bulkimport.api.ProxyImportService;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportServiceSpecification;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportValidationSpecification;
import com.ericsson.oss.services.cm.bulkimport.response.BulkImportServiceResponse;
import com.ericsson.oss.services.cm.bulkimport.response.ImportServiceValidationResponse;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util.FormatLogger;

/**
 * Implementation class for {@code ImportService}. This class provides the implementation for
 * {@link ImportService#bulkImport(ImportServiceSpecification)}.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class ImportServiceBean implements ImportService {
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(ImportServiceBean.class));
    private static final String METHOD_NAME = "Method name: {}";

    @EServiceRef
    ProxyImportService importService;

    @Override
    public BulkImportServiceResponse bulkImport(final ImportServiceSpecification importServiceSpecification) {
        LOGGER.info("Performing a bulk import. " + METHOD_NAME, "bulkImport");
        return importService.bulkImport(importServiceSpecification);
    }

    @Override
    public ImportServiceValidationResponse validate(final ImportValidationSpecification validationSpecification) {
        LOGGER.info("Performing a validate. " + METHOD_NAME, "validate");
        return importService.validate(validationSpecification);
    }

}
