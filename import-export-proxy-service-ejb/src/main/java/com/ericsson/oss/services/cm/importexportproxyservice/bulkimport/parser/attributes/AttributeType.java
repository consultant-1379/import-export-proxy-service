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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.parser.attributes;

import java.util.regex.Pattern;

/**
 * Enum class used to identify different attribute types for parsing process.
 */
@SuppressWarnings("squid:S1134")
public enum AttributeType {
    /**
     * Option namespace attribute.
     */
    OPTIONAL_NAMESPACE(ReaderConstants.READER_OPTIONAL_NAMESPACE_KEY),
    /**
     * Option version attribute.
     */
    OPTIONAL_VERSION(ReaderConstants.READER_OPTIONAL_VERSION_KEY),
    /**
     * Simple attribute.
     */
    BASIC(ReaderConstants.READER_BASIC_STRING_VALUE),
    /**
     * Map attribute.
     */
    MAP(ReaderConstants.READER_MAP_FORMAT),
    /**
     * List attribute.
     */
    LIST(ReaderConstants.READER_LIST_FORMAT),
    /**
     * List of map attribute.
     */
    LIST_MAP(ReaderConstants.READER_LIST_MAP_FORMAT),
    /**
     * Invalid format attribute.
     */
    //TODO must be the last, since it matches any character set
    //FIXME use an empty string as format
    NONE(ReaderConstants.READER_NONE_FORMAT);

    private final String formatter;

    AttributeType(final String frm) {
        this.formatter = frm;
    }

    /**
     * Returns type of attribute value.
     * @param input
     *            Attribute value
     * @return Corresponding attribute type
     */
    public static AttributeType getTypeFromString(final String input) {
        if (!"".equals(input) && null != input) {
            for (final AttributeType curr : values()) {
                if (Pattern.matches(curr.formatter, input)) {
                    return curr;
                }
            }
            return AttributeType.NONE;
        }
        return AttributeType.BASIC;
    }

    public String getFormatter() {
        return formatter;
    }
}
