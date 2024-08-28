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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.persistence;

import static com.ericsson.oss.services.cm.bulkimport.api.domain.JobType.IMPORT_NBI_V2;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.INPROGRESS;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.MAX_JOB_COUNT_FOR_RETRIEVAL;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob.IMPORT_JOB_TABLE;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation.IMPORT_OPERATION_TABLE;
import static com.google.common.collect.Lists.transform;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.JobExport;
import com.ericsson.oss.services.cm.bulkimport.persistence.utils.QueryBuilder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * Persistence service for the ImportJob entity.
 */
public class ImportJobPersistenceService {
    private static final String NBI_V2 = IMPORT_NBI_V2.getType();
    @Inject
    @PersistenceContext(unitName = "importPersistenceUnit")
    EntityManager entityManager;

    public ImportJob create(final ImportJob importJob) {
        entityManager.persist(importJob);
        entityManager.flush();
        return importJob;
    }

    public ImportJob update(final ImportJob importJob) {
        importJob.onStore();
        final ImportJob result = entityManager.merge(importJob);
        entityManager.flush();
        return result;
    }

    public ImportJob getImportJob(final Long jobId) {
        final ImportJob impJob = entityManager.find(ImportJob.class, jobId);
        if (impJob != null && impJob.getJobType() == IMPORT_NBI_V2) {
            return impJob;
        }
        return null;
    }

    public void deleteJob(final ImportJob importJob) {
        entityManager.remove(importJob);
        entityManager.flush();
    }

    public List<ImportJob> getAllJobs(final Criteria<ImportJob> criteria) {
        final List<Long> ids = hasErrorFilter(criteria)
                ? getAllImportJobWithErrorIds(criteria)
                : getAllImportJobIds(criteria);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportJob> query = criteriaBuilder.createQuery(ImportJob.class);
        final Root<ImportJob> importJob = query.from(ImportJob.class);
        final CriteriaQuery<ImportJob> criteriaQuery = query
                .where(importJob.get("id").in(ids))
                .orderBy(getOrder(criteria, criteriaBuilder, importJob));
        return entityManager.createQuery(criteriaQuery).setMaxResults(MAX_JOB_COUNT_FOR_RETRIEVAL).getResultList();
    }

    public Long totalCount(final Criteria<ImportJob> criteria) {
        if (hasErrorFilter(criteria)) {
            return this.totalCountWithError(criteria);
        } else {
            final String sql = MessageFormat.format(
                    "SELECT count(*) FROM {0} AS {1} WHERE {2} AND {1}.{3}=''{4}''",
                    IMPORT_JOB_TABLE, IMPORT_JOB_TABLE,
                    QueryBuilder.CONDITIONS_MARKER,
                    ImportJob.JOB_TYPE, NBI_V2);
            final Query query = createNativeQuery(sql, criteria);
            final BigInteger totalCount = (BigInteger) query.getSingleResult();
            return totalCount.longValue();
        }
    }

    /**
     * Creates an entity record in import job export status table as and when an export request is triggered.
     *
     * @param importJobId import job Id value.
     * @return import job export id value.
     */
    public long createNewJobExport(final long importJobId) {
        final JobExport jobExport = new JobExport();
        jobExport.setImportJobId(importJobId);
        jobExport.setStatus(INPROGRESS);
        jobExport.setFilePath("");
        entityManager.persist(jobExport);
        entityManager.flush();
        return jobExport.getJobExportId();
    }

    /**
     * Updates status value to a given import job export record.
     *
     * @param importJobId     import job id.
     * @param jobExportId     ID of the import job export process.
     * @param jobExportStatus Status to be updated for a given import job export record.
     * @param filePath        File path of the exported file.
     */
    public void setJobExportStatus(final long importJobId, final long jobExportId, final String jobExportStatus, final String filePath) {
        final JobExport jobExport = getJobExport(importJobId, jobExportId);
        if (jobExport != null) {
            jobExport.setStatus(jobExportStatus);
            jobExport.setFilePath(filePath);
            entityManager.merge(jobExport);
            entityManager.flush();
        }
    }

