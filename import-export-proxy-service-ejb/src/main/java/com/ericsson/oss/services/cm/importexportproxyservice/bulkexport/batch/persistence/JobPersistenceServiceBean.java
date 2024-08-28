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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.persistence;

import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.batch.runtime.BatchStatus;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.DeleteOptions;
import com.ericsson.oss.itpf.datalayer.dps.async.DeleteOperations;
import com.ericsson.oss.itpf.datalayer.dps.async.DeleteResult;
import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.exception.general.InvalidConfigurationException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.LockablePersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.*;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.services.cm.export.api.ExportJobExecutionEntity;
import com.ericsson.oss.services.cm.export.transformer.api.ProcessResult;
import com.ericsson.oss.services.cm.export.transformer.api.TransformationStatus;
import com.ericsson.oss.services.cm.export.transformer.api.TransformerDataCategory;
import com.ericsson.oss.services.cm.export.transformer.api.TransformerUserFilter;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Timed;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.retry.DpsRetryPolicies;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.userfilter.UserFilterUtilitiesBean;

/**
 * Implementation of the {@code JobPersistenceService}.
 */
@Stateless
public class JobPersistenceServiceBean implements JobPersistenceService {

    private static final String CONDENSE_NODES = "condense_nodes";
    private static final String LIVE_BUCKET = "Live";

    @EServiceRef
    private DataPersistenceService dataPersistenceService;

    @Inject
    private UserFilterUtilitiesBean userFilterUtilitiesBean;

    @Inject
    private RetryManager retryManager;

    @Inject
    private DpsRetryPolicies retryPolicy;

    @Inject
    private Logger logger;

    private PersistenceObject findJobInputPoByIdForReadOnlyAccess(final long poId) {
        final PersistenceObject persistenceObject = dataPersistenceService.getDataBucket(LIVE_BUCKET, CONDENSE_NODES).findPoById(poId);
        validatePo(persistenceObject, poId);
        return persistenceObject;
    }

    @Override
    public PersistenceObject findPoById(final long poId) {
        return dataPersistenceService.getLiveBucket().findPoById(poId);
    }

    @Override
    public String[] getNodesToExportFromJobInputPo(final long poId) {
        final List<String> nodes = getNodesToExportListFromJobInputPo(poId);
        return nodes.toArray(new String[0]);
    }

