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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.cm.export.api.ExportParameters;
import com.ericsson.oss.services.cm.export.transformer.api.MoAttributeSpecification;
import com.ericsson.oss.services.cm.export.transformer.api.MoSpecification;
import com.ericsson.oss.services.cm.export.transformer.api.TransformerUserFilter;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.log.ExportServiceLog;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.validation.ValidationExportService;

/**
 * Stateless EJB to handle utilities for user defined filters.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class UserFilterUtilitiesBean {
    private static final String EXPORT_RESOURCE = UserFilterUtilitiesBean.class.getSimpleName();

    @Inject
    private ValidationExportService validationExportService;

    @Inject
    private ExportServiceLog serviceLog;

    @Inject
    private Logger logger;

    /**
     * Validate user filter content.
     *
     * @param exportParameters
     *            export parameters which contains user supplied filter
     * @return user filter content as string
     */
    public String validateUserFilter(final ExportParameters exportParameters) {
        final String userFilterFileContent =
                validationExportService.validateUserFilter(exportParameters).replaceAll("\\r\\n|\\r|\\n", "").replaceAll("[\\s;]+$", "");
        if (!userFilterFileContent.equals(EMPTY_FILE)) {
            validationExportService.validateUserFilterFileContent(userFilterFileContent);
        }
        return userFilterFileContent;
    }

    /**
     * Parses user filter string into list of MoSpecification maps.
     *
     * @param userFilterFileContent
     *            user filter content as string
     * @return user filter map
     */
    public Map<String, Object> parseUserFilterFileContent(final String userFilterFileContent) {
        final Map<String, Object> userFilterMap = new HashMap<>();
        if (!userFilterFileContent.equals(EMPTY_FILE)) {
            final long parseStartTime = System.currentTimeMillis();
            final MoSpecificationContainer filterSpecificationsContainer = new MoSpecificationContainer(userFilterFileContent);
            final List<Map<String, Object>> moSpecificationMapList = filterSpecificationsContainer.getAllMoSpecifications();
            final long parseEndTime = System.currentTimeMillis();
            logFilterFileParseSummary(parseStartTime, parseEndTime);
            userFilterMap.put(MEMBER_MO_SPECIFICATIONS_LIST, moSpecificationMapList);
        }
        return userFilterMap;
    }

    /**
     * Generates TransformerUserFilter from User Defined Filter Po.
     *
     * @param userFilterMap
     *            user filter Po.
     * @return transformer DTO representing user defined filter contents
     */
    public TransformerUserFilter generateTransformerUserFilterFromUserFilterMap(final Map<String, Object> userFilterMap) {
        final TransformerUserFilter transformerUserFilter = new TransformerUserFilter();
        if (userFilterMap.isEmpty()) {
            return transformerUserFilter;
        }
        final List<Map<String, Object>> moSpecificationMapList = (List<Map<String, Object>>) userFilterMap.get(MEMBER_MO_SPECIFICATIONS_LIST);
        for (final Map<String, Object> moSpecificationMap : moSpecificationMapList) {
            final MoSpecification moSpecification = generateMoSpecificationFromMoSpecificationMap(moSpecificationMap);
            transformerUserFilter.addMoSpecification(moSpecification);
        }
        logger.debug("Generated user filter for Transformer from versant Po: {}", transformerUserFilter);
        return transformerUserFilter;
    }

    private MoSpecification generateMoSpecificationFromMoSpecificationMap(final Map<String, Object> moSpecificationMap) {
        final MoSpecification moSpecification = new MoSpecification();
        moSpecification.setMoName((String) moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_NAME));
        moSpecification.setType((String) moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME));
        moSpecification.setNamespace((String) moSpecificationMap.get(ATTR_EXPORT_FILTER_NAME_SPACE));
        moSpecification.setNamespaceVersion((String) moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_VERSION));
        moSpecification.setAutoAttributeList((String) moSpecificationMap.get(MEMBER_MO_ATTRIBUTE_SELECTOR));
        moSpecification.setIncludeExcludeMoFilteringType((String) moSpecificationMap.get(INCLUDE_EXCLUDE_MO_FILTERING_TYPE));

        final List<Map<String, Object>> moAttributeSpecificationMapList =
                (List<Map<String, Object>>) moSpecificationMap.get(MEMBER_ATTRIBUTE_SPECIFICATIONS_LIST);

        for (final Map<String, Object> moAttributeSpecificationMap : moAttributeSpecificationMapList) {
            final MoAttributeSpecification moAttributeSpecification =
                    generateAttributeSpecificationFromAttributeSpecificationMap(moAttributeSpecificationMap);
            moSpecification.getMoAttributeSpecifications().add(moAttributeSpecification);
        }

        return moSpecification;
    }

    private MoAttributeSpecification
            generateAttributeSpecificationFromAttributeSpecificationMap(final Map<String, Object> moAttributeSpecificationMap) {
        final MoAttributeSpecification moAttributeSpecification = new MoAttributeSpecification();
        moAttributeSpecification.setName((String) moAttributeSpecificationMap.get(MEMBER_MO_ATTRIBUTE_NAME));
        moAttributeSpecification.setType((String) moAttributeSpecificationMap.get(MEMBER_MO_ATTRIBUTE_TYPE));
        return moAttributeSpecification;
    }

    /**
     * Copies one user filter Po to another.
     *
     * @param userFilterMap
     *            filter Po to be copied.
     * @return copied user filter Po
     */
    public Map<String, Object> copyUserFilterPo(final Map<String, Object> userFilterMap) {
        final Map<String, Object> copiedUserFilterMap = new HashMap<>();
        if (userFilterMap.isEmpty()) {
            return copiedUserFilterMap;
        }
        logger.info("Copying master export job Po for slave job...");

        final List<Map<String, Object>> moSpecificationMapList = (List<Map<String, Object>>) userFilterMap.get(MEMBER_MO_SPECIFICATIONS_LIST);
        final List<Map<String, Object>> copiedMoSpecificationMapList = new ArrayList<>();

        for (final Map<String, Object> moSpecificationMap : moSpecificationMapList) {
            final Map<String, Object> copiedMoSpecificationMap = copyMoSpecificationMap(moSpecificationMap);
            copiedMoSpecificationMapList.add(copiedMoSpecificationMap);
        }

        copiedUserFilterMap.put(MEMBER_MO_SPECIFICATIONS_LIST, copiedMoSpecificationMapList);
        logger.info("Copied master export job Po for slave job...");

        return copiedUserFilterMap;
    }

    /**
     * Creates a copy of an MoSpecification map.
     *
     * @param moSpecificationMap
     *            MoSpecification map to be copied.
     * @return a copy of the MoSpecification map.
     */
    public Map<String, Object> copyMoSpecificationMap(final Map<String, Object> moSpecificationMap) {
        final Map<String, Object> copiedMoSpecificationMap = new HashMap<>();

        final String moName = (String) moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_NAME);
        final String moType = (String) moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME);
        final String namespace = (String) moSpecificationMap.get(ATTR_EXPORT_FILTER_NAME_SPACE);
        final String version = (String) moSpecificationMap.get(ATTR_EXPORT_FILTER_MODEL_VERSION);
        final String attributeSelector = (String) moSpecificationMap.get(MEMBER_MO_ATTRIBUTE_SELECTOR);
        final String includeExcludeBehaviour = (String) moSpecificationMap.get(INCLUDE_EXCLUDE_MO_FILTERING_TYPE);

        copiedMoSpecificationMap.put(ATTR_EXPORT_FILTER_MODEL_NAME, moName);
        copiedMoSpecificationMap.put(ATTR_EXPORT_FILTER_MODEL_CLASS_NAME, moType);
        copiedMoSpecificationMap.put(ATTR_EXPORT_FILTER_NAME_SPACE, namespace);
        copiedMoSpecificationMap.put(ATTR_EXPORT_FILTER_MODEL_VERSION, version);
        copiedMoSpecificationMap.put(MEMBER_MO_ATTRIBUTE_SELECTOR, attributeSelector);
        copiedMoSpecificationMap.put(INCLUDE_EXCLUDE_MO_FILTERING_TYPE, includeExcludeBehaviour);

        final List<Map<String, Object>> moAttributeSpecificationMapList =
                (List<Map<String, Object>>) moSpecificationMap.get(MEMBER_ATTRIBUTE_SPECIFICATIONS_LIST);
        final List<Map<String, Object>> copiedMoAttributeSpecificationMapList = new ArrayList<>();

        for (final Map<String, Object> moAttributeSpecificationMap : moAttributeSpecificationMapList) {
            final Map<String, Object> copiedMoAttributeSpecificationMap = copyMoAttributeSpecificationMap(moAttributeSpecificationMap);
            copiedMoAttributeSpecificationMapList.add(copiedMoAttributeSpecificationMap);
        }
        copiedMoSpecificationMap.put(MEMBER_ATTRIBUTE_SPECIFICATIONS_LIST, copiedMoAttributeSpecificationMapList);
        return copiedMoSpecificationMap;
    }

    private Map<String, Object> copyMoAttributeSpecificationMap(final Map<String, Object> moAttributeSpecificationMap) {
        final Map<String, Object> copiedAttributeSpecificationMap = new HashMap<>();

        final String attributeName = (String) moAttributeSpecificationMap.get(MEMBER_MO_ATTRIBUTE_NAME);
        final String attributeType = (String) moAttributeSpecificationMap.get(MEMBER_MO_ATTRIBUTE_TYPE);

        copiedAttributeSpecificationMap.put(MEMBER_MO_ATTRIBUTE_NAME, attributeName);
        copiedAttributeSpecificationMap.put(MEMBER_MO_ATTRIBUTE_TYPE, attributeType);

        return copiedAttributeSpecificationMap;
    }

    private void logFilterFileParseSummary(final long parseStartTime, final long parseEndTime) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATETIME_FORMAT_WITH_MILLISEC);
        final Map<String, Object> additionalInfoAttributes = new LinkedHashMap<>();
        additionalInfoAttributes.put("parseStartTime", dateFormat.format(new Date(parseStartTime)));
        additionalInfoAttributes.put("parseEndTime", dateFormat.format(new Date(parseEndTime)));
        additionalInfoAttributes.put("parseDuration", TimeUnit.MILLISECONDS.toSeconds(parseEndTime - parseStartTime));
        serviceLog.logEvent("CMEXPORT.FILTER_FILE_PARSING_COMPLETED", EXPORT_RESOURCE, additionalInfoAttributes);
    }
}