    /**
     * Queries and returns value of import job export status.
     *
     * @param jobExportId ID of import job export process
     * @return export status of an import job.
     */
    public JobExport getJobExport(final Long importJobId, final Long jobExportId) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JobExport> query = criteriaBuilder.createQuery(JobExport.class);
        final Root<JobExport> from = query.from(JobExport.class);
        query.select(from).where(criteriaBuilder.and(
                criteriaBuilder.equal(from.get("jobExportId"), jobExportId),
                criteriaBuilder.equal(from.get("importJobId"), importJobId))
        );
        final List<JobExport> resultList = entityManager.createQuery(query).getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    private Order getOrder(final Criteria<ImportJob> criteria, final CriteriaBuilder criteriaBuilder, final Root<ImportJob> importJob) {
        String sortParameter = "timeStart";
        Order sortOrder = criteriaBuilder.desc(importJob.get(sortParameter));
        if (null != criteria.getSortBy() && null != criteria.getOrderBy()) {
            switch (criteria.getSortBy()) {
                case "lastValidationStart":
                    sortParameter = "lastValidationStartTime";
                    break;
                case "lastValidation":
                    sortParameter = "lastValidationTime";
                    break;
                case "lastExecutionStart":
                    sortParameter = "lastExecutionStartTime";
                    break;
                case "lastExecution":
                    sortParameter = "timeEnd";
                    break;
                default:
                    sortParameter = "timeStart";
            }

            if ("ascending".equals(criteria.getOrderBy())) {
                sortOrder = criteriaBuilder.asc(importJob.get(sortParameter));
            } else {
                sortOrder = criteriaBuilder.desc(importJob.get(sortParameter));
            }
        }
        return sortOrder;
    }

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    private Long totalCountWithError(final Criteria<ImportJob> criteria) {
        final String whereCondtions = MessageFormat.format("{0} AND {1}.{2}=''{3}''",
                QueryBuilder.CONDITIONS_MARKER,
                IMPORT_JOB_TABLE, ImportJob.JOB_TYPE, NBI_V2);
        final String sql = MessageFormat.format(
                " SELECT COUNT (DISTINCT {2}.{0} ) FROM {1} AS {2} INNER JOIN {3} AS {4} ON {4}.{5}={2}.{6} WHERE {7}",
                ImportJob.ID_FIELD,
                IMPORT_JOB_TABLE, IMPORT_JOB_TABLE,
                IMPORT_OPERATION_TABLE, IMPORT_OPERATION_TABLE,
                ImportOperation.JOB_ID_FIELD, ImportJob.ID_FIELD,
                whereCondtions);
        final Query query = createNativeQuery(sql, criteria);
        final BigInteger totalCount = (BigInteger) query.getSingleResult();
        return totalCount.longValue();
    }

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    private List<Long> getAllImportJobWithErrorIds(final Criteria<ImportJob> criteria) {
        final String whereCondtions = MessageFormat.format("{0} AND {1}.{2}=''{3}''",
                QueryBuilder.CONDITIONS_MARKER,
                IMPORT_JOB_TABLE, ImportJob.JOB_TYPE, NBI_V2);
        final String sql = MessageFormat.format(
                "SELECT DISTINCT {3}.{0},{3}.{1} FROM {2} AS {3} INNER JOIN {4} AS {5} ON {5}.{6}={3}.{7} WHERE {8} ORDER BY {3}.{9} DESC",
                ImportJob.ID_FIELD, ImportJob.TIME_START_FIELD,
                IMPORT_JOB_TABLE, IMPORT_JOB_TABLE,
                IMPORT_OPERATION_TABLE, IMPORT_OPERATION_TABLE,
                ImportOperation.JOB_ID_FIELD, ImportJob.ID_FIELD,
                whereCondtions,
                ImportJob.TIME_START_FIELD
        );
        final List idsResult = createNativeQuery(sql, criteria)
                .setFirstResult(criteria.getOffset())
                .setMaxResults(criteria.getLimit())
                .getResultList();
        if (idsResult.isEmpty()) {
            return new ArrayList<>();
        }
        final List<Long> ids = transform(idsResult, new Function<Object[], Long>() {
            @Override
            public Long apply(final Object[] o) {
                return ((BigInteger) o[0]).longValue();
            }
        });
        return ids;
    }

