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

import static com.ericsson.oss.services.cm.export.api.ExportServiceError.USER_FILTER_CONTENT_INVALID;

import java.text.MessageFormat;

import com.ericsson.oss.services.cm.export.api.UserFilterValidationMessage;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.validation.ValidationException;

/**
 * Class containing accepted pattern for user filters and
 * business logic to check input filter string values.
 */
public final class UserFilterSyntaxValidator {
    /**
     * Separator for user filters.
     */
    private static final String USER_FILTER_SEPARATOR = ";";
    /**
     * Separator between Mo classes and attribute (list).
     */
    private static final String USER_DEFINED_FILTER_ELEMENTS_SEPARATOR = "\\.{1}";
    /**
     * Separator for attributes in attributes list.
     */
    private static final String USER_DEFINED_FILTER_ELEMENTS_LIST_SEPARATOR = "\\,{1}";
    /**
     * Pattern for accepted string (alphanumeric characters, -, $ and _).
     */
    private static final String GENERIC_STRING_PATTERN = "[a-zA-Z0-9\\-_$]+";
    /**
     * Pattern for accepted MO class names (see GENERIC_STRING_PATTERN constant).
     */
    private static final String MO_CLASS_PATTERN = GENERIC_STRING_PATTERN;
    /**
     * Pattern for accepted MO attribute names (see GENERIC_STRING_PATTERN constant).
     */
    private static final String MO_ATTRIBUTE_PATTERN = GENERIC_STRING_PATTERN;
    /**
     * Pattern for accepted MO persistent attributes: *{1}.
     */
    private static final String USER_DEFINED_FILTER_PATTERN_PERSISTENT_ATTRIBUTES = "(\\*{1}|<[mw]>)";
    /**
     * Pattern for accepted MO list elements (see USER_DEFINED_FILTER_PATTERN_PERSISTENT_ATTRIBUTES and GENERIC_STRING_PATTERN constant).
     */
    private static final String MO_ATTRIBUTE_LIST_ELEMENT_PATTERN_WITH_STAR = "(" + USER_DEFINED_FILTER_PATTERN_PERSISTENT_ATTRIBUTES
            + "|" + MO_ATTRIBUTE_PATTERN + ")";
    /**
     * Pattern for accepted MO list elements.
     */
    private static final String MO_ATTRIBUTE_LIST_ELEMENT_PATTERN = "[ ]*" + MO_ATTRIBUTE_LIST_ELEMENT_PATTERN_WITH_STAR + "[ ]*";
    /**
     * Pattern for accepted MO attribute lists: (MO_ATTRIBUTE{1}(,MO_ATTRIBUTE){0..n}).
     */
    private static final String ATTRIBUTE_LIST_PATTERN = "\\("
            + MO_ATTRIBUTE_LIST_ELEMENT_PATTERN + "("
            + USER_DEFINED_FILTER_ELEMENTS_LIST_SEPARATOR
            + MO_ATTRIBUTE_LIST_ELEMENT_PATTERN + ")*\\)";
    /**
     * Pattern for include MO Class clause.
     */
    private static final String INCLUDE_MO_CLASS_CLAUSE_PATTERN = MO_CLASS_PATTERN + USER_DEFINED_FILTER_ELEMENTS_SEPARATOR + "("
            + USER_DEFINED_FILTER_PATTERN_PERSISTENT_ATTRIBUTES + "|" + MO_ATTRIBUTE_PATTERN + "|" + ATTRIBUTE_LIST_PATTERN + ")";
    /**
     * Pattern for exclude MO Class token.
     */
    private static final String EXCLUDE_MO_TOKEN = "\\!";
    /**
     * Pattern for subtree token.
     */
    private static final String SUBTREE_TOKEN = "\\+";
    /**
     * Pattern for exclude MO Class clause.
     */
    private static final String EXCLUDE_MO_CLASS_CLAUSE_PATTERN = EXCLUDE_MO_TOKEN + MO_CLASS_PATTERN;
    /**
     * Pattern for exclude subtree MO Class clause.
     */
    private static final String EXCLUDE_SUBTREE_MO_CLASS_CLAUSE_PATTERN = EXCLUDE_MO_TOKEN + MO_CLASS_PATTERN + SUBTREE_TOKEN;
    /**
     * Pattern for include subtree MO Class clause.
     */
    private static final String INCLUDE_SUBTREE_MO_CLASS_CLAUSE_PATTERN = MO_CLASS_PATTERN + SUBTREE_TOKEN
            + USER_DEFINED_FILTER_ELEMENTS_SEPARATOR + "(" + USER_DEFINED_FILTER_PATTERN_PERSISTENT_ATTRIBUTES + ")";
    /**
     * Pattern for accepted filter: MO_CLASS.(MO_PERSISTENT_ATTRIBUTES | MO_ATTRIBUTE_LIST | MO_ATTRIBUTE_NAME)
     */
    private static final String USER_DEFINED_FILTER_PATTERN = EXCLUDE_SUBTREE_MO_CLASS_CLAUSE_PATTERN + "|" + INCLUDE_SUBTREE_MO_CLASS_CLAUSE_PATTERN
            + "|" + EXCLUDE_MO_CLASS_CLAUSE_PATTERN + "|" + INCLUDE_MO_CLASS_CLAUSE_PATTERN;
    /**
     * The number of criteria expected for an MO Class user filter clause with a single period.
     */
    private static final Integer NUMBER_OF_CRITERIA_FOR_SINGLE_PERIOD_IN_CLAUSE = 2;

