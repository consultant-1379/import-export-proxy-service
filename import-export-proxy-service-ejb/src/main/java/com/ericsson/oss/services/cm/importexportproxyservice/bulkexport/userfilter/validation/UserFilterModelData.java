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

import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.*;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.cm.export.transformer.api.IncludeExcludeMoFilteringType;
import com.ericsson.oss.services.cm.export.transformer.api.MoSpecification;

/**
 * Class which contains all relevant Model data for a given user defined filter.
 */
public class UserFilterModelData {

    private final List<Map<String, Object>> moSpecifications;
    private final boolean moClassesCaseCorrectedInFilter;

    public UserFilterModelData(final List<Map<String, Object>> moSpecifications, final boolean moClassesCaseCorrectedInFilter) {
        this.moSpecifications = moSpecifications;
        this.moClassesCaseCorrectedInFilter = moClassesCaseCorrectedInFilter;
    }

    public List<Map<String, Object>> getMoSpecifications() {
        return moSpecifications;
    }

    public boolean isMoClassesCaseCorrectedInFilter() {
        return moClassesCaseCorrectedInFilter;
    }

    public String getMoSpecificationsAsString() {
        return MoSpecificationListFormatter.formatMoSpecifications(moSpecifications);
    }

    private static final class MoSpecificationListFormatter {
        private MoSpecificationListFormatter() {
        }

        static String formatMoSpecifications(final List<Map<String, Object>> moSpecifications) {
            final StringBuilder filterContentBuilder = new StringBuilder();
            boolean multipleMoClasses = false;
            for (final Map<String, Object> moSpecification : moSpecifications) {
                if (multipleMoClasses) {
                    filterContentBuilder.append(";");
                }
                final String includeExcludeFilteringType = (String) moSpecification.get(INCLUDE_EXCLUDE_MO_FILTERING_TYPE);
                if (isExcludeFilter(includeExcludeFilteringType)) {
                    filterContentBuilder.append("!");
                }
                final String moClass = (String) moSpecification.get(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME);
                filterContentBuilder.append(moClass);
                if (isSubtreeFilter(includeExcludeFilteringType)) {
                    filterContentBuilder.append("+");
                }
                appendAttributes(filterContentBuilder, moSpecification);
                multipleMoClasses = true;
            }
            return filterContentBuilder.toString();
        }

        private static boolean isSubtreeFilter(final String includeExcludeFilteringType) {
            return IncludeExcludeMoFilteringType.EXCLUDE_DESCENDANT.name().equals(includeExcludeFilteringType)
                    || IncludeExcludeMoFilteringType.INCLUDE_DESCENDANT.name().equals(includeExcludeFilteringType);
        }

        private static boolean isExcludeFilter(final String includeExcludeFilteringType) {
            return IncludeExcludeMoFilteringType.EXCLUDE.name().equals(includeExcludeFilteringType)
                    || IncludeExcludeMoFilteringType.EXCLUDE_DESCENDANT.name().equals(includeExcludeFilteringType);
        }

        private static void appendAttributes(final StringBuilder filterContentBuilder, final Map<String, Object> moSpecification) {
            final boolean allPersistedAttributes = isAllPersistedAttributes(moSpecification);
            final List<Map<String, Object>> attributes = (List<Map<String, Object>>) moSpecification.get(MEMBER_ATTRIBUTE_SPECIFICATIONS_LIST);
            if (allPersistedAttributes && !attributes.isEmpty()) {
                filterContentBuilder.append(".(*,");
                appendMultipleAttributes(filterContentBuilder, attributes);
                filterContentBuilder.append(")");
            } else if (allPersistedAttributes) {
                filterContentBuilder.append(".*");
            } else if (attributes.size() == 1) {
                appendSingleAttribute(filterContentBuilder, attributes);
            } else if (attributes.size() > 1) {
                filterContentBuilder.append(".(");
                appendMultipleAttributes(filterContentBuilder, attributes);
                filterContentBuilder.append(")");
            }
        }

        private static boolean isAllPersistedAttributes(final Map<String, Object> moSpecification) {
            final String includeExcludeFilteringType = (String) moSpecification.get(INCLUDE_EXCLUDE_MO_FILTERING_TYPE);
            return MoSpecification.AutoAttributeList.PERSISTED_ATTRIBUTES.name().equals(moSpecification.get(MEMBER_MO_ATTRIBUTE_SELECTOR))
                    || IncludeExcludeMoFilteringType.INCLUDE_PERSISTENT_ATTRIBUTES.name().equals(includeExcludeFilteringType);
        }

        private static void appendSingleAttribute(final StringBuilder filterContentBuilder, final List<Map<String, Object>> attributes) {
            final String attributeName = (String) attributes.get(0).get(MEMBER_MO_ATTRIBUTE_NAME);
            filterContentBuilder.append(".").append(attributeName);
        }

        private static void appendMultipleAttributes(final StringBuilder filterContentBuilder, final List<Map<String, Object>> attributes) {
            boolean firstDone = false;
            for (final Map<String, Object> attribute : attributes) {
                if (firstDone) {
                    filterContentBuilder.append(",");
                }
                final String attributeName = (String) attribute.get(MEMBER_MO_ATTRIBUTE_NAME);
                filterContentBuilder.append(attributeName);
                firstDone = true;
            }
        }
    }
}