    @SuppressWarnings("unchecked")
    private List<Long> getAllImportJobIds(final Criteria<ImportJob> criteria) {
        final String sql = MessageFormat.format(
                "SELECT {2}.{0} FROM {1} AS {2} WHERE {3} AND {2}.{4}=''{5}'' ORDER BY {2}.{6} DESC",
                ImportJob.ID_FIELD,
                IMPORT_JOB_TABLE, IMPORT_JOB_TABLE,
                QueryBuilder.CONDITIONS_MARKER,
                ImportJob.JOB_TYPE, NBI_V2,
                ImportJob.TIME_START_FIELD);
        return createNativeQuery(sql, criteria)
                .setFirstResult(criteria.getOffset())
                .setMaxResults(criteria.getLimit())
                .getResultList();
    }

    private Query createNativeQuery(final String sqlTemplate, final Criteria<ImportJob> criteria) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        final Map<String, List<Restriction<?>>> restrictions = groupByFieldName(criteria.getRestrictions());
        queryBuilder.withConditions(restrictions.get(ImportJobCriteria.ID_FIELD));
        queryBuilder.withConditions(restrictions.get(ImportJobCriteria.TIME_START_FIELD));
        queryBuilder.withConditions(restrictions.get(ImportJobCriteria.USERNAME_FIELD));
        queryBuilder.withConditions(restrictions.get(ImportJobCriteria.JOB_NAME_FIELD));
        queryBuilder.withConditions(nestedConditions(restrictions));
        queryBuilder.withConditions(restrictions.get(ImportJobCriteria.OPERATION_STATUS_FIELD));
        return queryBuilder.createNativeSql(entityManager, sqlTemplate);
    }

    private Map<String, List<Restriction<?>>> groupByFieldName(final List<Restriction<?>> restrictions) {
        return QueryBuilder.groupByAttribute(restrictions);
    }

    private List<Restriction<?>> nestedConditions(final Map<String, List<Restriction<?>>> restrictions) {
        final List<Restriction<?>> nestedConditions = new ArrayList<>();
        for (final Map.Entry<String, List<Restriction<?>>> entry : restrictions.entrySet()) {
            if (entry.getKey().isEmpty() && entry.getValue() != null) {
                nestedConditions.addAll(Collections2.filter(
                        entry.getValue(),
                        new Predicate<Restriction<?>>() {
                            @Override
                            public boolean apply(final Restriction<?> restriction) {
                                return restriction != null && isNestedCondition(restriction);
                            }
                        }));
            }
        }
        return nestedConditions;
    }

    private boolean isNestedCondition(final Restriction<?> restriction) {
        return restriction.getOperator() == Restriction.Operator.ALL_OF
                || restriction.getOperator() == Restriction.Operator.ANY_OF;
    }

    private boolean hasErrorFilter(final Criteria<ImportJob> criteria) {
        return Iterables.tryFind(criteria.getRestrictions(), new Predicate<Restriction<?>>() {
            @Override
            public boolean apply(final Restriction<?> restriction) {
                final String attribute = restriction.getAttribute();
                return ImportJobCriteria.OPERATION_STATUS_FIELD.equalsIgnoreCase(attribute);
            }
        }).isPresent();
    }

    public static ImportJobCriteria importJobCriteria() {
        return new ImportJobCriteria();
    }

    /**
     * Criteria class for ImportJob use in Persistence Service.
     */
    public static class ImportJobCriteria extends Criteria.CriteriaBuilder<ImportJob, ImportJobCriteria> {
        static final String ID_FIELD = IMPORT_JOB_TABLE + "." + ImportJob.ID_FIELD;
        static final String TIME_START_FIELD = IMPORT_JOB_TABLE + "." + ImportJob.TIME_START_FIELD;
        static final String USERNAME_FIELD = IMPORT_JOB_TABLE + "." + ImportJob.USERNAME_FIELD;
        static final String JOB_NAME_FIELD = IMPORT_JOB_TABLE + "." + ImportJob.JOB_NAME;
        static final String OPERATION_STATUS_FIELD = IMPORT_OPERATION_TABLE + "." + ImportOperation.OPERATION_STATUS_FIELD;

        public final Field<Long> id = this.field(ID_FIELD, Long.class);
        public final Field<Timestamp> timeStart = this.field(TIME_START_FIELD, Timestamp.class);
        public final Field<String> username = this.field(USERNAME_FIELD, String.class);
        public final Field<String> jobName = this.field(JOB_NAME_FIELD, String.class);
        public final Field<String> operationStatus = this.field(OPERATION_STATUS_FIELD, String.class);

        protected ImportJobCriteria() {
        }
    }
}