    private static final String WHITESPACE = " ";

    /**
     * The root MO Classes which are invalid for include or exclude subtree MO class clause.
     */
    private static final String INVALID_ROOT_MO = "SubNetwork";
    /**
     * Pattern for invalid include subtree with root MO Class clause.
     */
    private static final String INVALID_INCLUDE_ROOT_MO_SUBTREE_PATTERN = INVALID_ROOT_MO + SUBTREE_TOKEN
            + USER_DEFINED_FILTER_ELEMENTS_SEPARATOR + USER_DEFINED_FILTER_PATTERN_PERSISTENT_ATTRIBUTES;
    /**
     * Pattern for invalid exclude subtree with root MO Class clause.
     */
    private static final String INVALID_EXCLUDE_ROOT_MO_SUBTREE_PATTERN = EXCLUDE_MO_TOKEN + INVALID_ROOT_MO + SUBTREE_TOKEN;

    private static final String INVALID_MULTIPLE_SPECIFIERS_IN_ATTRIBUTE_LIST_PATTERN = "((.*)\\((.*)(<[mw]>)(.*)(<[mw]>)(.*)\\)(.*)"
            + "|(.*)\\((.*)(\\*)(.*)(<[mw]>)(.*)\\)(.*)|(.*)\\((.*)(<[mw]>)(.*)(\\*)(.*)\\)(.*))";

    private UserFilterSyntaxValidator() {}

    public static void checkFilterCompliantToExpectedPattern(final String userFilterFileContent) {
        final String[] criteriaRawArray = userFilterFileContent.split(USER_FILTER_SEPARATOR);
        boolean atLeastOneIncludeClause = false;
        boolean atLeastOneExcludeClause = false;
        for (final String criteriaRaw : criteriaRawArray) {
            final String criteriaRawTrimmed = criteriaRaw.trim();
            if (!criteriaRawTrimmed.matches(USER_DEFINED_FILTER_PATTERN)) {
                generateExceptionForInvalidUserFilterClausePattern(criteriaRaw, criteriaRawTrimmed);
            } else if (criteriaRawTrimmed.matches(INVALID_INCLUDE_ROOT_MO_SUBTREE_PATTERN)
                    || criteriaRawTrimmed.matches(INVALID_EXCLUDE_ROOT_MO_SUBTREE_PATTERN)) {
                generateExceptionForUnsupportedRootMoClassWithSubtree(criteriaRawTrimmed);
            } else if (criteriaRawTrimmed.matches(INVALID_MULTIPLE_SPECIFIERS_IN_ATTRIBUTE_LIST_PATTERN)) {
                generateExceptionForMultipleSpecifiersInAttributeList(criteriaRawTrimmed);
            } else {
                if (criteriaRawTrimmed.matches(EXCLUDE_MO_CLASS_CLAUSE_PATTERN)
                        || criteriaRawTrimmed.matches(EXCLUDE_SUBTREE_MO_CLASS_CLAUSE_PATTERN)) {
                    atLeastOneExcludeClause = true;
                } else {
                    atLeastOneIncludeClause = true;
                }
                generateExceptionIfBothIncludeAndExcludeClausePatternsInFile(atLeastOneIncludeClause, atLeastOneExcludeClause, criteriaRawTrimmed);
            }
        }
    }

