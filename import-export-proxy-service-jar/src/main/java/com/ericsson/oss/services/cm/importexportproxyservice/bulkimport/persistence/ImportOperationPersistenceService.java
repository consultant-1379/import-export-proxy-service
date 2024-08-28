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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationFailure;
import com.ericsson.oss.services.cm.bulkimport.persistence.utils.QueryBuilder;

/**
 * Persistence service for the ImportJob entity.
 */
public class ImportOperationPersistenceService {

    @Inject
    @PersistenceContext(unitName = "importPersistenceUnit")
    EntityManager entityManager;

    public ImportOperation create(final ImportOperation importOperation) {
        entityManager.persist(importOperation);
        entityManager.flush();
        return importOperation;
    }

    public ImportOperation createUnFlushed(final ImportOperation importOperation) {
        entityManager.persist(importOperation);
        return importOperation;
    }

    public void flush() {
        entityManager.flush();
    }

    public ImportOperation update(final ImportOperation importOperation) {
        final ImportOperation result = entityManager.merge(importOperation);
        entityManager.flush();
        return result;
    }

    public ImportOperation getRequired(final Long operationId) {
        final ImportOperation result = entityManager.find(ImportOperation.class, operationId);
        return result;
    }

    public List<ImportOperation> getAll(final Criteria<ImportOperation> criteria) {
        final List<Long> ids = getAllImportOperationIds(criteria);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> query = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = query.from(ImportOperation.class);
        final CriteriaQuery<ImportOperation> criteriaQuery = query
                .where(importOperation.get("id").in(ids))
                .orderBy(criteriaBuilder.asc(importOperation.get("id")));
        final List<ImportOperation> result = entityManager.createQuery(
                criteriaQuery
        ).getResultList();
        return result;
    }

    public Long totalCount(final Criteria<ImportOperation> criteria) {
        final String slq = "SELECT count(*)"
                + " from " + ImportOperation.IMPORT_OPERATION_TABLE
                + " WHERE " + QueryBuilder.CONDITIONS_MARKER;
        final Query query = createNativeQuery(slq, criteria);
        final BigInteger totalCount = (BigInteger) query.getSingleResult();
        return totalCount.longValue();
    }

    public Long getObsoleteOperationCount(final long jobId) {
        final String sqlString = "SELECT count(*)"
                + " from " + ImportOperation.IMPORT_OPERATION_TABLE
                + " WHERE " + ImportOperation.JOB_ID_FIELD + " = " + jobId
                + " AND " + ImportOperation.OPERATION_STATUS_FIELD
                + " IN ('EXECUTION_ERROR', 'VALID')"
                + " AND " + ImportOperation.ID_FIELD + " IN ("
                + " SELECT " + ImportOperationFailure.OPERATION_ID_FIELD
                + " from " + ImportOperationFailure.IMPORT_OPERATION_FAILURE_TABLE
                + " WHERE warning_cause is not null)";
        final BigInteger obsoleteOperationCount = (BigInteger) entityManager.createNativeQuery(sqlString).getSingleResult();
        return obsoleteOperationCount.longValue();
    }

    public List<ImportOperation> getAllImportOperations(final long jobId, final String... importOperationStatuses) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> criteriaQuery = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(importOperation).orderBy(criteriaBuilder.asc(importOperation.get(ImportOperation.ID_FIELD))).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(
                                importOperation.get("jobId"),
                                jobId
                        ),
                        importOperation.get("operationStatus")
                                .in(importOperationStatuses)
                )
        );
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public List<ImportOperation> getAllImportOperations(final long jobId) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportOperation> criteriaQuery = criteriaBuilder.createQuery(ImportOperation.class);
        final Root<ImportOperation> importOperation = criteriaQuery.from(ImportOperation.class);
        criteriaQuery.select(importOperation).orderBy(criteriaBuilder.asc(importOperation.get(ImportOperation.ID_FIELD))).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(
                                importOperation.get("jobId"),
                                jobId
                        ))
        );
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<Long> getAllImportOperationIds(final Criteria<ImportOperation> criteria) {
        final String sql = "SELECT " + ImportOperation.ID_FIELD
                + " FROM " + ImportOperation.IMPORT_OPERATION_TABLE
                + " WHERE " + QueryBuilder.CONDITIONS_MARKER
                + " ORDER BY " + ImportOperation.ID_FIELD + " ASC";
        return createNativeQuery(sql, criteria)
                .setFirstResult(criteria.getOffset())
                .setMaxResults(criteria.getLimit())
                .getResultList();
    }

    private Query createNativeQuery(final String sqlTemplate, final Criteria<ImportOperation> criteria) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        final Map<String, List<Restriction<?>>> restrictions = groupByFieldName(criteria.getRestrictions());
        queryBuilder.withConditions(restrictions.get(ImportOperation.ID_FIELD));
        queryBuilder.withConditions(restrictions.get(ImportOperation.JOB_ID_FIELD));
        queryBuilder.withConditions(restrictions.get(ImportOperation.OPERATION_STATUS_FIELD));
        return queryBuilder.createNativeSql(entityManager, sqlTemplate);
    }

    private Map<String, List<Restriction<?>>> groupByFieldName(final List<Restriction<?>> restrictions) {
        final Map<String, List<Restriction<?>>> groupedRestrictions = QueryBuilder.groupByAttribute(restrictions);
        return groupedRestrictions;
    }

    public static ImportOperationCriteria importOperationCriteria() {
        return new ImportOperationCriteria();
    }

    /**
     * Criteria class for ImportOperation use in Persistence Service.
     */
    public static class ImportOperationCriteria extends Criteria.CriteriaBuilder<ImportOperation, ImportOperationCriteria> {
        public final Field<Long> id = this.field(ImportOperation.ID_FIELD, Long.class);
        public final Field<Long> jobId = this.field(ImportOperation.JOB_ID_FIELD, Long.class);
        public final Field<String> status = this.field(ImportOperation.OPERATION_STATUS_FIELD, String.class);

        ImportOperationCriteria() {
        }
    }
}
