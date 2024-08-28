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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.persistence;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportJobDetails;
import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportJobDetails.ImportJobDetailsBuilder;
import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportOperationDetails;
import com.ericsson.oss.services.cm.async.bulkimport.job.status.AsyncBulkImportJobStatus;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobStatus;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobType;
import com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportJobSpecification;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportServiceSpecification;
import com.ericsson.oss.services.cm.bulkimport.entities.Failure;
import com.ericsson.oss.services.cm.bulkimport.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.entities.ImportOperationStatus;
import com.ericsson.oss.services.cm.bulkimport.entities.ImportSimpleAttribute;
import com.ericsson.oss.services.cm.bulkimport.entities.Job;
import com.ericsson.oss.services.cm.bulkimport.entities.Operation;
import com.ericsson.oss.services.cm.bulkimport.entities.OperationStatusLegacy;
import com.ericsson.oss.services.cm.bulkimport.util.SerializationUtil;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.ImportJobDoesNotExistException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.UnexpectedErrorException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util.FormatLogger;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util.PropertiesUtils;

/**
 * Database interaction class to process request towards Import database.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@SuppressWarnings("PMD.TooManyFields")
public class ImportPersistenceManagerBean {
    public static final int DB_THRESHOLD_LIMIT = 90;
    public static final String DEPLOYMENT_TYPE_PROPERTY = "enm_deployment_type";
    public static final String JOB_ID = "jobId";
    public static final String OPERATION_COPY_MO = "COPYMANAGEDOBJECTS";
    public static final String OPERATION_COPY_ROOT = "copyRoot";
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(ImportPersistenceManagerBean.class));
    private static final String OPERATION_CREATE = "1";
    private static final String OPERATION_UPDATE = "2";
    private static final String OPERATION_DELETE = "3";
    private static final String OPERATION_ACTION = "4";
    private static final String ID = "id";
    private static final String IMPORT_OPERATION_STATUS = "importOperationStatus";
    private static final String JOB = "job";
    private static final String ENTITY_FIELD_IMPORT_JOB = "importJob";
    private static final String ENTITY_FIELD_IMPORT_JOB_ID = "id";
    private static final String FDN = "fdn";
    private static final String ENTITY_FIELD_SCHEDULE_TIME = "scheduleTime";
    private static final String ENTITY_FIELD_TIME_START = "timeStart";
    private static final String IMPORT_OPERATION_UPDATE_TIME = "timeUpdate";
    private static final String IMPORT_OPERATION_NE_IDS = "neIds";
    private static final String ENTITY_FIELD_JOB_STATUS = "status";
    private static final String ENTITY_FIELD_JOB_TYPE = "jobType";
    private static final Collection<String> ACTIVE_JOB_STATUS = Arrays.asList(JobStatus.CANCELLING.getStatus(), JobStatus.PARSING.getStatus(),
            JobStatus.VALIDATING.getStatus(), JobStatus.EXECUTING.getStatus(), AsyncBulkImportJobStatus.IN_PROGRESS.toString());
    private static final int BYTES_IN_ONE_MB = 1024 * 1024;
    private static final String METHOD_NAME = "Method name: {}";

    @Inject
    @PersistenceContext(unitName = "importPersistenceUnit")
    EntityManager entityManager;

    /**
     * Gets job details identified by given job id.
     *
     * @param jobId
     *            job identifier.
     * @return Job instance.
     */
    public Job getJob(final long jobId) {
        LOGGER.debug(String.format("Getting job with id = %d. Method name: {}", jobId), "getJob");
        return entityManager.find(Job.class, jobId);
    }

    /**
     * Get the details of the job with the specified ID. The jobs are created as a result of invocation of the asynchronous import service.
     *
     * @param jobId
     *            the jobId of the import service execution job.
     * @return {@code ImportJobDetails} the job detail.
     *         Use vertically sliced persistence beans instead.
     */
    @SuppressWarnings("unchecked")
    public List<ImportJobDetails> getJobDetails(final Long jobId) {
        LOGGER.info("Getting job details with id = {}. Method name: {}", jobId, "getJobDetails");
        return getImportJobDetailsList(jobId);
    }

    /**
     * Get the details of all the jobs based on the create date filter. The jobs are created as a result of invocation of the asynchronous import
     * service.
     *
     * @return {@code ImportJobDetails} list of the job details.
     *         Use vertically sliced persistence beans instead.
     */
    @SuppressWarnings("unchecked")
    public List<ImportJobDetails> getAllJobDetails(final Date beginTime, final Date endTime) {
        LOGGER.info("Getting all jobs details for time filters. Method name: {}", "getAllJobDetails");
        return getImportJobDetailsList(beginTime, endTime);
    }

    /**
     * Gets all the operations for a specified job id stored in Import database.
     *
     * @param jobId
     *            job identifier.
     * @return list of operation instances.
     *         Use vertically sliced persistence beans instead.
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<ImportOperationDetails> getAllImportOperationDetailsForJob(final long jobId) {
        LOGGER.info("Getting all operation details for jobId {}. Method name: {}", jobId, "getAllImportOperationDetailsForJob");
        final List<ImportOperationDetails> operationDetailsList = new ArrayList<>();
        final List<Operation> operations = entityManager.createNamedQuery("Operation.findAllForJob")
                .setParameter(JOB_ID, jobId)
                .getResultList();
        for (final Operation operation : operations) {
            addImportOperationDetails(operation, operationDetailsList);
        }
        final List<ImportOperation> importOperations = getAllAttemptedImportOperations(jobId);
        for (final ImportOperation importOperation : importOperations) {
            addImportOperationDetails(importOperation, operationDetailsList);
        }
        Collections.sort(operationDetailsList);
        return operationDetailsList;
    }

    /**
     * Gets all the import operations for a specified job id stored in Import database.
     *
     * @param jobId
     *            job identifier.
     * @return list of import operation instances.
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<ImportOperation> getAllImportOperations(final long jobId) {
        LOGGER.info("Getting all operations for jobId {}. Method name: {}", jobId, "getAllImportOperations");
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> criteriaQuery = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(importOperation).orderBy(criteriaBuilder.asc(importOperation.get(ID))).where(
                criteriaBuilder.equal(
                        importOperation.get(JOB).get(ID),
                        jobId));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Gets all the import operations for a specified job id stored in Import database.
     *
     * @param jobId
     *            job identifier.
     * @param importOperationStatus
     *            all import operation status valid for a specific job.
     * @return list of import operation instances.
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<ImportOperation> getAllImportOperations(final long jobId, final ImportOperationStatus... importOperationStatus) {
        LOGGER.info("Getting all operations for jobId {} with status {}. Method name: {}", jobId, importOperationStatus, "getAllImportOperations");
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> criteriaQuery = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(importOperation).orderBy(criteriaBuilder.asc(importOperation.get(ID))).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(
                                importOperation.get(JOB).get(ID),
                                jobId),
                        importOperation.get(IMPORT_OPERATION_STATUS)
                                .in(importOperationStatus)));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Gets all the import operations for a specified job id stored in Import database.
     *
     * @param importOperationIds
     *            a list of importOperationIds to retrieve.
     * @return list of import operation instances.
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<ImportOperation> getAllImportOperations(final List<Long> importOperationIds) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> criteriaQuery = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(importOperation).orderBy(criteriaBuilder.asc(importOperation.get(ID))).where(
                importOperation.get(ID)
                        .in(importOperationIds));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Gets specified partition (supplying OFFSET and LIMIT) of import operations for a specified job id stored in Import database.
     *
     * @param jobId
     *            job identifier.
     * @param importOperationStatus
     *            all import operation status valid for a specific job.
     * @return list of import operation instances.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<ImportOperation> getImportOperationsPartition(final long jobId, final int offset,
            final int limit, final ImportOperationStatus... importOperationStatus) {
        LOGGER.info("Getting specified range of operations for jobId {} with status {}, offset {} and limit {}. Method name: {}",
                jobId, importOperationStatus, offset, limit, "getImportOperationsPartition");
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> criteriaQuery = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(importOperation).orderBy(criteriaBuilder.asc(importOperation.get(ID))).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(
                                importOperation.get(JOB).get(ID),
                                jobId)));
        final List<ImportOperation> importOperations = entityManager.createQuery(criteriaQuery)
                .setFirstResult(offset).setMaxResults(limit).getResultList();
        final List<ImportOperation> importOperationsFilteredByStatus = importOperations.stream()
                .filter(operation -> Arrays.asList(importOperationStatus).contains(operation.getImportOperationStatus()))
                .collect(Collectors.toList());
        return importOperationsFilteredByStatus;
    }

    /**
     * Gets count of import operations for a specified job id stored in Import database.
     *
     * @param jobId
     *            job identifier.
     * @param importOperationStatus
     *            all import operation status valid for a specific job.
     * @return count of import operations.
     */
    public long getImportOperationsCount(final long jobId, final ImportOperationStatus... importOperationStatus) {
        LOGGER.info("Getting Count of operations for jobId {} with status {}. Method name: {}",
                jobId, importOperationStatus, "getImportOperationCount");
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(criteriaBuilder.count(importOperation)).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(
                                importOperation.get(JOB).get(ID),
                                jobId),
                        importOperation.get(IMPORT_OPERATION_STATUS)
                                .in(importOperationStatus)));
        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    /**
     * Returns File Path.
     *
     * @param jobId
     *            the job Id
     * @return FilePath
     */
    @SuppressWarnings("unchecked")
    public String getFilePath(final long jobId) {
        LOGGER.debug("Getting fileName for jobId {}", jobId);
        return getImportJobSpecification(jobId).getFilePath();
    }

    @SuppressWarnings("unchecked")
    private ImportJobSpecification getImportJobSpecification(final long jobId) {
        final String queryString = "SELECT j.importSpecification FROM Job AS j WHERE j.id = ?1";
        final List<String> resultList = entityManager.createQuery(queryString).setParameter(1, jobId).getResultList();
        if (resultList.isEmpty()) {
            throw new ImportJobDoesNotExistException(jobId);
        }
        return SerializationUtil.deserialize(resultList.get(0), ImportJobSpecification.class);
    }

    /**
     * Gets the import operation for a specified id stored in Import database.
     *
     * @param importOperationId
     *            .
     * @return list of import operation instances.
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ImportOperation getImportOperation(final Long importOperationId) {
        return entityManager.find(ImportOperation.class, importOperationId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ImportOperation getImportOperationWithAttributesLoaded(final Long importOperationId) {
        final ImportOperation importOperation = getImportOperation(importOperationId);
        ensureAttributesFetched(importOperation);
        return importOperation;
    }

    /**
     * Gets all the import operations for a specified job id stored in Import database and load all attributes as part of same transaction.
     *
     * @param jobId
     *            job identifier.
     * @return list of import operation instances.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<ImportOperation> getAllImportOperationsWithAttributesLoaded(final long jobId) {
        LOGGER.info("Getting all operations for jobId. Method name: {}", "getAllImportOperationsWithAttributesLoaded");
        final List<ImportOperation> importOperations = getAllImportOperations(jobId);
        for (final ImportOperation importOperation : importOperations) {
            ensureAttributesFetched(importOperation);
        }
        return importOperations;
    }

    /**
     * Returns configuration properties and the status of a Job.
     *
     * @param jobId
     *            the job Id
     * @return ImportJobProperties
     *         Use vertically sliced persistence beans instead.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ImportServiceSpecification getImportServiceSpecification(final long jobId) {
        final String queryString = "SELECT j.status,j.importSpecification FROM Job AS j WHERE j.id = ?1";
        final List<Object> resultList = entityManager.createQuery(queryString)
                .setParameter(1, jobId)
                .getResultList();
        if (resultList.isEmpty()) {
            throw new ImportJobDoesNotExistException(jobId);
        }
        final Object[] resultObject = (Object[]) resultList.get(0);
        final ImportServiceSpecification importServiceSpecification =
                (ImportServiceSpecification) deserializeClob(resultObject[1].toString());
        return importServiceSpecification;
    }

    public List<ImportOperation> getAllAttemptedImportOperations(final long jobId) {
        LOGGER.info("Getting all executed import operations for jobId {}", jobId);
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> criteriaQuery = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(importOperation).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(
                                importOperation.get(JOB).get(ID),
                                jobId),
                        criteriaBuilder.notEqual(
                                importOperation.get(IMPORT_OPERATION_STATUS),
                                ImportOperationStatus.PARSED)));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public Map<String, Integer> getOperationCountByType(final Long jobId) {
        final String query = "SELECT operation_type, count(operation_type) FROM import_operation WHERE job_Id=?1 GROUP BY operation_type;";
        return iterateOverResultAndExctractOperationCount(jobId, query);
    }

    public Map<String, Integer> getOperationCountByStatus(final Long jobId) {
        final String query = "SELECT operation_status, count(operation_status) FROM import_operation WHERE job_Id=?1 GROUP BY operation_status;";
        return iterateOverResultAndExctractOperationCount(jobId, query);
    }

    private Map<String, Integer> iterateOverResultAndExctractOperationCount(final Long jobId, final String query) {
        final Iterator<Object> queryResultIterator = entityManager.createNativeQuery(query).setParameter(1, jobId).getResultList().iterator();
        final Map<String, Integer> operationCount = new HashMap<>();
        while (queryResultIterator.hasNext()) {
            final Object[] result = (Object[]) queryResultIterator.next();
            operationCount.put(result[0].toString(), ((BigInteger) result[1]).intValue());
        }
        return operationCount;
    }

    public long getAllAttemptedOperationInCurrentInvocation(final long jobId, final Timestamp invocationStartTime) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        final List<Predicate> conditions = getConditionsForCurrentInvocation(jobId, invocationStartTime, criteriaBuilder, importOperation);
        criteriaQuery.select(criteriaBuilder.count(importOperation)).where(
                criteriaBuilder.and(conditions.toArray(new Predicate[conditions.size()])));
        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    public long getUniqueNeIdsCount(final long jobId, final Timestamp invocationStartTime) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        final List<Predicate> conditions = getConditionsForCurrentInvocation(jobId, invocationStartTime, criteriaBuilder, importOperation);
        criteriaQuery.select(criteriaBuilder.countDistinct(importOperation.get(IMPORT_OPERATION_NE_IDS))).where(
                criteriaBuilder.and(conditions.toArray(new Predicate[conditions.size()])));
        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    private List<Predicate> getConditionsForCurrentInvocation(final long jobId, final Timestamp invocationStartTime,
            final CriteriaBuilder criteriaBuilder, final Root<ImportOperation> importOperation) {
        final List<Predicate> conditions = new ArrayList<>();
        final Predicate maxPastTimePredicate =
                criteriaBuilder.greaterThanOrEqualTo(importOperation.<Timestamp>get(IMPORT_OPERATION_UPDATE_TIME), invocationStartTime);
        conditions.add(maxPastTimePredicate);
        final Predicate jobIdPredicate = criteriaBuilder.equal(importOperation.get(JOB).get(ID), jobId);
        conditions.add(jobIdPredicate);
        return conditions;
    }

    private ImportJobDetails createImportJobDetails(final ImportJobDto jobDto) {
        return new ImportJobDetailsBuilder()
                .setId(jobDto.getJobId())
                .setAsyncBulkImportJobStatus(jobDto.getJobStatus())
                .setFileFormat(jobDto.getFileFormat())
                .setPlannedArea(jobDto.getConfiguration())
                .setFilePath(jobDto.getFilePath())
                .setTotalManagedObjectCreated(jobDto.getCreateCount())
                .setTotalManagedObjectUpdated(jobDto.getUpdateCount())
                .setTotalManagedObjectDeleted(jobDto.getDeleteCount())
                .setTotalMoActionsPerformed(jobDto.getActionCount())
                .setStartTime(jobDto.getStartTime())
                .setEndTime(jobDto.getEndTime())
                .setLastValidationTime(jobDto.getLastValidationTime())
                .setFailureReason(jobDto.getFailureReason())
                .setNodesNotCopied(jobDto.getCopyRootCount())
                .setElapsedTimeSeconds(jobDto.getElapsedTime())
                .setUserId(jobDto.getUserId())
                .build();
    }

    private List<ImportJobDetails> getImportJobDetailsList(final Date beginTime, final Date endTime) {
        final List<ImportJobDetails> importJobDetailsList = new ArrayList<>();
        final Query importOperationDetailsQuery = addTimeFilterToQuery(beginTime, endTime, createImportOperationDetailsQuery());
        queryOperationDetailsAndBuildImportJobDetailsList(importJobDetailsList, importOperationDetailsQuery);
        return importJobDetailsList;
    }

    private List<ImportJobDetails> getImportJobDetailsList(final Long jobId) {
        final List<ImportJobDetails> importJobDetailsList = new ArrayList<>();
        final Query importOperationDetailsQuery = addIdFilterToQuery(jobId, createImportOperationDetailsQuery());
        queryOperationDetailsAndBuildImportJobDetailsList(importJobDetailsList, importOperationDetailsQuery);
        return importJobDetailsList;
    }

    private void queryOperationDetailsAndBuildImportJobDetailsList(final List<ImportJobDetails> importJobDetailsList,
            final Query importOperationDetailsQuery) {
        final Map<Long, ImportJobDto> importJobDtoMap = new HashMap<>();
        final Iterator<Object> importOperationQueryResultIterator = importOperationDetailsQuery.getResultList().iterator();
        iterateResultsAndPrepareJobIdDtoMap(importOperationQueryResultIterator, importJobDtoMap);
        for (final ImportJobDto jobDto : importJobDtoMap.values()) {
            final ImportJobDetails jobDetails = createImportJobDetails(jobDto);
            importJobDetailsList.add(jobDetails);
        }
    }

    private void iterateResultsAndPrepareJobIdDtoMap(final Iterator<Object> queryResultIterator, final Map<Long, ImportJobDto> importJobDtoMap) {
        while (queryResultIterator.hasNext()) {
            final Object[] result = (Object[]) queryResultIterator.next();
            final long jobId = ((BigInteger) result[0]).longValue();
            LOGGER.trace("Iterate results and prepare jobId to Map for jobId [{}]", jobId);
            final ImportJobDto jobDto = importJobDtoMap.containsKey(jobId) ? importJobDtoMap.get(jobId) : new ImportJobDto(result).getJobDto();
            updateOperationCountBasedOnOperationType(result[6], result[7], jobDto);
            LOGGER.trace("Job DTO: {} ", jobDto);
            importJobDtoMap.put(jobId, jobDto);
        }
    }

    private Query addIdFilterToQuery(final Long jobId, final String query) {
        final String where = " WHERE Job.id = ?1";
        final Query jobDetailsQuery = entityManager.createNativeQuery(query + where);
        jobDetailsQuery.setParameter(1, jobId);
        return jobDetailsQuery;
    }

    private Query addTimeFilterToQuery(final Date beginTime, final Date endTime, final String query) {
        final String filteredQuery = appendQueryWithFilteredJobs(beginTime, endTime, query);
        LOGGER.debug("Query after appending with time filter {}", filteredQuery);
        final Query jobDetailsQuery = entityManager.createNativeQuery(filteredQuery);
        setQueryParamsForTimeFilters(beginTime, endTime, jobDetailsQuery);
        return jobDetailsQuery;
    }

    private String createImportOperationDetailsQuery() {
        final String query =
                "SELECT "
                        + " Job.id, Job.status, Job.import_specification, Job.time_start, Job.time_end, Job.failure_cause,"
                        + " OperationDetails.operation_type, OperationDetails.operation_count, Job.time_elapsed_seconds,"
                        + " Job.last_validation_time, Job.username"
                        + " FROM job Job"
                        + " LEFT OUTER JOIN"
                        + "   (SELECT job_id, operation_type, count(operation_type) AS operation_count"
                        + "    FROM import_operation"
                        + "    WHERE "
                        + "      (operation_type IN('1','2','3','4') AND operation_status='EXECUTED')"
                        + "    GROUP BY job_id, operation_type) OperationDetails"
                        + " ON Job.id=OperationDetails.job_id";
        return query;
    }

    private String appendQueryWithFilteredJobs(final Date beginTime, final Date endTime, final String query) {
        final String queryWithFilter = query + " where Job.id in (" + getJobsWithTimeAndLimitFilter(beginTime, endTime) + ")";
        return queryWithFilter;
    }

    private String getJobsWithTimeAndLimitFilter(final Date beginTime, final Date endTime) {
        String query = "SELECT id from Job" + " WHERE (Job.job_type = '" + JobType.IMPORT.getType() + "') ";
        int paramNumber = 0;
        if (isValidValue(beginTime)) {
            query += " AND Job.time_start>= ?" + ++paramNumber;
        }
        if (isValidValue(endTime)) {
            query += " AND Job.time_start<= ?" + ++paramNumber;
        }
        query += " ORDER BY Job.time_start DESC LIMIT " + ImportConstants.MAX_JOB_COUNT_FOR_RETRIEVAL;
        return query;
    }

    private void setQueryParamsForTimeFilters(final Date beginTime, final Date endTime, final Query jobDetailsQuery) {
        int paramNumber = 0;
        if (isValidValue(beginTime)) {
            jobDetailsQuery.setParameter(++paramNumber, new Timestamp(beginTime.getTime()));
        }
        if (isValidValue(endTime)) {
            jobDetailsQuery.setParameter(++paramNumber, new Timestamp(endTime.getTime()));
        }
    }

    private boolean isValidValue(final Object value) {
        return value != null;
    }

    /**
     * Deletes the data from all the tables in importdb for the supplied jobId.
     */
    public void cleanUpImportData(final long jobId) {
        LOGGER.debug("Clean import_operation and job tables used by import process for jobId : {}", jobId);
        final Query deleteImportOperationQuery = entityManager.createNativeQuery("DELETE FROM import_operation WHERE job_id = ?1");
        deleteImportOperationQuery.setParameter(1, jobId);
        deleteImportOperationQuery.executeUpdate();
        final Query deleteJobQuery = entityManager.createNativeQuery("DELETE FROM job WHERE id = ?1");
        deleteJobQuery.setParameter(1, jobId);
        deleteJobQuery.executeUpdate();
    }

    private double getCurrentDbSizeInMb() {
        final Query query = entityManager.createNativeQuery("SELECT pg_database_size('importdb')");
        final BigInteger sizeBytes = (BigInteger) query.getResultList().get(0);
        final double sizeInMb = sizeBytes.doubleValue() / BYTES_IN_ONE_MB;
        LOGGER.info("Current import Db size in MB's : {}", sizeInMb);
        return sizeInMb;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void doVacuum() {
        final Session session = entityManager.unwrap(Session.class);
        final SessionImpl sessionImpl = (SessionImpl) session;
        final Connection connection = sessionImpl.connection();
        try (final PreparedStatement statement = connection.prepareStatement("VACUUM ANALYZE")) {
            statement.execute();
            LOGGER.info("VACUUM ANALYZE Completed");
        } catch (final SQLException exception) {
            LOGGER.error("Could not run Vacuum Analyze command : {}", exception);
        }
    }

    /**
     * Fetch the min date from the database and convert it into 'yyyy-MM-DD' format.
     * Converting the String format to data with Timestamp 00:00:00.
     */
    public Date getMinDateFromDb() {
        final Query query = entityManager.createNativeQuery("SELECT MIN(time_start)from job");
        final Date minDate = (Date) query.getResultList().get(0);
        final SimpleDateFormat simpleMinDate = new SimpleDateFormat("yyyy-MM-dd");
        return Date.from(LocalDate.parse(simpleMinDate.format(minDate)).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public int getdbPercentage() {
        int allocatedDbSize = 0;
        final String enmDeploymentType = PropertiesUtils.fetchProperty(DEPLOYMENT_TYPE_PROPERTY);
        LOGGER.debug("DEPLOYMENT_TYPE_PROPERTY : {}", enmDeploymentType);
        if (enmDeploymentType == null) {
            return 0;
        }
        switch (enmDeploymentType) {
            case "Extra_Large_ENM":
            case "Extra_Large_CloudNative_ENM":
            case "Extra_Large_ENM_On_Rack_Servers":
            case "Large_ENM":
            case "Large_Transport_only_ENM":
                allocatedDbSize = 12288;
                break;
            case "Medium_ENM":
            case "SIENM_multi_technology":
            case "Small_ENM_customer_cloud":
            case "Small_CloudNative_ENM":
            case "OSIENM_transport_only":
            case "SIENM_transport_only":
                allocatedDbSize = 8192;
                break;
            case "ENM_extra_small":
                allocatedDbSize = 4096;
                break;
            default:
                return 0;
        }
        return (int) ((getCurrentDbSizeInMb() / allocatedDbSize) * 100);
    }

    private Object deserializeClob(final String clob) {
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(clob.getBytes());
            final XMLDecoder decoder = new XMLDecoder(inputStream);
            final Object result = decoder.readObject();
            decoder.close();
            inputStream.close();
            return result;
        } catch (final IOException ioException) {
            throw new UnexpectedErrorException(ioException.getMessage());
        }
    }

    private void addImportOperationDetails(final Operation operation, final List<ImportOperationDetails> operationDetailsList) {
        final List<Failure> failures = operation.getFailures();
        if (failures != null && !failures.isEmpty()) {
            addImportOperationDetailsPerFailureReasonForFailedOperation(operation, operationDetailsList, failures);
        } else {
            addImportOperationDetailsForSuccessfulOperation(operation, operationDetailsList);
        }
    }

    private void addImportOperationDetails(final ImportOperation importOperation, final List<ImportOperationDetails> operationDetailsList) {
        final List<Failure> failures = importOperation.getFailures();
        if (failures != null && !failures.isEmpty()) {
            addImportOperationDetailsPerFailureReasonForFailedImportOperation(importOperation, operationDetailsList, failures);
        } else {
            addImportOperationDetailsForSuccessfulImportOperation(importOperation, operationDetailsList);
        }
    }

    private void addImportOperationDetailsForSuccessfulOperation(final Operation operation,
            final List<ImportOperationDetails> operationDetailsList) {
        LOGGER.trace("Creating ImportOperationDetails with operation [{}]", operation);
        operationDetailsList.add(new ImportOperationDetails(operation.getId(), operation.getJob().getId(),
                operation.getTimeUpdate(), operation.getOperationStatus().toString(),
                operation.getOperationType(), operation.getFdn(),
                null, null));
    }

    private void addImportOperationDetailsPerFailureReasonForFailedOperation(final Operation operation,
            final List<ImportOperationDetails> operationDetailsList,
            final List<Failure> failures) {
        for (final Failure failure : failures) {
            LOGGER.trace("Creating ImportOperationDetails with operation [{}] and failure [{}]", operation, failure);
            operationDetailsList.add(new ImportOperationDetails(operation.getId(), operation.getJob().getId(),
                    operation.getTimeUpdate(), operation.getOperationStatus().toString(),
                    operation.getOperationType(), operation.getFdn(),
                    failure.getFailureCause(), failure.getLineNumber()));
        }
    }

    private void addImportOperationDetailsForSuccessfulImportOperation(final ImportOperation importOperation,
            final List<ImportOperationDetails> operationDetailsList) {
        LOGGER.trace("Creating ImportOperationDetails with importOperation [{}]", importOperation);
        final ImportOperationStatus importOperationStatus = importOperation.getImportOperationStatus();
        operationDetailsList.add(new ImportOperationDetails(importOperation.getId(), importOperation.getJob().getId(),
                importOperation.getTimeUpdate(),
                String.valueOf(OperationStatusLegacy.fromOperationStatus(importOperationStatus)),
                String.valueOf(importOperation.getOperationType()),
                importOperation.getFdn(), null, importOperation.getLineNumber()));
    }

    private void addImportOperationDetailsPerFailureReasonForFailedImportOperation(final ImportOperation importOperation,
            final List<ImportOperationDetails> operationDetailsList,
            final List<Failure> failures) {
        for (final Failure failure : failures) {
            LOGGER.trace("Creating ImportOperationDetails with importOperation [{}] and failure [{}]", importOperation, failure);
            final ImportOperationStatus importOperationStatus = importOperation.getImportOperationStatus();
            operationDetailsList.add(new ImportOperationDetails(importOperation.getId(), importOperation.getJob().getId(),
                    importOperation.getTimeUpdate(), OperationStatusLegacy.fromOperationStatus(importOperationStatus).toString(),
                    importOperation.getOperationType().getMessage(), importOperation.getFdn(), failure.getFailureCause(),
                    importOperation.getLineNumber()));
        }
    }

    private void updateOperationCountBasedOnOperationType(final Object operationTypeFromResult, final Object operationCountFromResult,
            final ImportJobDto jobDto) {
        if (operationTypeFromResult != null && jobDto != null) {
            final String operationType = operationTypeFromResult.toString();
            final int operationCount = ((BigInteger) operationCountFromResult).intValue();
            switch (operationType) {
                case OPERATION_CREATE:
                    jobDto.setCreateCount(operationCount);
                    break;
                case OPERATION_UPDATE:
                    jobDto.setUpdateCount(operationCount);
                    break;
                case OPERATION_DELETE:
                    jobDto.setDeleteCount(operationCount);
                    break;
                case OPERATION_ACTION:
                    jobDto.setActionCount(operationCount);
                    break;
                case OPERATION_COPY_MO:
                    jobDto.setCopyMoCount(operationCount);
                    break;
                case OPERATION_COPY_ROOT:
                    jobDto.setCopyRootCount(operationCount);
                    break;
                default:
                    break;
            }
        }
    }

    private void ensureAttributesFetched(final ImportOperation importOperation) {
        Hibernate.initialize(importOperation.getSimpleAttributes());
        Hibernate.initialize(importOperation.getComplexAttributes());
    }

    private Timestamp now() {
        return new Timestamp(new Date().getTime());
    }

    public void updateImportSimpleAttribute(final ImportSimpleAttribute importSimpleAttribute) {
        entityManager.merge(importSimpleAttribute);
    }

    /**
     * Holds import job id, its details and the count of different operation types (Create, Update, Delete, Action, Copy MO and Copy Root) performed
     * in that job.
     */
    class ImportJobDto {
        private final long jobId;
        private final AsyncBulkImportJobStatus jobStatus;
        private final ImportServiceSpecification importServiceSpecification;
        private final String fileFormat;
        private final String configuration;
        private final String filePath;
        private final Timestamp startTime;
        private final Timestamp endTime;
        private final Timestamp lastValidationTime;
        private final Integer elapsedTime;
        private final String failureReason;
        private int createCount;
        private int updateCount;
        private int deleteCount;
        private int actionCount;
        private int copyRootCount;
        private int copyMoCount;
        private final String userId;

        ImportJobDto(final Object[] result) {
            jobId = ((BigInteger) result[0]).longValue();
            jobStatus = JobStatusLegacy.fromJobStatusInDb((String) result[1], (String) result[5]);
            importServiceSpecification = (ImportServiceSpecification) deserializeClob(result[2].toString());
            configuration = importServiceSpecification.getConfiguration();
            fileFormat = importServiceSpecification.getFileFormat() != null
                    ? String.valueOf(importServiceSpecification.getFileFormat())
                    : "JSON";
            filePath = importServiceSpecification.getFilePath() != null
                    ? importServiceSpecification.getFilePath()
                    : "/";
            startTime = (Timestamp) result[3];
            endTime = (Timestamp) result[4];
            failureReason = (String) result[5];
            elapsedTime = (Integer) result[8];
            lastValidationTime = (Timestamp) result[9];
            userId = (String) result[10];
        }

        public ImportJobDto getJobDto() {
            return this;
        }

        public long getJobId() {
            return jobId;
        }

        public AsyncBulkImportJobStatus getJobStatus() {
            return jobStatus;
        }

        public String getFileFormat() {
            return fileFormat;
        }

        public String getConfiguration() {
            return configuration;
        }

        public String getFilePath() {
            return filePath;
        }

        public Timestamp getStartTime() {
            return startTime;
        }

        public Timestamp getLastValidationTime() {
            return lastValidationTime;
        }

        public Timestamp getEndTime() {
            return endTime;
        }

        public Integer getElapsedTime() {
            return elapsedTime;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public int getCreateCount() {
            return createCount;
        }

        public void setCreateCount(final int createCount) {
            this.createCount = createCount;
        }

        public int getUpdateCount() {
            return updateCount;
        }

        public void setUpdateCount(final int updateCount) {
            this.updateCount = updateCount;
        }

        public int getDeleteCount() {
            return deleteCount;
        }

        public void setDeleteCount(final int deleteCount) {
            this.deleteCount = deleteCount;
        }

        public int getActionCount() {
            return actionCount;
        }

        public void setActionCount(final int actionCount) {
            this.actionCount = actionCount;
        }

        public int getCopyRootCount() {
            return copyRootCount;
        }

        public void setCopyRootCount(final int count) {
            copyRootCount = count;
        }

        public void setCopyMoCount(final int count) {
            copyMoCount = count;
        }

        public String getUserId() {
            return userId;
        }

        @Override
        public String toString() {
            return "ImportJobDto{"
                    + "jobId=" + jobId
                    + ", jobStatus=" + jobStatus
                    + ", importServiceSpecification=" + importServiceSpecification
                    + ", fileFormat='" + fileFormat + '\''
                    + ", configuration='" + configuration + '\''
                    + ", filePath='" + filePath + '\''
                    + ", startTime=" + startTime
                    + ", lastValidationTime=" + lastValidationTime
                    + ", endTime=" + endTime
                    + ", elapsedTime=" + elapsedTime
                    + ", failureReason='" + failureReason + '\''
                    + ", createCount=" + createCount
                    + ", updateCount=" + updateCount
                    + ", deleteCount=" + deleteCount
                    + ", actionCount=" + actionCount
                    + ", copyRootCount=" + copyRootCount
                    + ", userId=" + userId
                    + '}';
        }
    }
}
