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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.userfilter.validation;

import java.util.Collection;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;

/**
 * Class which handles interaction with Model Service.
 */
public class UserFilterModelServiceHandler {

    @Inject
    private ModelService modelService;

    @Inject
    private Logger logger;

    /**
     * Fetches all model info for a provided MO type.
     *
     * @param moClassName
     *            MO type to fetch model info for.
     * @return a Collection of model info for MO type.
     */
    public Collection<ModelInfo> getAllModelInfosForMoClassName(final String moClassName) {
        final long start = System.currentTimeMillis();
        final String modelUrn = String.format("/%s/%s/%s/%s", SchemaConstants.DPS_PRIMARYTYPE, ModelMetaInformation.ANY, moClassName,
                ModelMetaInformation.ANY);
        final Collection<ModelInfo> allModelInfos = modelService.getModelMetaInformation().getModelsFromUrnIgnoreCase(modelUrn);
        logger.debug("Time to get model info for MO [{}] was [{}] ms", moClassName, System.currentTimeMillis() - start);
        return allModelInfos;
    }

}
