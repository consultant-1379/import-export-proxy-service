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

import static com.ericsson.oss.services.cm.export.api.DataCategory.TCIM_DATA;
import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.EMPTY_FILE;
import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.INVALID_JOB_ID;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.CONFIG_DOES_NOT_EXIST;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.EXPORT_JOB_SECURITY_EXCEPTION;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.EXPORT_NODES_NOT_SPECIFIED;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.EXPORT_NODES_SCOPE_INVALID;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.EXPORT_SEARCH_CRITERIA_INVALID;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.EXPORT_UNSUPPORTED_OPERATION;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.INVALID_EXPORT_FILTER;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.INVALID_USE_OF_BATCHING_WITHOUT_FILTER;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.INVALID_USE_OF_FILTER_OPTIONS;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.JOB_ID_NOT_FOUND;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.JOB_NAME_ALREADY_EXISTS;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.JOB_NAME_INVALID;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.JOB_NAME_NOT_FOUND;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.TRANSFORMER_NOT_FOUND;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.TRANSFORMER_PERMISSION_DENIED_NO_VALID_LICENSE_ERROR;
import static com.ericsson.oss.services.cm.export.api.ExportServiceError.USER_FILTER_CONTENT_INVALID;
import static com.ericsson.oss.services.cm.export.api.FilterChoice.NO_FILTER;
import static com.ericsson.oss.services.cm.export.api.FilterChoice.PREDEFINED;
import static com.ericsson.oss.services.cm.export.api.FilterChoice.USER_DEFINED_CONTENT;
import static com.ericsson.oss.services.cm.export.api.FilterChoice.USER_DEFINED_FILE;
import static com.ericsson.oss.services.cm.export.api.NodeSearchScope.ExportScopeType.FDN;
import static com.ericsson.oss.services.cm.export.api.NodeSearchScope.ExportScopeType.NODE_NAME;
import static com.ericsson.oss.services.cm.export.api.UserFilter.UserFilterType.FILE_CONTENT;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.util.CommonChecksUtil.isNullOrEmpty;

import java.util.List;
import java.util.regex.Pattern;

import javax.batch.operations.JobSecurityException;
import javax.batch.operations.NoSuchJobExecutionException;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.exception.general.ObjectNotFoundException;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.licensing.LicensingService;
import com.ericsson.oss.itpf.sdk.licensing.Permission;
import com.ericsson.oss.services.cm.export.api.ExportFilter;
import com.ericsson.oss.services.cm.export.api.ExportParameters;
import com.ericsson.oss.services.cm.export.api.ExportServiceError;
import com.ericsson.oss.services.cm.export.api.FilterChoice;
import com.ericsson.oss.services.cm.export.api.JobExecutionEntity;
import com.ericsson.oss.services.cm.export.api.NodeSearchCriteria;
import com.ericsson.oss.services.cm.export.api.NodeSearchScope;
import com.ericsson.oss.services.cm.export.api.UserFilter;
import com.ericsson.oss.services.cm.export.transformer.api.Transformer;
import com.ericsson.oss.services.cm.export.transformer.api.TransformerUserFilter;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.persistence.JobRepositoryBean;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.ejb.transformer.TransformerProvider;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.filter.ExportFilterModelHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.userfilter.UserFilterFileHandlerBean;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.userfilter.validation.UserFilterSyntaxValidator;

/**
 * {@link ValidationExportService} implementation.
 */
@Default
public class ValidationExportServiceBean implements ValidationExportService {

    public static final String TCIM_ROOT_FDN = "Network=1";
    private static final String EXPORT_TYPE_DYNAMIC = "dynamic";
    private static final String VP_DYNAMIC_CM_NBI__5MHzSC = "FAT1023443";
    private static final String VP_DYNAMIC_CM_NBI__CELL_CARRIER = "FAT1023603";
    private static final String VP_DYNAMIC_CM_NBI_ONOFFSCOPE_GSM_TRX = "FAT1023653";
    private static final String VP_DYNAMIC_CM_NBI_ONOFFSCOPE_RADIO = "FAT1023988";
    private static final String VP_DYNAMIC_CM_NBI_ONOFFSCOPE_CORE = "FAT1023989";
    private static final String VP_DYNAMIC_CM_NBI_ONOFFSCOPE_TRANSPORT = "FAT1023990";
    private static final String REGEX_SIMPLE_STRING_ALPHANUMERIC_HYPHEN_UNDERSCORE = "^[a-zA-Z0-9_][a-zA-Z0-9_-]*$";
    private static final String TCIM_INVALID_OPERATION_MESSAGE =
            "TCIM_DATA cannot be specified with any other data category and is only supported for the scope " + TCIM_ROOT_FDN;

