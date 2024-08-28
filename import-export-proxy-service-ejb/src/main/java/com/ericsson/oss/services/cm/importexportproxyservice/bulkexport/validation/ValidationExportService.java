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

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.services.cm.export.api.*;
import com.ericsson.oss.services.cm.export.transformer.api.Transformer;
import com.ericsson.oss.services.cm.export.transformer.api.TransformerUserFilter;

/**
 * Set of utilities to validate parameters used to DPS calls.
 */
public interface ValidationExportService {
    /**
     * Validates and retrieves the {@code DataBucket} specified by name.
     *
     * @param config
     *            the configuration name
     * @return the configuration if already exists
     */
    DataBucket validateConfig(String config);

    /**
     * Create a {@code ValidationException} with a message and some additional
     * information.
     * This method can be used if a service needs to map an exception to a {@code ValidationException}
     *
     * @param error
     *            the export service error
     * @param additionalInfo
     *            some additional information to fill the message template
     * @return a ValidationException
     */
    ValidationException createValidationException(ExportServiceError error, Object... additionalInfo);

    /**
     * Validates and retrieves the {@code Transformer} specified by export type.
     *
     * @param exportType
     *            the export type name
     * @return the Transformer implementation for the export type if can found
     */
    Transformer validateExportType(String exportType);

    /**
     * Validates and retrieves the {@code JobExecution} specified by job execution ID and batch-processing job definition name.
     *
     * @param jobId
     *            the export job Id
     * @param jobDefinitionName
     *            the batch-processing job definition name.
     * @return the JobExecution for the job Id found
     */
    JobExecutionEntity validateJobExecution(Long jobId, String jobDefinitionName);

    /**
     * Validates the NodeSearchCriteria.
     *
     * @param nodeSearchCriteria
     *            nodeSearchCriteria
     */
    void validateNodeSearchCriteria(NodeSearchCriteria nodeSearchCriteria);

    /**
     * Validates the export filter.
     *
     * @param exportFilter
     *            export filter
     */
    void validateExportFilter(ExportFilter exportFilter);

    /**
     * Verifies that the namespace and name of the filter are neither null nor empty, and that the version is not null.
     *
     * @param namespace
     *            filter name space
     * @param name
     *            filter name
     * @param version
     *            filter version
     * @return
     *         true if the values specified in filter are not null and not empty; false otherwise
     */
    boolean isExportFilterTupleValid(String namespace, String name, String version);

    /**
     * Verifies that the job name is neither null nor empty.
     *
     * @param jobName
     *            the export job name
     */
    void validateJobName(final String jobName);

    /**
     * Verifies that the job ID (associated with the given job name) is valid.
     *
     * @param jobId
     *            the export job Id
     * @param jobName
     *            the export job name associated with the job ID
     */
    void validateJobId(Long jobId, String jobName);

    /**
     * Verifies that the job name does not exist.
     *
     * @param jobId
     *            the export job Id
     * @param jobName
     *            the export job name associated with the job ID
     */
    void validateJobNameDoesNotExist(Long jobId, String jobName);

    /**
     * Verifies that the appropriate license is available.
     *
     * @param exportType
     *            export type for license validation
     */
    void validateLicense(final String exportType);

    /**
     * Validates user defined filter.
     *
     * @param exportParameters
     *            export parameters
     * @return filter content
     */
    String validateUserFilter(ExportParameters exportParameters);

    /**
     * Validates user defined filter content.
     *
     * @param userFilter
     *            user defined filter to validate
     * @return boolean indicating whether the filter content is valid
     */
    boolean isUserFilterValid(final TransformerUserFilter userFilter);

    /**
     * Validates that batchFilter parameter is provided along with either predefinedFilter or userFilter.
     *
     * @param exportParameters
     *            export parameters
     */
    void validateBatching(final ExportParameters exportParameters);

    /**
     * Validates export filter. It should be either a user defined filter or a predefined filter and not both.
     *
     * @param exportParameters
     *            export parameters
     */
    void validateFilter(ExportParameters exportParameters);

    /**
     * Gets the export filter choice.
     *
     * @param exportParameters
     *            export parameters
     * @return the Filter choice.
     */
    FilterChoice getFilterChoice(final ExportParameters exportParameters);

    /**
     * Validates user defined filter content before parsing.
     *
     * @param userFilterFileContent
     *            userFilterFileContent
     */
    void validateUserFilterFileContent(final String userFilterFileContent);

    /**
     * Validates the requests for TCIM model.
     *
     * @param exportParameters
     *            export parameters
     */
    void validateTcimExport(final ExportParameters exportParameters);
}
