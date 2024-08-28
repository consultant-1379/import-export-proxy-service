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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.userfilter;

import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.*;
import static com.ericsson.oss.services.cm.export.transformer.api.IncludeExcludeMoFilteringType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.ericsson.oss.services.cm.export.transformer.api.IncludeExcludeMoFilteringType;
import com.ericsson.oss.services.cm.export.transformer.api.MoSpecification.AutoAttributeList;

/**
 * Container for all individual Managed Object filter specifications in a User Defined Filter.
 */
public class MoSpecificationContainer {
    private static final String ATTRIBUTE_SEPARATOR = ".";
    private static final String ATTRIBUTE_SEPARATOR_PATTERN = "\\" + ATTRIBUTE_SEPARATOR;
    private static final String COMMA = ",";
    private static final String EMPTY_STRING = "";
    private static final String EXCLUDE_TOKEN = "!";
    private static final String MO_CLASS_SEPARATOR = ";";
    private static final String STAR_TOKEN = "*";
    private static final String SUBTREE_TOKEN = "+";
    private static final String SUBTREE_TOKEN_PATTERN = "\\" + SUBTREE_TOKEN;
    private static final String MANDATORY_ATTRIBUTES_TAG_TOKEN = "<m>";
    private static final String WRITABLE_ATTRIBUTES_TAG_TOKEN = "<w>";

    private static final Pattern PERSISTENT_SPECIFIERS_IN_ATTR_LIST_PATTERN = Pattern.compile("(.*)\\*,(.*)|(.*),\\*(.*)|(.*)\\(\\*\\)(.*)"
            + "|(.*)<[mw]>,(.*)|(.*),<[mw]>(.*)|(.*)<[mw]>(.*)|(.*)//(<[mw]>//)(.*)");

    private final Map<String, List<Map<String, Object>>> mapOfMoSpecifications = new HashMap<>();

    public MoSpecificationContainer(final String userFilterFileContent) {
        generateMoSpecificationsList(userFilterFileContent);
    }

    public List<Map<String, Object>> getMoSpecificationsForMoType(final String moClass) {
        return mapOfMoSpecifications.get(moClass);
    }

    public List<Map<String, Object>> getAllMoSpecifications() {
        final List<Map<String, Object>> listOfMoSpecificationMap = new ArrayList<>();
        for (final List<Map<String, Object>> userDefinedFilterClauses : mapOfMoSpecifications.values()) {
            listOfMoSpecificationMap.addAll(userDefinedFilterClauses);
        }
        return listOfMoSpecificationMap;
    }

    private void generateMoSpecificationsList(final String userFilterFileContent) {
        buildMapOfMoSpecifications(userFilterFileContent);
        rebuildMapOfFilterClausesToFindOnesForMoSpecifications();
    }