    @EServiceRef
    private DataPersistenceService dataPersistenceService;

    @Inject
    private TransformerProvider transformerProvider;

    @Inject
    private JobRepositoryBean jobRepositoryBean;

    @Inject
    private ExportFilterModelHandler modelHandler;

    @Inject
    private LicensingService licensingService;

    @Inject
    private UserFilterFileHandlerBean userFilterFileHandlerBean;

    @Inject
    private Logger logger;

    @Override
    public DataBucket validateConfig(final String config) {
        try {
            final DataBucket bucket = dataPersistenceService.getDataBucket(config);
            return bucket;
        } catch (final ObjectNotFoundException objectNotFoundException) {
            throw createValidationException(CONFIG_DOES_NOT_EXIST, config);
        }
    }

    @Override
    public void validateNodeSearchCriteria(final NodeSearchCriteria nodeSearchCriteria) {
        if (nodeSearchCriteria == null) {
            throw createValidationException(EXPORT_SEARCH_CRITERIA_INVALID);
        }
        final List<NodeSearchScope> nodeSearchScopes = nodeSearchCriteria.getNodeSearchScopes();
        if (isNullOrEmpty(nodeSearchCriteria.getCollections()) && isNullOrEmpty(nodeSearchCriteria.getSavedSearches())) {
            validateNodeSearchScopeIsNotEmpty(nodeSearchScopes);
        }
        validateNodeSearchScopes(nodeSearchScopes);
    }

    private void validateNodeSearchScopeIsNotEmpty(final List<NodeSearchScope> nodeSearchScopes) {
        if (nodeSearchScopes.isEmpty()) {
            throw createValidationException(EXPORT_NODES_NOT_SPECIFIED);
        }
    }

    private void validateNodeSearchScopes(final List<NodeSearchScope> nodeSearchScopes) {
        for (final NodeSearchScope nodeSearchScope : nodeSearchScopes) {
            if (nodeSearchScope.getScopeType() == NODE_NAME || nodeSearchScope.getScopeType() == FDN) {
                final String value = nodeSearchScope.getValue();
                if (value == null || value.trim().isEmpty()) {
                    throw createValidationException(EXPORT_NODES_SCOPE_INVALID);
                }
            }
        }
    }

    @Override
    public ValidationException createValidationException(final ExportServiceError message, final Object... additionalInfo) {
        return new ValidationException(message.message(additionalInfo), message.solution(), message.code());
    }

    @Override
    public Transformer validateExportType(final String exportType) {
        try {
            return transformerProvider.getTransformer(exportType);
        } catch (final NamingException exception) {
            throw createValidationException(TRANSFORMER_NOT_FOUND, exportType);
        }
    }

    @Override
    public JobExecutionEntity validateJobExecution(final Long jobId, final String jobDefinitionName) {
        try {
            final JobExecutionEntity jobExecution = jobRepositoryBean.getJobExecution(jobId);
            if (jobExecution != null) {
                return jobExecution;
            } else {
                throw createValidationException(JOB_ID_NOT_FOUND, jobId.toString());
            }
        } catch (final NoSuchJobExecutionException noSuchJobExecutionException) {
            throw createValidationException(JOB_ID_NOT_FOUND, jobId.toString());
        } catch (final JobSecurityException jobSecurityException) {
            throw createValidationException(EXPORT_JOB_SECURITY_EXCEPTION);
        } catch (final Exception exception) {
            logger.error("Exception found while getting status for jobId {}", jobId.toString(), exception);
            throw createValidationException(JOB_ID_NOT_FOUND, jobId.toString());
        }
    }