    @Override
    public List<String> getNodesToExportListFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAttribute(ATTR_NODES_TO_EXPORT);
    }

    @Override
    public List<String> findPartitionedNodes(final long poId, final int partitionIndex) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        final Map<Integer, Object> partitionedNodeMap = jobInputPo.getAttribute(ATTR_PARTITIONED_MAP);
        final Map<String, Object> partition = (Map<String, Object>) partitionedNodeMap.get(partitionIndex);
        return (List<String>) partition.get(MEMBER_PARTITIONED_NODES);
    }

    @Override
    public Map<String, Object> findPartitionedNodesForSlaveMap(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAttribute(ATTR_PARTITIONED_MAP);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long createJobInputPo(final Map<String, Object> attributes) {
        final PersistenceObject persistenceObject = dataPersistenceService.getLiveBucket().getPersistenceObjectBuilder()
                .namespace(NAMESPACE_BATCH).version(VERSION_JOB_INPUT).type(TYPE_JOB_INPUT).addAttributes(attributes).create();
        return persistenceObject.getPoId();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long createMasterJobInputPo(final Map<String, Object> attributes) {
        final PersistenceObject persistenceObject = dataPersistenceService.getLiveBucket().getPersistenceObjectBuilder()
                .namespace(NAMESPACE_BATCH).version(VERSION_JOB_INPUT).type(TYPE_MASTER_EXPORT_JOB_INPUT).addAttributes(attributes).create();
        return persistenceObject.getPoId();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateJobInputPartitionedNodesAttribute(final long poId, final long jobId, final Map<Integer, Object> partitionedNodesMap) {
        final PersistenceObject jobInputPo = dataPersistenceService.getLiveBucket().findPoById(poId);
        validatePo(jobInputPo, poId);
        jobInputPo.setAttribute(ATTR_PARTITIONED_MAP, partitionedNodesMap);
        jobInputPo.setAttribute(ATTR_JOB_ID, jobId);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateSkipNodeExportResultPo(final Map<String, Object> attributes, final long nodeExportResultPoid) {
        final PersistenceObject nodeExportResultPo = dataPersistenceService.getLiveBucket().findPoById(nodeExportResultPoid);
        validatePo(nodeExportResultPo, nodeExportResultPoid);
        updateNodeExportResultPo(nodeExportResultPo, attributes);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateMasterJobInput(final long poId, final long jobId, final Map<String, Object> partitionedNodesMap,
            final Map<String, Object> nodeSizeCategoryCountPerSlave, final Map<String, Object> masterNodeSizeCategoryCount) {
        final PersistenceObject masterJobInputPo = dataPersistenceService.getLiveBucket().findPoById(poId);
        validatePo(masterJobInputPo, poId);
        masterJobInputPo.setAttribute(ATTR_PARTITIONED_MAP, partitionedNodesMap);
        masterJobInputPo.setAttribute(ATTR_NODE_SIZE_CATEGORY_COUNT_PER_SLAVE, nodeSizeCategoryCountPerSlave);
        masterJobInputPo.setAttribute(ATTR_NODE_SIZE_CATEGORY_COUNT, masterNodeSizeCategoryCount);
        masterJobInputPo.setAttribute(ATTR_JOB_ID, jobId);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long createNodeExportResultPo(final Map<String, Object> attributes) {
        final PersistenceObject persistenceObject = dataPersistenceService.getLiveBucket().getPersistenceObjectBuilder()
                .namespace(NAMESPACE_BATCH).version(VERSION_NODE_EXPORT_RESULT).type(TYPE_NODE_EXPORT_RESULT).addAttributes(attributes).create();
        return persistenceObject.getPoId();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long createJobOutputPo(final Map<String, Object> attributes) {
        final PersistenceObject persistenceObject = dataPersistenceService.getLiveBucket().getPersistenceObjectBuilder()
                .namespace(NAMESPACE_BATCH).version(VERSION_JOB_OUTPUT).type(TYPE_JOB_OUTPUT).addAttributes(attributes).create();
        return persistenceObject.getPoId();
    }

    @Override
    public List<PersistenceObject> findNodeExportResultPosByJobId(final long jobId) {
        return findNodeExportResults(jobId);
    }

    private List<PersistenceObject> findNodeExportResults(final long jobId) {
        final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(NAMESPACE_BATCH, TYPE_NODE_EXPORT_RESULT);

        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(ATTR_JOB_ID, jobId);
        typeQuery.setRestriction(restriction);

        return executeQuery(dataPersistenceService.getLiveBucket(), typeQuery);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private int findAndDeleteNodeExportResults(final Long jobId) {
        final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(NAMESPACE_BATCH, TYPE_NODE_EXPORT_RESULT);

        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(ATTR_JOB_ID, jobId);
        typeQuery.setRestriction(restriction);
        final DeleteOperations dpsFacade = getDeleteOperations();
        final int totalDeleted =
                retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<Integer>() {
                    @Override
                    public Integer execute(final RetryContext retryContext) throws Exception {
                        try {
                            final Future<DeleteResult> deleteResult =
                                    dpsFacade.deleteFromQuery("Live", typeQuery, DeleteOptions.deleteWithNoEvents());
                            final DeleteResult extractedResult = deleteResult.get();
                            switch (extractedResult.getResult().toString()) {
                                case "SUCCEEDED":
                                    final int deletionCount = extractedResult.getNumberOfObjectsDeleted();
                                    logger.debug("SUCCEEDED Removed a total of [{}] NodeExportResults for jobID: [{}]",
                                            deletionCount, jobId);
                                    return deletionCount;
                                case "FAILED_TRANSIENT":
                                case "PARTIALLY_COMPLETED":
                                    final String warnMsg = String.format(
                                            "Delete Operation for jobID: [%d] completed in either FAILED_TRANSIENT or "
                                                    + "PARTIALLY COMPLETED state due to [%s]. Operation will be retried.",
                                            jobId, extractedResult.getFailureDetails());
                                    logger.warn(warnMsg);
                                    throw new DpsPersistenceException(new Throwable(warnMsg));
                                case "FAILED_FATAL":
                                    final String errorMsg = String.format(
                                            "Delete Operation for jobId: [%d] completed in FAILED_FATAL state due to [%s]."
                                                    + " Operation will not be retried.",
                                            jobId, extractedResult.getFailureDetails());
                                    logger.error(errorMsg);
                                    return -1;
                                default:
                                    break;
                            }
                        } catch (final InterruptedException e) {
                            logger.error("Delete Operation failed with InterruptedException with cause: [{}]", e.getCause());
                        } catch (final ExecutionException e) {
                            logger.error("Delete Operation failed with ExecutionException with cause: [{}]", e.getCause());
                        } catch (final RetriableCommandException e) {
                            throw retryPolicy.getException(e);
                        }
                        return 0;
                    }
                });
        if (totalDeleted < 0) { /*Delete Result is FAILED FATAL and therefore and exception should be thrown*/
            throw new DeletionFromQueryFailedException(String.format("Delete Operation for jobId: [%d] completed in FAILED_FATAL state", jobId));
        }
        return totalDeleted;
    }

    public DeleteOperations getDeleteOperations() {
        try {
            final InitialContext initialContext = new InitialContext();
            final Object obj = initialContext.lookup(DeleteOperations.JNDI_LOOKUP_NAME);
            return (DeleteOperations) obj;
        } catch (final NamingException e) {
            throw new InvalidConfigurationException("Unable to find implementation of DeleteOperations interface.", e);
        }
    }

    private PersistenceObject findNodeExportResult(final long jobId, final String nodename, final String datacategory) {
        final List<PersistenceObject> persistenceObjects =
                retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<List<PersistenceObject>>() {
                    @Override
                    public List<PersistenceObject> execute(final RetryContext retryContext) throws Exception {
                        try {
                            final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
                            final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(NAMESPACE_BATCH, TYPE_NODE_EXPORT_RESULT);
                            final TypeRestrictionBuilder rb = typeQuery.getRestrictionBuilder();
                            final Restriction restriction1 = rb.equalTo(ATTR_JOB_ID, jobId);
                            final Restriction restriction2 = rb.equalTo(ATTR_NODE_NAME, nodename);
                            final Restriction restriction3 = rb.equalTo(ATTR_DATA_CATEGORY, datacategory);
                            final Restriction restrictions = rb.allOf(restriction1, restriction2, restriction3);

                            typeQuery.setRestriction(restrictions);
                            return executeQuery(dataPersistenceService.getLiveBucket(), typeQuery);
                        } catch (final RetriableCommandException e) {
                            logger.error(retryPolicy.getException(e).getMessage());
                            throw retryPolicy.getException(e);
                        }
                    }
                });
        if (!persistenceObjects.isEmpty()) {
            return persistenceObjects.get(0);
        } else {
            return null;
        }
    }

    @Override
    public PersistenceObject findJobOutputPoByJobId(final long jobId) {
        return retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<PersistenceObject>() {
            @Override
            public PersistenceObject execute(final RetryContext retryContext) throws Exception {
                try {
                    return findJobOutput(jobId);
                } catch (final RetriableCommandException e) {
                    logger.error(retryPolicy.getException(e).getMessage());
                    throw retryPolicy.getException(e);
                }
            }
        });
    }

    private PersistenceObject findJobOutput(final long jobId) {
        final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(NAMESPACE_BATCH, TYPE_JOB_OUTPUT);

        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(ATTR_JOB_ID, jobId);
        typeQuery.setRestriction(restriction);

        final List<PersistenceObject> persistenceObjects = executeQuery(dataPersistenceService.getLiveBucket(), typeQuery);
        if (!persistenceObjects.isEmpty()) {
            return persistenceObjects.get(0);
        } else {
            return null;
        }
    }

    private List<PersistenceObject> executeQuery(final DataBucket config, final Query<? extends RestrictionBuilder> query) {
        final QueryExecutor queryExecutor = config.getQueryExecutor();
        final Iterator<PersistenceObject> poIterator = queryExecutor.execute(query);
        final List<PersistenceObject> persistenceObjects = new ArrayList<>();
        while (poIterator.hasNext()) {
            final PersistenceObject persistenceObject = poIterator.next();
            persistenceObjects.add(persistenceObject);
        }
        return persistenceObjects;
    }

    @Override
    public List<ProcessResult> mapNodeExportResultPosToProcessResults(final long jobId) {
        final List<ProcessResult> processResults = new ArrayList<>();
        final List<PersistenceObject> nodeExportResultPos = findNodeExportResultPosByJobId(jobId);

        for (final PersistenceObject nodeExportResultPo : nodeExportResultPos) {
            final ProcessResult processResult = new ProcessResult();
            final Long jobIdAttr = nodeExportResultPo.getAttribute(ATTR_JOB_ID);
            final String fdnAttr = nodeExportResultPo.getAttribute(ATTR_FDN);
            final String transformationStatusAttr = nodeExportResultPo.getAttribute(ATTR_TRANSFORMATION_STATUS);
            final String statusMessageAttr = nodeExportResultPo.getAttribute(ATTR_STATUS_MESSAGE);
            final String fileNameAttr = nodeExportResultPo.getAttribute(ATTR_FILE_NAME);
            final long numberOfMos = nodeExportResultPo.getAttribute(ATTR_NUMBER_OF_MOS);
            final String nodeRootFdn = nodeExportResultPo.getAttribute(ATTR_NODE_ROOT_FDN);
            final String dataCategory = (String) nodeExportResultPo.getAttribute(ATTR_DATA_CATEGORY);
            final long dpsReadDuration = nodeExportResultPo.getAttribute(ATTR_DPS_READ_DURATION);
            if (dataCategory != null) {
                processResult.setDataCategory(TransformerDataCategory.valueOf(dataCategory));
            }
            processResult.setJobId(jobIdAttr);
            processResult.setFdn(fdnAttr);
            processResult.setState(TransformationStatus.valueOf(transformationStatusAttr));
            processResult.setStatusMessage(statusMessageAttr);
            processResult.setFileName(fileNameAttr);
            processResult.setNumberOfMos(numberOfMos);
            processResult.setDpsReadDuration(dpsReadDuration);
            if (nodeRootFdn != null) {
                processResult.setNodeRootFdn(nodeRootFdn);
            }
            processResults.add(processResult);
        }
        return processResults;
    }

    @Override
    public String getExportTypeFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAttribute(ATTR_EXPORT_TYPE);
    }

    @Override
    public String getConfigNameFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAttribute(ATTR_CONFIG_NAME);
    }

    @Override
    public List<String> getMosToFilterFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        final List<String> moListToFilter = jobInputPo.getAttribute(ATTR_MANAGED_OBJECTS_TO_FILTER);
        if (moListToFilter == null) {
            return new ArrayList<>(0);
        }
        return moListToFilter;
    }

    @Override
    public boolean getEnumTranslateFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAttribute(ATTR_ENUM_TRANSLATE);
    }

    @Override
    public Map<String, Object> getAllAtributesFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAllAttributes();
    }

    @Override
    public Map<String, Object> getFilterPoFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAttribute(ATTR_EXPORT_FILTER);
    }

    @Override
    public Map<String, Object> getUserFilterPoFromJobInputPo(final long poId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(poId);
        return jobInputPo.getAttribute(ATTR_USER_FILTER);
    }

    @Override
    public List<Map<String, Object>> getUserFilterMoSpecificationsFromJobInputPo(final long poId) {
        final Map<String, Object> userFilterMap = getUserFilterPoFromJobInputPo(poId);
        if (userFilterMap == null) {
            return Collections.emptyList();
        }
        return (List<Map<String, Object>>) userFilterMap.get(MEMBER_MO_SPECIFICATIONS_LIST);
    }

    @Override
    public void updateMoSpecificationsForUserFilterPo(final long poId, final List<Map<String, Object>> moSpecifications) {
        final DataBucket dataBucket = retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<DataBucket>() {
            @Override
            public DataBucket execute(final RetryContext retryContext) throws Exception {
                try {
                    return dataPersistenceService.getLiveBucket();
                } catch (final RetriableCommandException e) {
                    logger.error(retryPolicy.getException(e).getMessage());
                    throw retryPolicy.getException(e);
                }
            }
        });
        final PersistenceObject jobInputPo = dataBucket.findPoById(poId);
        validatePo(jobInputPo, poId);
        final Map<String, Object> userFilterMap = jobInputPo.getAttribute(ATTR_USER_FILTER);
        userFilterMap.put(MEMBER_MO_SPECIFICATIONS_LIST, moSpecifications);
        jobInputPo.setAttribute(ATTR_USER_FILTER, userFilterMap);
    }

    @Override
    public TransformerUserFilter getTransformerUserFilterFromUserFilterMap(final Map<String, Object> userFilterMap) {
        return userFilterUtilitiesBean.generateTransformerUserFilterFromUserFilterMap(userFilterMap);
    }

    @Override
    public String getExportFileFromJobOutputPo(final long poId) {
        return dataPersistenceService.getLiveBucket().findPoById(poId).getAttribute(ATTR_EXPORT_FILE);
    }

    @Timed(group = "cleanup", name = "deleteDpsData")
    @Override
    public void deleteJobPo(final long masterJobId, final List<Long> slaveJobIds) {
        removePo(findMasterJobInputPoByJobId(masterJobId));
        removePo(findJobOutput(masterJobId));
        final int counter = findAndDeleteNodeExportResults(masterJobId);
        logger.debug("NodeExportResults count : [{}] For masterJobId : [{}]", counter, masterJobId);
        for (final long slaveJobId : slaveJobIds) {
            removePo(findJobInputPoByJobId(slaveJobId));
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void deletePo(final long poId) {
        final PersistenceObject jobInputPo = dataPersistenceService.getLiveBucket().findPoById(poId);
        validatePo(jobInputPo, poId);
        removePo(jobInputPo);
    }

    private void removePo(final PersistenceObject po) {
        if (po != null) {
            dataPersistenceService.getLiveBucket().deletePo(po);
        }
    }

    private PersistenceObject findJobInputPoByJobId(final long jobId) {
        final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(NAMESPACE_BATCH, TYPE_JOB_INPUT);

        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(ATTR_JOB_ID, jobId);
        typeQuery.setRestriction(restriction);
        final List<PersistenceObject> persistenceObjects = executeQuery(dataPersistenceService.getLiveBucket(), typeQuery);
        if (!persistenceObjects.isEmpty()) {
            return persistenceObjects.get(0);
        } else {
            return null;
        }
    }

    private PersistenceObject findMasterJobInputPoByJobId(final long jobId) {
        final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery =
                queryBuilder.createTypeQuery(NAMESPACE_BATCH, TYPE_MASTER_EXPORT_JOB_INPUT);
        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(ATTR_JOB_ID, jobId);
        typeQuery.setRestriction(restriction);
        final List<PersistenceObject> persistenceObjects = executeQuery(dataPersistenceService.getLiveBucket(), typeQuery);
        if (!persistenceObjects.isEmpty()) {
            return persistenceObjects.get(0);
        } else {
            logger.debug("Cannot find PO for jobID: [{}] in DPS");
            return null;
        }
    }

    @Override
    public Map<Long, ExportJobExecutionEntity> mapJobOutputPosToExportJobExecutionEntities() {
        final Map<Long, ExportJobExecutionEntity> mapExportJobExecutionEntities = new HashMap<>();
        final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(NAMESPACE_BATCH, TYPE_JOB_OUTPUT);

        final DataBucket liveBucket = dataPersistenceService.getLiveBucket();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
        final Iterator<PersistenceObject> poIterator = queryExecutor.execute(typeQuery);

        while (poIterator.hasNext()) {
            final PersistenceObject jobOutputPo = poIterator.next();
            final Long jobId = jobOutputPo.getAttribute(ATTR_JOB_ID);
            final ExportJobExecutionEntity jobExecutionEntity = mapJobOutputPoToExportJobExecutionEntity(jobOutputPo);
            mapExportJobExecutionEntities.put(jobId, jobExecutionEntity);
        }
        return mapExportJobExecutionEntities;
    }

    private void validatePo(final PersistenceObject jobInputPo, final long poId) {
        if (jobInputPo == null) {
            throw new PersistenceObjectNotFoundException("Invalid or non-existing poID : " + poId);
        }
    }

    @Override
    public ExportJobExecutionEntity mapJobOutputPoToExportJobExecutionEntity(final PersistenceObject jobOutputPo) {
        final ExportJobExecutionEntity jobExecutionEntity = new ExportJobExecutionEntity();
        final Long jobId = jobOutputPo.getAttribute(ATTR_JOB_ID);

        final String batchStatus = jobOutputPo.getAttribute(ATTR_BATCH_STATUS);
        final String fileName = jobOutputPo.getAttribute(ATTR_EXPORT_FILE);
        final String jobName = jobOutputPo.getAttribute(ATTR_JOB_NAME);
        final String exitStatus = jobOutputPo.getAttribute(ATTR_EXIT_STATUS);
        final long createTime = jobOutputPo.getAttribute(ATTR_CREATE_TIME);
        final long startTime = jobOutputPo.getAttribute(ATTR_START_TIME);
        final long lastUpdateTime = jobOutputPo.getAttribute(ATTR_LAST_UPDATE_TIME);
        final long endTime = jobOutputPo.getAttribute(ATTR_END_TIME);
        // Null Handling to allow upgrade behaviour with no issues.
        String userId = "";
        if (jobOutputPo.getAttribute(ATTR_USER_ID) != null) {
            userId = jobOutputPo.getAttribute(ATTR_USER_ID);
        }
        int expectedNodesExported = 0;
        if (jobOutputPo.getAttribute(ATTR_EXPECTED_NODES_EXPORTED) != null) {
            expectedNodesExported = jobOutputPo.getAttribute(ATTR_EXPECTED_NODES_EXPORTED);
        }
        int nodesExported = 0;
        if (jobOutputPo.getAttribute(ATTR_NODES_EXPORTED) != null) {
            nodesExported = jobOutputPo.getAttribute(ATTR_NODES_EXPORTED);
        }
        int nodesNotExported = 0;
        if (jobOutputPo.getAttribute(ATTR_NODES_NOT_EXPORTED) != null) {
            nodesNotExported = jobOutputPo.getAttribute(ATTR_NODES_NOT_EXPORTED);
        }
        int nodesNoMatchFound = 0;
        if (jobOutputPo.getAttribute(ATTR_NODES_NO_MATCH_FOUND) != null) {
            nodesNoMatchFound = jobOutputPo.getAttribute(ATTR_NODES_NO_MATCH_FOUND);
        }
        long numberOfMosExported = 0L;
        if (jobOutputPo.getAttribute(ATTR_MOS_EXPORTED) != null) {
            numberOfMosExported = jobOutputPo.getAttribute(ATTR_MOS_EXPORTED);
        }
        long durationReadingFromDps = 0L;
        if (jobOutputPo.getAttribute(ATTR_DPS_READ_DURATION) != null) {
            durationReadingFromDps = jobOutputPo.getAttribute(ATTR_DPS_READ_DURATION);
        }

        jobExecutionEntity.setId(jobId);
        jobExecutionEntity.setStatus(BatchStatus.valueOf(batchStatus));
        jobExecutionEntity.setFileName(fileName);
        jobExecutionEntity.setJobName(jobName);
        jobExecutionEntity.setExitStatus(exitStatus);
        jobExecutionEntity.setCreateTime(createTime);
        jobExecutionEntity.setStartTime(startTime);
        jobExecutionEntity.setLastUpdateTime(lastUpdateTime);
        jobExecutionEntity.setEndTime(endTime);
        jobExecutionEntity.setExpectedNodesExported(expectedNodesExported);
        jobExecutionEntity.setNodesExported(nodesExported);
        jobExecutionEntity.setNodesNotExported(nodesNotExported);
        jobExecutionEntity.setNodesNoMatchFound(nodesNoMatchFound);
        jobExecutionEntity.setNumberOfMosExported(numberOfMosExported);
        jobExecutionEntity.setDpsReadDuration(durationReadingFromDps);
        jobExecutionEntity.setUserId(userId);
        return jobExecutionEntity;
    }

    @Override
    public Map<String, Object> getNodeSizeCategoryCountFromJobInputPo(final long jobInputPoId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(jobInputPoId);
        return jobInputPo.getAttribute(ATTR_NODE_SIZE_CATEGORY_COUNT);
    }

    @Override
    public MasterJobInputPoAttributes getMasterJobInputPoAttributesForStartingSlaveJob(final long masterJobInputPoId, final String slaveId) {
        final LockablePersistenceObject masterJobInputPo = (LockablePersistenceObject) findJobInputPoByIdForReadOnlyAccess(masterJobInputPoId);
        masterJobInputPo.acquireReadLock();

        final Map<String, Object> allAttributes = masterJobInputPo.getAllAttributes();
        final String exportType = (String) allAttributes.get(ATTR_EXPORT_TYPE);
        final String configName = (String) allAttributes.get(ATTR_CONFIG_NAME);
        final Boolean enumTranslate = (Boolean) allAttributes.get(ATTR_ENUM_TRANSLATE);
        final Boolean prettyFormat = (Boolean) allAttributes.get(ATTR_PRETTY_FORMAT);
        final Boolean batchFilter = (Boolean) allAttributes.get(ATTR_BATCH_FILTER);
        final Boolean exportCppInventoryMos = extractBooleanAttribute(allAttributes.get(ATTR_EXPORT_CPP_INVENTORY_MOS), false);
        final Boolean exportNonSynchronizedNodes = extractBooleanAttribute(allAttributes.get(ATTR_EXPORT_NON_SYNCHRONIZED_NODES), true);

        final List<Object> dataCategories = (List<Object>) allAttributes.get(ATTR_DATA_CATEGORIES);
        final Map<String, Object> predefinedFilterPo = (Map<String, Object>) allAttributes.get(ATTR_EXPORT_FILTER);
        final Map<String, Object> userDefinedFilterPo = (Map<String, Object>) allAttributes.get(ATTR_USER_FILTER);
        final List<String> partitionedNodes = getPartitionedNodes(allAttributes, slaveId);
        final Map<String, Object> nodeSizeCategoryCount = getNodeSizeCategoryCount(allAttributes, slaveId);

        final MasterJobInputPoAttributes masterJobInputPoAttributes = new MasterJobInputPoAttributes();
        masterJobInputPoAttributes.setExportType(exportType);
        masterJobInputPoAttributes.setConfigName(configName);
        masterJobInputPoAttributes.setEnumTranslate(enumTranslate);
        masterJobInputPoAttributes.setPrettyFormat(prettyFormat);
        masterJobInputPoAttributes.setBatchFilter(batchFilter);
        masterJobInputPoAttributes.setExportCppInventoryMos(exportCppInventoryMos);
        masterJobInputPoAttributes.setExportNonSynchronizedNodes(exportNonSynchronizedNodes);
        masterJobInputPoAttributes.setDataCategories(dataCategories);
        masterJobInputPoAttributes.setPredefinedFilterPo(predefinedFilterPo);
        masterJobInputPoAttributes.setUserDefinedFilterPo(userDefinedFilterPo);
        masterJobInputPoAttributes.setPartitionedNodes(partitionedNodes);
        masterJobInputPoAttributes.setNodeSizeCategoryCount(nodeSizeCategoryCount);
        return masterJobInputPoAttributes;
    }

    @Override
    public long getNodeExportResultPo(final String nodeName, final String dataCategory, final long masterJobPoId) {
        long nodeExportResultPoid = 0L;
        final PersistenceObject nodeExportResultPo = findNodeExportResult(masterJobPoId, nodeName, dataCategory);
        if (null != nodeExportResultPo) {
            nodeExportResultPoid = nodeExportResultPo.getPoId();
        }
        return nodeExportResultPoid;
    }

    public Boolean getSkipPreliminary(final long jobInputPoId) {
        final PersistenceObject jobInputPo = findJobInputPoByIdForReadOnlyAccess(jobInputPoId);
        return (Boolean) jobInputPo.getAttribute(ATTR_SKIP_PRELIMINARY_NODE);
    }

    private Boolean extractBooleanAttribute(final Object booleanObject, final Boolean defaultValue) {
        if (booleanObject != null) {
            return (Boolean) booleanObject;
        }
        return defaultValue;
    }

    private List<String> getPartitionedNodes(final Map<String, Object> allAttributes, final String slaveId) {
        final Map<String, Object> partitionedNodeMap = (Map<String, Object>) allAttributes.get(ATTR_PARTITIONED_MAP);
        final Map<String, Object> partition = (Map<String, Object>) partitionedNodeMap.get(slaveId);
        if (partition != null) {
            return (List<String>) partition.get(MEMBER_PARTITIONED_NODES);
        }
        return Collections.emptyList();
    }

    private Map<String, Object> getNodeSizeCategoryCount(final Map<String, Object> allAttributes, final String slaveId) {
        final Map<String, Object> nodeSizeCategoryCountPerSlave = (Map<String, Object>) allAttributes.get(ATTR_NODE_SIZE_CATEGORY_COUNT_PER_SLAVE);
        if (nodeSizeCategoryCountPerSlave != null) {
            final Map<String, Object> nodeSizeCategoryCount = (Map<String, Object>) nodeSizeCategoryCountPerSlave.get(slaveId);
            if (nodeSizeCategoryCount != null) {
                return nodeSizeCategoryCount;
            }
        }
        return Collections.emptyMap();
    }

    private void updateNodeExportResultPo(final PersistenceObject nodeExportResultPo, final Map<String, Object> skipattributes) {
        nodeExportResultPo.setAttribute(ATTR_TRANSFORMATION_STATUS, skipattributes.get(ATTR_TRANSFORMATION_STATUS));
        nodeExportResultPo.setAttribute(ATTR_STATUS_MESSAGE, skipattributes.get(ATTR_STATUS_MESSAGE));
        nodeExportResultPo.setAttribute(ATTR_NUMBER_OF_MOS, skipattributes.get(ATTR_NUMBER_OF_MOS));
        nodeExportResultPo.setAttribute(ATTR_DPS_READ_DURATION, skipattributes.get(ATTR_DPS_READ_DURATION));
        nodeExportResultPo.setAttribute(ATTR_DATA_CATEGORY, skipattributes.get(ATTR_DATA_CATEGORY));
        nodeExportResultPo.setAttribute(ATTR_FILE_NAME, skipattributes.get(ATTR_FILE_NAME));
        nodeExportResultPo.setAttribute(ATTR_EXPORT_STATUS, skipattributes.get(ATTR_EXPORT_STATUS));
        nodeExportResultPo.setAttribute(ATTR_NODE_ROOT_FDN, skipattributes.get(ATTR_NODE_ROOT_FDN));
    }
}