    private static void generateExceptionForUnsupportedRootMoClassWithSubtree(final String criteriaRawTrimmed) {
        throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                new MessageFormat(UserFilterValidationMessage.UNSUPPORTED_ROOT_MO_CLASS_WITH_SUBTREE_MESSAGE),
                UserFilterValidationMessage.UNSUPPORTED_ROOT_MO_CLASS_WITH_SUBTREE_SOLUTION, criteriaRawTrimmed);
    }

    private static void generateExceptionForInvalidUserFilterClausePattern(final String criteriaRaw, final String criteriaRawTrimmed) {
        final String[] criteriaElements = criteriaRawTrimmed.split(USER_DEFINED_FILTER_ELEMENTS_SEPARATOR);
        if (criteriaElements.length < NUMBER_OF_CRITERIA_FOR_SINGLE_PERIOD_IN_CLAUSE) {
            generateExceptionNoPeriodInUserFilterClausePattern(criteriaRaw, criteriaRawTrimmed, criteriaElements);
        } else if (criteriaElements.length > NUMBER_OF_CRITERIA_FOR_SINGLE_PERIOD_IN_CLAUSE) {
            generateExceptionTooManyPeriodsInUserFilterClausePattern(criteriaRawTrimmed);
        } else {
            generateExceptionOnePeriodWithErrorInUserFilterClausePattern(criteriaRawTrimmed, criteriaElements);
        }
    }

    private static void generateExceptionNoPeriodInUserFilterClausePattern(final String criteriaRaw, final String criteriaRawTrimmed,
            final String[] criteriaElements) {
        if (criteriaRawTrimmed.contains(WHITESPACE)) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.EMPTY_SPACES_USER_FILTER_MESSAGE),
                    UserFilterValidationMessage.EMPTY_SPACES_USER_FILTER_SOLUTION, criteriaRaw);
        }
        // Check if invalid EXCLUDE filter
        checkFilterClassCompliantToExpectedExcludePattern(criteriaRawTrimmed, criteriaElements[0]);

        // Must be INCLUDE filter with no period
        throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                new MessageFormat(UserFilterValidationMessage.NO_PERIOD_USER_FILTER_MESSAGE),
                UserFilterValidationMessage.PERIOD_USER_FILTER_SOLUTION, criteriaRawTrimmed);
    }

    private static void generateExceptionTooManyPeriodsInUserFilterClausePattern(final String criteriaRawTrimmed) {
        throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                new MessageFormat(UserFilterValidationMessage.TOO_MANY_PERIODS_USER_FILTER_MESSAGE),
                UserFilterValidationMessage.PERIOD_USER_FILTER_SOLUTION, criteriaRawTrimmed);
    }

    private static void generateExceptionOnePeriodWithErrorInUserFilterClausePattern(final String criteriaRawTrimmed,
            final String[] criteriaElements) {
        checkFilterClassCompliantToExpectedExcludePattern(criteriaRawTrimmed, criteriaElements[0]);
        checkFilterClassCompliantToExpectedIncludeSubtreePattern(criteriaRawTrimmed, criteriaElements[0]);
        checkFilterClassCompliantToExpectedPattern(criteriaRawTrimmed, criteriaElements[0]);
        checkFilterAttributeCompliantToExpectedPattern(criteriaRawTrimmed, criteriaElements[1]);
    }

    private static void generateExceptionIfBothIncludeAndExcludeClausePatternsInFile(final boolean atLeastOneIncludeClause,
            final boolean atLeastOneExcludeClause, final String criteriaRawTrimmed) {
        if (atLeastOneExcludeClause && atLeastOneIncludeClause) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.INCOMPATIBLE_CLAUSES_USER_FILTER_MESSAGE),
                    UserFilterValidationMessage.INCOMPATIBLE_CLAUSES_USER_FILTER_SOLUTION, criteriaRawTrimmed);
        }
    }

    private static void checkFilterClassCompliantToExpectedPattern(final String criteria, final String criteriaMoClass) {
        if (criteriaMoClass.contains(WHITESPACE)) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.EMPTY_SPACES_USER_FILTER_MESSAGE),
                    UserFilterValidationMessage.EMPTY_SPACES_USER_FILTER_SOLUTION, criteria);
        }
        if (!criteriaMoClass.matches(MO_CLASS_PATTERN)) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.INVALID_MO_CLASS_USER_FILTER_MESSAGE),
                    UserFilterValidationMessage.INVALID_MO_CLASS_ATTRIBUTE_USER_FILTER_SOLUTION, criteria, criteriaMoClass);
        }
    }

    private static void checkFilterAttributeCompliantToExpectedPattern(final String criteria, final String criteriaMoAttribute) {
        if (!criteriaMoAttribute.trim().equals(criteriaMoAttribute)) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.EMPTY_SPACES_USER_FILTER_MESSAGE),
                    UserFilterValidationMessage.EMPTY_SPACES_USER_FILTER_SOLUTION, criteria);
        }
        if (criteriaMoAttribute.startsWith("(") || criteriaMoAttribute.endsWith(")")) {
            if (!(criteriaMoAttribute.startsWith("(") && criteriaMoAttribute.endsWith(")"))) {
                throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                        new MessageFormat(UserFilterValidationMessage.MISSING_BRACKET_USER_FILTER_MESSAGE),
                        UserFilterValidationMessage.MISSING_BRACKET_USER_FILTER_SOLUTION, criteria, criteriaMoAttribute);
            } else {
                for (final String listElement : stripTrailingBrackets(criteriaMoAttribute).split(USER_DEFINED_FILTER_ELEMENTS_LIST_SEPARATOR)) {
                    if (!listElement.matches(MO_ATTRIBUTE_LIST_ELEMENT_PATTERN)) {
                        throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                                new MessageFormat(UserFilterValidationMessage.INVALID_MO_ATTRIBUTE_LIST_ELEMENT_USER_FILTER_MESSAGE),
                                UserFilterValidationMessage.INVALID_MO_CLASS_ATTRIBUTE_USER_FILTER_SOLUTION, criteria, listElement);
                    }
                }
            }
        }
        throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                new MessageFormat(UserFilterValidationMessage.INVALID_MO_ATTRIBUTE_USER_FILTER_MESSAGE),
                UserFilterValidationMessage.INVALID_MO_CLASS_ATTRIBUTE_USER_FILTER_SOLUTION, criteria, criteriaMoAttribute);
    }

    private static void checkFilterClassCompliantToExpectedExcludePattern(final String criteria, final String criteriaMoClass) {
        if (criteriaMoClass.matches(EXCLUDE_MO_CLASS_CLAUSE_PATTERN) || criteriaMoClass.matches(EXCLUDE_SUBTREE_MO_CLASS_CLAUSE_PATTERN)) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.UNSUPPORTED_ATTRIBUTE_FILTER_MESSAGE),
                    UserFilterValidationMessage.UNSUPPORTED_ATTRIBUTE_FILTER_SOLUTION, criteria);
        } else if (criteriaMoClass.startsWith("!")) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.INVALID_MO_CLASS_USER_FILTER_MESSAGE),
                    UserFilterValidationMessage.INVALID_MO_CLASS_USER_FILTER_SOLUTION, criteria, criteriaMoClass.replaceFirst("!", ""));
        }
    }

    private static void checkFilterClassCompliantToExpectedIncludeSubtreePattern(final String criteria, final String criteriaMoClass) {
        if (criteriaMoClass.matches(MO_CLASS_PATTERN + SUBTREE_TOKEN)) {
            throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                    new MessageFormat(UserFilterValidationMessage.UNSUPPORTED_ATTRIBUTE_FILTER_MESSAGE),
                    UserFilterValidationMessage.UNSUPPORTED_ATTRIBUTE_FILTER_SOLUTION, criteria);
        }
    }

    private static String stripTrailingBrackets(final String criteriaMoAttribute) {
        return criteriaMoAttribute.startsWith("(") && criteriaMoAttribute.endsWith(")")
                ? criteriaMoAttribute.substring(1, criteriaMoAttribute.length() - 1)
                : criteriaMoAttribute;
    }

    private static ValidationException createUserFileFilterValidationExceptionWithMessageAndSolution(
            final MessageFormat additionalFormattedInfoMessage, final String additionalInfoSolution, final String... additionalInfoArgs) {
        return new ValidationException(USER_FILTER_CONTENT_INVALID.message(additionalFormattedInfoMessage.format(additionalInfoArgs)),
                USER_FILTER_CONTENT_INVALID.solution(additionalInfoSolution), USER_FILTER_CONTENT_INVALID.code());
    }

    private static void generateExceptionForMultipleSpecifiersInAttributeList(final String criteriaRawTrimmed) {
        throw createUserFileFilterValidationExceptionWithMessageAndSolution(
                new MessageFormat(UserFilterValidationMessage.UNSUPPORTED_DUPLICATE_PERSISTENT_SPECIFIERS_IN_ATTRIBUTE_LIST_UDF_ERROR_MESSAGE),
                UserFilterValidationMessage.UNSUPPORTED_DUPLICATE_PERSISTENT_SPECIFIER_IN_ATTRIBUTE_LIST_UDF_SOLUTION, criteriaRawTrimmed);
    }
}