    @Override
    public void validateExportFilter(final ExportFilter exportFilter) {
        if (exportFilter == null) {
            throw createValidationException(INVALID_EXPORT_FILTER);
        }
        if (isExportFilterTupleNoFilter(exportFilter.getNamespace(), exportFilter.getName(), exportFilter.getVersion())) {
            return; // apply no filtering
        }
        if (!isFilterValid(exportFilter)
                || !modelHandler.isExportFilterModelDeployed(exportFilter.getNamespace(), exportFilter.getName(), exportFilter.getVersion())) {
            throw createValidationException(INVALID_EXPORT_FILTER);
        }
    }

    /*
     * This will be called from {@link NodeItemProcessor} to check if ExportFilter is enabled/disabled.
     */
    @Override
    public boolean isExportFilterTupleValid(final String namespace, final String name, final String version) {
        return isValidNameSpace(namespace) && isValidName(name) && isValidVersion(version);
    }

    /**
     * Verifies ExportFilter by checking the namespace and name are neither null nor empty, and the version is not null.
     *
     * @param filter
     *            the filter to verify
     * @return true if the values specified in filter are not null and not empty; false otherwise
     */
    private boolean isFilterValid(final ExportFilter filter) {
        return isExportFilterTupleValid(filter.getNamespace(), filter.getName(), filter.getVersion());
    }

    private boolean isValidNameSpace(final String namespace) {
        return !isNoFilter(namespace);
    }

    private boolean isValidName(final String name) {
        return isValidString(name) && !isNoFilter(name);
    }

    private boolean isValidVersion(final String version) {
        return version != null && !isNoFilter(version);
    }

    private boolean isValidString(final String str) {
        return str != null && !str.trim().isEmpty();
    }

    public boolean isValidJobName(final String jobName) {
        return Pattern.matches(REGEX_SIMPLE_STRING_ALPHANUMERIC_HYPHEN_UNDERSCORE, jobName);
    }

    private boolean isNoFilter(final String str) {
        return isValidString(str) && str.equals(ExportFilter.NO_FILTER_STRING);
    }

    private boolean isExportFilterTupleNoFilter(final String namespace, final String name, final String version) {
        return isNoFilter(namespace) && isNoFilter(name) && isNoFilter(version);
    }

    @Override
    public void validateJobName(final String jobName) {
        if (!isValidString(jobName) || !isValidJobName(jobName)) {
            throw createValidationException(JOB_NAME_INVALID);
        }
    }

    @Override
    public void validateJobId(final Long jobId, final String jobName) {
        if (INVALID_JOB_ID.equals(jobId)) {
            throw createValidationException(JOB_NAME_NOT_FOUND, jobName);
        }
    }

    @Override
    public void validateJobNameDoesNotExist(final Long jobId, final String jobName) {
        if (!INVALID_JOB_ID.equals(jobId)) {
            throw createValidationException(JOB_NAME_ALREADY_EXISTS, jobName);
        }
    }

    @Override
    public void validateLicense(final String exportType) {
        if (!EXPORT_TYPE_DYNAMIC.equals(exportType)) {
            return;
        }
        switch (getDynamicFileFormatLicensePermission()) {
            case ALLOWED:
                return;
            case DENIED_NO_VALID_LICENSE:
                throw createValidationException(TRANSFORMER_PERMISSION_DENIED_NO_VALID_LICENSE_ERROR, exportType);
            default:
                break;
        }
    }

    private Permission getDynamicFileFormatLicensePermission() {
        if (licensingService.validatePermission(VP_DYNAMIC_CM_NBI__5MHzSC) == Permission.ALLOWED
                || licensingService.validatePermission(VP_DYNAMIC_CM_NBI_ONOFFSCOPE_GSM_TRX) == Permission.ALLOWED
                || licensingService.validatePermission(VP_DYNAMIC_CM_NBI__CELL_CARRIER) == Permission.ALLOWED
                || licensingService.validatePermission(VP_DYNAMIC_CM_NBI_ONOFFSCOPE_RADIO) == Permission.ALLOWED
                || licensingService.validatePermission(VP_DYNAMIC_CM_NBI_ONOFFSCOPE_CORE) == Permission.ALLOWED
                || licensingService.validatePermission(VP_DYNAMIC_CM_NBI_ONOFFSCOPE_TRANSPORT) == Permission.ALLOWED) {
            return Permission.ALLOWED;
        }
        return Permission.DENIED_NO_VALID_LICENSE;
    }

