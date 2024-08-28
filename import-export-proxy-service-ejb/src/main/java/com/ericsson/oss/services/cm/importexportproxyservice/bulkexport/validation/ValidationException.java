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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.validation;

/**
 * Validation Exception implementation.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 5496664793544977316L;
    private final String solution;
    private final int code;

    /**
     * Constructor which takes the message of the exception.
     *
     * @param message
     *            of the exception
     * @param solution
     *            suggested solution
     * @param code
     *            error code
     */
    public ValidationException(final String message, final String solution, final int code) {
        super(message);
        this.solution = solution;
        this.code = code;
    }

    /**
     * Gets the suggested solution.
     *
     * @return the solution
     */
    public String solution() {
        return solution;
    }

    /**
     * Gets the error code.
     *
     * @return the error code.
     */
    public int code() {
        return code;
    }
}
