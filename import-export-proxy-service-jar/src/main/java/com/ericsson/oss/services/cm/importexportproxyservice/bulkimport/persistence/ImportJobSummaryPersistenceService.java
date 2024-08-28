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

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.cm.bulkimport.api.criteria.Criteria;
import com.ericsson.oss.services.cm.bulkimport.api.criteria.Restriction;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJobOperationSummary;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.OperationTypeV2;
import com.ericsson.oss.services.cm.bulkimport.persistence.utils.QueryBuilder;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Persistence service for the ImportJobOperationSummary entity.
 */
public class ImportJobSummaryPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportJobSummaryPersistenceService.class);

    @Inject
    @PersistenceContext(unitName = "importPersistenceUnit")
    EntityManager entityManager;

    public List<ImportJobOperationSummary> getPerOperationType(final Criteria<ImportOperation> criteria) {
        final List<Long> ids = getAllImportOperationIds(criteria);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return getPerOperationType(ids);
    }

    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    private List<ImportJobOperationSummary> getPerOperationType(final List<Long> ids) {
        final List<ImportJobOperationSummary> summaries = new ArrayList<>();
        final String sql =
                "SELECT " + ImportOperation.JOB_ID_FIELD
                        + "," + ImportOperation.OPERATION_TYPE_FIELD
                        + "," + ImportOperation.OPERATION_STATUS_FIELD
                        + ", count(*)"
                        + " FROM " + ImportOperation.IMPORT_OPERATION_TABLE
                        + " WHERE " + ImportOperation.ID_FIELD + " in (" + QueryBuilder.CONDITIONS_MARKER + ")"
                        + " GROUP BY "
                        + " " + ImportOperation.JOB_ID_FIELD
                        + "," + ImportOperation.OPERATION_TYPE_FIELD
                        + "," + ImportOperation.OPERATION_STATUS_FIELD
                        + " ORDER BY " + ImportOperation.JOB_ID_FIELD;
        for (final List<Long> partitionedIds : Lists.partition(ids, 500)) {
            final String in = Joiner.on(",").join(partitionedIds);
            final String summarySql = sql.replace(QueryBuilder.CONDITIONS_MARKER, in);
            final List<?> resultList = entityManager.createNativeQuery(summarySql).getResultList();
            for (final Object objSummary : resultList) {
                final ImportJobOperationSummary newSummary = summary((Object[]) objSummary);
                final Boolean updated = updateExistingSummaries(summaries, newSummary);
                if (!updated) {
                    summaries.add(newSummary);
                }
            }
        }
        return summaries;
    }

    @SuppressWarnings("unchecked")
    private List<Long> getAllImportOperationIds(final Criteria<ImportOperation> criteria) {
        final String sql = "SELECT " + ImportOperation.ID_FIELD
                + " FROM " + ImportOperation.IMPORT_OPERATION_TABLE
                + " WHERE " + QueryBuilder.CONDITIONS_MARKER
                + " ORDER BY " + ImportOperation.ID_FIELD + " DESC";
        return createNativeQuery(sql, criteria)
                .setFirstResult(criteria.getOffset())
                .setMaxResults(criteria.getLimit())
                .getResultList();
    }

    private Query createNativeQuery(final String sqlTemplate, final Criteria<ImportOperation> criteria) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        final Map<String, List<Restriction<?>>> restrictions = groupByFieldName(criteria.getRestrictions());
        queryBuilder.withConditions(restrictions.get(ImportOperation.JOB_ID_FIELD));
        return queryBuilder.createNativeSql(entityManager, sqlTemplate);
    }

    private Map<String, List<Restriction<?>>> groupByFieldName(final List<Restriction<?>> restrictions) {
        final Map<String, List<Restriction<?>>> groupedRestrictions = QueryBuilder.groupByAttribute(restrictions);
        return groupedRestrictions;
    }

    private ImportJobOperationSummary summary(final Object[] resultSet) {
        final BigInteger jobId = (BigInteger) resultSet[0];
        final OperationTypeV2 operationType = operationType(resultSet[1]);
        final String operationStatus = (String) resultSet[2];
        final BigInteger count = (BigInteger) resultSet[3];
        return new ImportJobOperationSummary(jobId.longValue(), operationType, operationStatus, count.intValue());
    }

    private OperationTypeV2 operationType(final Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                final int ordinal = ((Number) value).intValue();
                return OperationTypeV2.values()[ordinal];
            }
            if (value instanceof String) {
                final String str = (String) value;
                if (NumberUtils.isCreatable(str)) {
                    final int ordinal = NumberUtils.createInteger(str);
                    return OperationTypeV2.values()[ordinal];
                }
                for (final OperationTypeV2 operationType : OperationTypeV2.values()) {
                    if (operationType.name().equalsIgnoreCase(str) || operationType.getMessage().equalsIgnoreCase(str)) {
                        return operationType;
                    }
                }
            }
        } catch (final Exception ignored) {
            LOGGER.error("Exception occurred in operationType", ignored);
        }
        return null;
    }

    private static Boolean updateExistingSummaries(final List<ImportJobOperationSummary> summaries, final ImportJobOperationSummary newSummary) {
        for (final ImportJobOperationSummary summary : summaries) {
            if (summary.getOperationType().equals(newSummary.getOperationType())
                    && summary.getOperationStatus().equalsIgnoreCase(newSummary.getOperationStatus())
                    && summary.getJobId().equals(newSummary.getJobId())) {
                final int count = summary.getCount() + newSummary.getCount();
                summary.setCount(count);
                return true;
            }
        }
        return false;
    }
}