    private void buildMapOfMoSpecifications(final String userFilterFileContent) {
        final String[] filterClauses = userFilterFileContent.split(MO_CLASS_SEPARATOR);
        for (final String filterClause : filterClauses) {
            final Map<String, Object> moSpecificationMap = generateMoSpecification(filterClause.trim());
            List<Map<String, Object>> listOfMoSpecificationMap = new ArrayList<>();
            if (!mapOfMoSpecifications.containsKey(moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME))) {
                listOfMoSpecificationMap.add(moSpecificationMap);
            } else {
                listOfMoSpecificationMap = mapOfMoSpecifications.get(moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME));
                listOfMoSpecificationMap.add(moSpecificationMap);
            }
            mapOfMoSpecifications.put(moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME).toString(), listOfMoSpecificationMap);
        }
    }

    private Map<String, Object> generateMoSpecification(final String filterClause) {
        final Map<String, Object> moSpecificationMap = new HashMap<>();
        moSpecificationMap.put(ATTR_EXPORT_FILTER_MODEL_NAME, null);
        moSpecificationMap.put(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME, getMoType(filterClause));
        moSpecificationMap.put(ATTR_EXPORT_FILTER_NAME_SPACE, null);
        moSpecificationMap.put(ATTR_EXPORT_FILTER_MODEL_VERSION, null);
        moSpecificationMap.put(INCLUDE_EXCLUDE_MO_FILTERING_TYPE, retrieveIncludeExcludeBehaviourFromFilter(filterClause));
        moSpecificationMap.put(MEMBER_MO_ATTRIBUTE_SELECTOR, retrieveAutoAttributeListType(filterClause));
        moSpecificationMap.put(MEMBER_ATTRIBUTE_SPECIFICATIONS_LIST, generateMoAttributeSpecifications(filterClause));
        return moSpecificationMap;
    }

    private String getMoType(final String filterClause) {
        final String filterClauseWithoutExclude = filterClause.replaceAll(EXCLUDE_TOKEN, "");
        final String filterClauseWithoutExcludeAndSubtree = filterClauseWithoutExclude.replaceAll(SUBTREE_TOKEN_PATTERN, "");
        return getMoClause(filterClauseWithoutExcludeAndSubtree);
    }

    private static String retrieveIncludeExcludeBehaviourFromFilter(final String filterSpecification) {
        final String moClause = getMoClause(filterSpecification);
        IncludeExcludeMoFilteringType includeExcludeFilteringType = filterSpecification.startsWith(EXCLUDE_TOKEN) ? EXCLUDE : NONE;
        final Boolean isSubtreeFiltering = moClause.endsWith(SUBTREE_TOKEN);
        if (isSubtreeFiltering) {
            includeExcludeFilteringType = getDescendentTypeWithSubtree(includeExcludeFilteringType);
        } else if (isPersistentSpecifiersPresentInAttributeList(filterSpecification)) {
            includeExcludeFilteringType = INCLUDE_PERSISTENT_ATTRIBUTES;
        }
        return includeExcludeFilteringType.name();
    }

    private static IncludeExcludeMoFilteringType getDescendentTypeWithSubtree(final IncludeExcludeMoFilteringType includeExcludeFilteringType) {
        return includeExcludeFilteringType.equals(EXCLUDE) ? EXCLUDE_DESCENDANT : INCLUDE_DESCENDANT;
    }

    private String retrieveAutoAttributeListType(final String filterSpecification) {
        final String attributeFilter = getAttributeClause(filterSpecification);
        if (isStarSpecifiedAlone(attributeFilter)) {
            return AutoAttributeList.PERSISTED_ATTRIBUTES.name();
        } else if (isMandatoryAttributesTag(attributeFilter)) {
            return AutoAttributeList.MANDATORY_PERSISTED_ATTRIBUTES.name();
        } else if (isWritableAttributesTag(attributeFilter)) {
            return AutoAttributeList.WRITABLE_PERSISTED_ATTRIBUTES.name();
        } else {
            return AutoAttributeList.ALL_ATTRIBUTES.name();
        }
    }

    private static boolean isStarSpecifiedAlone(final String attributeFilter) {
        return attributeFilter.trim().endsWith(STAR_TOKEN) || attributeFilter.trim().endsWith("*)");
    }

    private List<Map<String, Object>> generateMoAttributeSpecifications(final String filterSpecification) {
        final List<Map<String, Object>> moAttributeSpecifications = new ArrayList<>();
        if (filterSpecification.contains(ATTRIBUTE_SEPARATOR)) {
            final String attributeSpec = filterSpecification.split(ATTRIBUTE_SEPARATOR_PATTERN)[1]
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "")
                    .replaceAll("\\s+", "");
            final String[] attrList = attributeSpec.split(COMMA);
            for (final String attr : attrList) {
                if (!isSpecialToken(attr)) {
                    final Map<String, Object> moAttributeSpecificationMap = new HashMap<>();
                    moAttributeSpecificationMap.put(MEMBER_MO_ATTRIBUTE_NAME, attr);
                    moAttributeSpecificationMap.put(MEMBER_MO_ATTRIBUTE_TYPE, "String");
                    moAttributeSpecifications.add(moAttributeSpecificationMap);
                }
            }
        }
        return moAttributeSpecifications;
    }

    private boolean isSpecialToken(final String attr) {
        return STAR_TOKEN.equals(attr) || MANDATORY_ATTRIBUTES_TAG_TOKEN.equals(attr)
                || WRITABLE_ATTRIBUTES_TAG_TOKEN.equals(attr);
    }

    private void rebuildMapOfFilterClausesToFindOnesForMoSpecifications() {
        for (final String moClass : mapOfMoSpecifications.keySet()) {
            mapOfMoSpecifications.put(moClass, getListOfClausesRequiredForBuildingMoSpecifications(moClass));
        }
    }

    private List<Map<String, Object>> getListOfClausesRequiredForBuildingMoSpecifications(final String moClass) {
        final List<Map<String, Object>> listOfMoSpecificationMap = mapOfMoSpecifications.get(moClass);
        final List<Map<String, Object>> listOfMoSpecMapToReturn = new ArrayList<>();
        Map<String, Object> moSpecificationWithLastNormalClause = null;
        Map<String, Object> moSpecificationWithLastSubtreeClause = null;
        for (final Map<String, Object> moSpecification : listOfMoSpecificationMap) {
            if (isUserDefinedFilterClauseAnExcludeSubTreeClause(moSpecification)
                    || isUserDefinedFilterClauseAnIncludeSubTreeClause(moSpecification)) {
                moSpecificationWithLastSubtreeClause = moSpecification;
            } else {
                moSpecificationWithLastNormalClause = moSpecification;
            }
        }
        if (moSpecificationWithLastSubtreeClause != null) {
            listOfMoSpecMapToReturn.add(moSpecificationWithLastSubtreeClause);
            if (isUserDefinedFilterClauseAnExcludeSubTreeClause(moSpecificationWithLastSubtreeClause)) {
                return listOfMoSpecMapToReturn;
            }
        }
        if (moSpecificationWithLastNormalClause != null) {
            listOfMoSpecMapToReturn.add(moSpecificationWithLastNormalClause);
        }
        return listOfMoSpecMapToReturn;
    }

    private static boolean isUserDefinedFilterClauseAnExcludeSubTreeClause(final Map<String, Object> moSpecificationMap) {
        return EXCLUDE_DESCENDANT.name().equals(moSpecificationMap.get(INCLUDE_EXCLUDE_MO_FILTERING_TYPE).toString());
    }

    private static boolean isUserDefinedFilterClauseAnIncludeSubTreeClause(final Map<String, Object> moSpecificationMap) {
        return INCLUDE_DESCENDANT.name().equals(moSpecificationMap.get(INCLUDE_EXCLUDE_MO_FILTERING_TYPE).toString());
    }

    private static String getMoClause(final String filterSpecification) {
        return filterSpecification.contains(ATTRIBUTE_SEPARATOR) ? filterSpecification.split(ATTRIBUTE_SEPARATOR_PATTERN)[0] : filterSpecification;
    }

    private static String getAttributeClause(final String filterSpecification) {
        return filterSpecification.contains(ATTRIBUTE_SEPARATOR) ? filterSpecification.split(ATTRIBUTE_SEPARATOR_PATTERN)[1] : EMPTY_STRING;
    }

    private boolean isMandatoryAttributesTag(final String attributeFilter) {
        return attributeFilter.trim().contains(MANDATORY_ATTRIBUTES_TAG_TOKEN);
    }

    private boolean isWritableAttributesTag(final String attributeFilter) {
        return attributeFilter.trim().contains(WRITABLE_ATTRIBUTES_TAG_TOKEN);
    }

    private static boolean isPersistentSpecifiersPresentInAttributeList(final String filterSpecification) {
        final String attributeFilter = getAttributeClause(filterSpecification);
        final String attributeFilterWithoutSpace = attributeFilter.replaceAll("\\s+", "");
        return PERSISTENT_SPECIFIERS_IN_ATTR_LIST_PATTERN.matcher(attributeFilterWithoutSpace).matches();
    }

}
