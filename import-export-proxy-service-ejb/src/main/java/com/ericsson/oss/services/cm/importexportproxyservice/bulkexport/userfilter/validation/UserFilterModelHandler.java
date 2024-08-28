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

import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.ATTR_EXPORT_FILTER_MODEL_CLASS_NAME;

import java.util.*;
import javax.inject.Inject;

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.userfilter.UserFilterUtilitiesBean;


/**
 * Class which builds user defined filter model data using Model Service.
 */
public class UserFilterModelHandler {

    @Inject
    private UserFilterModelServiceHandler modelServiceHandler;

    @Inject
    private UserFilterUtilitiesBean userFilterUtilitiesBean;

    /**
     * Fetches the model data for the provided user-defined filter Map.
     *
     * @param moSpecifications
     *            List of MO specifications representing parsed content of user-defined filter.
     * @return Model data for the user-defined filter.
     */
    public UserFilterModelData getUserFilterModelData(final List<Map<String, Object>> moSpecifications) {
        boolean moClassesCaseCorrectedInFilter = false;
        final List<Map<String, Object>> updatedMoSpecifications = new ArrayList<>();
        if (moSpecifications != null) {
            for (final Map<String, Object> moSpecification : moSpecifications) {
                final String moClassName = (String) moSpecification.get(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME);
                final Collection<ModelInfo> allModelInfo = modelServiceHandler.getAllModelInfosForMoClassName(moClassName);

                final Set<String> matchingMoClasses = getMatchingMoClassesFromModelInfo(allModelInfo);
                if (isCaseInsensitiveMoClassFound(moClassName, matchingMoClasses)) {
                    updatedMoSpecifications.addAll(copyMoSpecifications(matchingMoClasses, moSpecification));
                    moClassesCaseCorrectedInFilter = true;
                } else {
                    updatedMoSpecifications.add(moSpecification);
                }
            }
        }
        return new UserFilterModelData(updatedMoSpecifications, moClassesCaseCorrectedInFilter);
    }

    private Set<String> getMatchingMoClassesFromModelInfo(final Collection<ModelInfo> allModelInfo) {
        final Set<String> matchingMoClassNames = new HashSet<>();
        for (final ModelInfo modelInfo : allModelInfo) {
            matchingMoClassNames.add(modelInfo.getName());
        }
        return matchingMoClassNames;
    }

    private boolean isCaseInsensitiveMoClassFound(final String moClassName, final Set<String> matchingMoClasses) {
        return isMoValid(matchingMoClasses) && (isMoreThanOneMoClassFoundInModel(matchingMoClasses)
                || !isMoClassInFilter(matchingMoClasses, moClassName));
    }

    private static boolean isMoValid(final Set<String> matchingMoClasses) {
        return !matchingMoClasses.isEmpty();
    }

    private static boolean isMoreThanOneMoClassFoundInModel(final Set<String> matchingMoClasses) {
        return matchingMoClasses.size() > 1;
    }

    private static boolean isMoClassInFilter(final Set<String> matchingMoClasses, final String moClassName) {
        return matchingMoClasses.contains(moClassName);
    }

    private List<Map<String, Object>> copyMoSpecifications(final Set<String> matchingMoClasses, final Map<String, Object> moSpecification) {
        final List<Map<String, Object>> correctedMoSpecifications = new ArrayList<>();
        for (final String moClass : matchingMoClasses) {
            final Map<String, Object> copiedMoSpecification = userFilterUtilitiesBean.copyMoSpecificationMap(moSpecification);
            copiedMoSpecification.put(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME, moClass);
            correctedMoSpecifications.add(copiedMoSpecification);
        }
        return correctedMoSpecifications;
    }
}
