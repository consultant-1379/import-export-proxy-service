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

import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobFailure.IMPORT_JOB_FAILURE_TABLE;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobFailure.OPERATION_ID_FIELD;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobOperation.IMPORT_JOB_OPERATION_TABLE;

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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobFailure;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.utils.QueryBuilder;

/**
 * Persistence service for the ImportJob entity.
 */
public class ImportJobFailurePersistenceService {
    @Inject
    @PersistenceContext(unitName = "importPersistenceUnit")
    EntityManager entityManager;

    public List<ImportJobFailure> getAll(final Criteria<ImportJobFailure> criteria) {
        final List<Long> ids = getIds(criteria);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ImportJobFailure> query = criteriaBuilder.createQuery(ImportJobFailure.class);
        final Root<ImportJobFailure> importJobFailure = query.from(ImportJobFailure.class);

        final List<Order> orderList = new ArrayList<>();
        orderList.add(criteriaBuilder.asc(importJobFailure.get("lineNumber")));
        orderList.add(criteriaBuilder.asc(importJobFailure.get("id")));

        final CriteriaQuery<ImportJobFailure> criteriaQuery = query
                .where(importJobFailure.get("id").in(ids))
                .orderBy(orderList);
        final List<ImportJobFailure> result = entityManager.createQuery(criteriaQuery).getResultList();
        return result;
    }

    public Long totalCount(final Criteria<ImportJobFailure> criteria) {
        final String slq = "SELECT count(*)"
                + " FROM " + IMPORT_JOB_FAILURE_TABLE
                + " JOIN " + IMPORT_JOB_OPERATION_TABLE
                + "  ON " + IMPORT_JOB_OPERATION_TABLE + "." + ImportJobOperation.ID_FIELD + "=" + IMPORT_JOB_FAILURE_TABLE + "." + OPERATION_ID_FIELD
                + " WHERE " + QueryBuilder.CONDITIONS_MARKER;
        final Query query = createNativeQuery(slq, criteria);
        final BigInteger totalCount = (BigInteger) query.getSingleResult();
        return totalCount.longValue();
    }

    @SuppressWarnings("unchecked")
    private List<Long> getIds(final Criteria<ImportJobFailure> criteria) {
        final String sql = "SELECT " + IMPORT_JOB_FAILURE_TABLE + "." + ImportJobFailure.ID_FIELD
                + " FROM " + IMPORT_JOB_FAILURE_TABLE
                + " JOIN " + IMPORT_JOB_OPERATION_TABLE
                + "  ON " + IMPORT_JOB_OPERATION_TABLE + "." + ImportJobOperation.ID_FIELD + "=" + IMPORT_JOB_FAILURE_TABLE + "." + OPERATION_ID_FIELD
                + " WHERE " + QueryBuilder.CONDITIONS_MARKER
                + " ORDER BY " + IMPORT_JOB_FAILURE_TABLE + "." + ImportJobFailure.ID_FIELD + " ASC";
        return createNativeQuery(sql, criteria)
                .setFirstResult(criteria.getOffset())
                .setMaxResults(criteria.getLimit())
                .getResultList();
    }

    private Query createNativeQuery(final String sqlTemplate, final Criteria<ImportJobFailure> criteria) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        final Map<String, List<Restriction<?>>> restrictions = groupByFieldName(criteria.getRestrictions());
        queryBuilder.withConditions(restrictions.get(IMPORT_JOB_OPERATION_TABLE + "." + ImportJobOperation.JOB_ID_FIELD));
        return queryBuilder.createNativeSql(entityManager, sqlTemplate);
    }

    private Map<String, List<Restriction<?>>> groupByFieldName(final List<Restriction<?>> restrictions) {
        final Map<String, List<Restriction<?>>> groupedRestrictions = QueryBuilder.groupByAttribute(restrictions);
        return groupedRestrictions;
    }

    public static ImportJobFailureCriteria importJobFailureCriteria() {
        return new ImportJobFailureCriteria();
    }

    /**
     * Criteria class for ImportJobFailure use in Persistence Service.
     */
    public static class ImportJobFailureCriteria extends Criteria.CriteriaBuilder<ImportJobFailure, ImportJobFailureCriteria> {
        public final Field<Long> jobId = field(IMPORT_JOB_OPERATION_TABLE + "." + ImportJobOperation.JOB_ID_FIELD, Long.class);

        ImportJobFailureCriteria() {
        }
    }
}