    @Override
    public String validateUserFilter(final ExportParameters exportParameters) {
        String filterFileContent = EMPTY_FILE;
        final UserFilter userFilter = exportParameters.getUserFilter();
        if (userFilter == null) {
            return filterFileContent;
        }
        switch (userFilter.getUserFilterType()) {
            case FILE_NAME:
                filterFileContent = userFilterFileHandlerBean.readUserFilterFile(userFilter.getValue());
                logger.info("{} : {} : File Content : {}", UserFilter.UserFilterType.FILE_NAME, userFilter.getValue(), filterFileContent);
                break;
            case FILE_CONTENT:
                filterFileContent = userFilter.getValue();
                break;
            default:
                logger.error("Invalid user filter type specified");
        }
        if (filterFileContent == null) {
            throw createValidationException(USER_FILTER_CONTENT_INVALID);
        }
        return filterFileContent;
    }

    @Override
    public boolean isUserFilterValid(final TransformerUserFilter userFilter) {
        return userFilter != null && !userFilter.getMoSpecifications().isEmpty();
    }

    @Override
    public void validateBatching(final ExportParameters exportParameters) {
        final UserFilter userFilter = exportParameters.getUserFilter();
        final ExportFilter predefinedFilter = exportParameters.getFilter();
        if (exportParameters.isBatchFilter() != null && !isFilterValid(predefinedFilter) && userFilter == null) {
            throw createValidationException(INVALID_USE_OF_BATCHING_WITHOUT_FILTER);
        }
    }

    @Override
    public void validateFilter(final ExportParameters exportParameters) {
        final UserFilter userFilter = exportParameters.getUserFilter();
        final ExportFilter predefinedFilter = exportParameters.getFilter();
        if (predefinedFilter == null) {
            throw createValidationException(INVALID_EXPORT_FILTER);
        }
        if (userFilter != null && isFilterValid(predefinedFilter)) {
            // both filters cannot be specified.
            throw createValidationException(INVALID_USE_OF_FILTER_OPTIONS);
        }
    }

    @Override
    public FilterChoice getFilterChoice(final ExportParameters exportParameters) {
        // This method assumes all filter validations have already done
        final ExportFilter predefinedFilter = exportParameters.getFilter();
        final UserFilter userFilter = exportParameters.getUserFilter();
        FilterChoice filterChoice = NO_FILTER;
        if (isFilterValid(predefinedFilter)) {
            filterChoice = PREDEFINED;
        } else if (userFilter != null) {
            if (userFilter.getUserFilterType() == FILE_CONTENT) {
                filterChoice = USER_DEFINED_CONTENT;
            } else {
                filterChoice = USER_DEFINED_FILE;
            }
        }
        return filterChoice;
    }

    @Override
    public void validateUserFilterFileContent(final String userFilterFileContent) {
        UserFilterSyntaxValidator.checkFilterCompliantToExpectedPattern(userFilterFileContent);
    }

    @Override
    public void validateTcimExport(final ExportParameters exportParameters) {
        if (exportParameters.getDataCategories().contains(TCIM_DATA)) {
            if (exportParameters.getDataCategories().size() > 1 || isInvalidTcimScope(exportParameters.getScope())) {
                throw createValidationException(EXPORT_UNSUPPORTED_OPERATION, TCIM_INVALID_OPERATION_MESSAGE);
            }
        } else {
            for (final NodeSearchScope nodeSearchScope : exportParameters.getScope().getNodeSearchScopes()) {
                if (isTcimRootFdn(nodeSearchScope)) {
                    throw createValidationException(EXPORT_UNSUPPORTED_OPERATION, TCIM_INVALID_OPERATION_MESSAGE);
                }
            }
        }
    }

    private boolean isInvalidTcimScope(final NodeSearchCriteria scope) {
        return !isNullOrEmpty(scope.getCollections())
                || !isNullOrEmpty(scope.getSavedSearches())
                || scope.getNodeSearchScopes().size() > 1
                || !isTcimRootFdn(scope.getNodeSearchScopes().get(0));
    }

    private boolean isTcimRootFdn(final NodeSearchScope scope) {
        return scope.getScopeType() == FDN && scope.getValue().equals(TCIM_ROOT_FDN);
    }
}