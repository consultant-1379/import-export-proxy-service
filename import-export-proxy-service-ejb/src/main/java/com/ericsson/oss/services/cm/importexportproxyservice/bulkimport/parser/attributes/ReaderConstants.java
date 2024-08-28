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

/**
 * Constants to define the regex for Attribute name and value.
 */
public final class ReaderConstants {
    /**
     * Pattern for validating optional namespace.
     */
    public static final String READER_OPTIONAL_NAMESPACE_KEY = "[ ]*(--namespace|-ns){1}[ ]*";
    /**
     * Pattern for validating optional version.
     */
    public static final String READER_OPTIONAL_VERSION_KEY = "[ ]*(--version|-v){1}[ ]*";
    /**
     * Pattern for excluding unexpected characters not enclosed with external quotes.
     */
    public static final String READER_BASIC_STRING_EXCLUDED_CHAR = "\\{\\}\\[\\]()=\"', ";
    /**
     * Pattern for excluding unexpected characters enclosed with external quotes.
     */
    public static final String READER_BASIC_STRING_EXCLUDED_QUOTED_CHAR = "\"'";
    /**
     * Pattern for including escaped double quotes.
     */
    public static final String READER_BASIC_STRING_INCLUDED_ESCAPED_QUOTES = "\\\\[\"']";
    /**
     * Pattern for validating left side of an attribute assignment.
     */
    public static final String READER_BASIC_STRING_KEY = "[ ]*-?[a-zA-Z0-9]+[ ]*";
    /**
     * Pattern for validating string attribute values enclosed by quotes.
     */
    public static final String READER_BASIC_QUOTED_STRING_VALUE =
            "[\"']+([^" + READER_BASIC_STRING_EXCLUDED_QUOTED_CHAR + "]|" + READER_BASIC_STRING_INCLUDED_ESCAPED_QUOTES + ")*[\"']+";
    /**
     * Pattern for validating string attribute values not enclosed by quotes.
     */
    public static final String READER_BASIC_NOT_QUOTED_STRING_VALUE =
            "([^" + READER_BASIC_STRING_EXCLUDED_CHAR + ":]|" + READER_BASIC_STRING_INCLUDED_ESCAPED_QUOTES + ")"
                    + "(([^" + READER_BASIC_STRING_EXCLUDED_CHAR + "]|" + READER_BASIC_STRING_INCLUDED_ESCAPED_QUOTES + ")*"
                    + "([^" + READER_BASIC_STRING_EXCLUDED_CHAR + ":]|" + READER_BASIC_STRING_INCLUDED_ESCAPED_QUOTES + "))?";
    /**
     * Pattern for validating right side of an attribute assignment.
     */
    public static final String READER_BASIC_STRING_VALUE = "[ ]*(((" + READER_BASIC_QUOTED_STRING_VALUE + ")|("
            + READER_BASIC_QUOTED_STRING_VALUE + "\\[" + READER_BASIC_QUOTED_STRING_VALUE + "\\]))|" + "(("
            + READER_BASIC_NOT_QUOTED_STRING_VALUE + ")|(" + READER_BASIC_NOT_QUOTED_STRING_VALUE + "\\["
            + READER_BASIC_NOT_QUOTED_STRING_VALUE + "\\])))[ ]*";
    /**
     * Pattern for validating list attribute values.
     */
    public static final String READER_LIST_FORMAT = "[ ]*\\[(((" + READER_BASIC_STRING_VALUE
            + "(\\[" + READER_BASIC_STRING_VALUE
            + "\\]),)|(" + READER_BASIC_STRING_VALUE + ",))*((" + READER_BASIC_STRING_VALUE
            + "\\[" + READER_BASIC_STRING_VALUE + "\\])|(" + READER_BASIC_STRING_VALUE + ")){1})?\\][ ]*";
    /**
     * Auxiliary pattern for managing list values for maps.
     */
    public static final String READER_LIST_FORMAT_TRANSFORMED = "[ ]*\\[((" + READER_BASIC_STRING_VALUE
            + "@)*(" + READER_BASIC_STRING_VALUE + "){1})?\\][ ]*";
    /**
     * Pattern for validating a simple attribute assignment.
     */
    public static final String READER_SIMPLE_ASSIGNMENT_FORMAT = READER_BASIC_STRING_KEY + "=(" + READER_BASIC_STRING_VALUE + "|"
            + READER_LIST_FORMAT + ")";
    /**
     * Pattern for validating map attribute values.
     */
    public static final String READER_MAP_FORMAT = "[ ]*\\{(" + READER_SIMPLE_ASSIGNMENT_FORMAT + ",)*(" + READER_SIMPLE_ASSIGNMENT_FORMAT
            + "){1}\\}[ ]*";
    /**
     * Pattern for validating list of map attribute values.
     */
    public static final String READER_LIST_MAP_FORMAT = "[ ]*\\[((" + READER_MAP_FORMAT + ",)*("
            + READER_MAP_FORMAT + "){1})?\\][ ]*";
    /**
     * Pattern for skipping 'n instance(s)' string from get output message, included in dynamic import files.
     */
    public static final String READER_NUM_INSTANCES_MESSAGE_FORMAT = "[1-9]+[0-9]* instance\\(s\\)";
    /**
     * Pattern for invalid values.
     */
    public static final String READER_NONE_FORMAT = ".";
    /**
     * Character to identify comment lines.
     */
    public static final String COMMENT_MARK = "#";
    /**
     * Empty reference value for dynamic file.
     */
    public static final String EMPTY_REFERENCE_VALUE = "<empty>";

    private ReaderConstants() {}
}
