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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.service;

import static com.ericsson.oss.services.cm.export.api.BatchExportConstants.JOB_INPUT_PO_ID;
import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.*;

import java.util.*;
import javax.batch.runtime.BatchStatus;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.services.cm.export.api.ExportJobExecutionEntity;
import com.ericsson.oss.services.cm.export.api.ExportNodeResult;
import com.ericsson.oss.services.cm.export.api.ExportNodeResult.ExportNodeResultStatus;
import com.ericsson.oss.services.cm.export.api.ExportResponse;
import com.ericsson.oss.services.cm.export.api.JobExecutionEntity;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.batch.persistence.JobPersistenceService;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.ejb.util.ExportStatsUtil;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.retry.DpsRetryPolicies;

/**
 * Bean to retrieve the status of an export job.
 */
@Stateless
public class ExportStatusBean {
    private static final String EMPTY_STRING = "";

    @Inject
    private JobPersistenceService jobPersistenceService;

    @Inject
    private RetryManager retryManager;

    @Inject
    private DpsRetryPolicies retryPolicy;

    @Inject
    private Logger logger;

    public ExportJobExecutionEntity status(final JobExecutionEntity jobExecution) {
        final long jobId = jobExecution.getExecutionId();
        final PersistenceObject jobOutputPo = retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<PersistenceObject>() {
            @Override
            public PersistenceObject execute(final RetryContext retryContext) throws Exception {
                try {
                    return jobPersistenceService.findJobOutputPoByJobId(jobId);
                } catch (final RetriableCommandException e) {
                    logger.error(retryPolicy.getException(e).getMessage());
                    throw retryPolicy.getException(e);
                }
            }
        });
        final ExportJobExecutionEntity exportJobExecutionEntity;

        if (jobOutputPo == null) {
            exportJobExecutionEntity = getExecutionEntityForRunningJob(jobExecution);
        } else {
            exportJobExecutionEntity = getStatusFromJobOutputPo(jobOutputPo);
            // Logic to maintain the backward compatibility for jobs created before this update
            // if the expected node exported counter is 0 then JobOuput PO was created without the node counters
            // TODO Remove this logic as soon as the main customers has upgraded ENM
            if (exportJobExecutionEntity.getExpectedNodesExported() == 0) {
                setNodeCounters(jobExecution, exportJobExecutionEntity);
            }
        }
        // TODO Workaround to issue http://jira-oss.lmera.ericsson.se/browse/TORF-40098.
        if ((exportJobExecutionEntity.getNodesExported() > 0) && (jobExecution.getBatchStatus() == BatchStatus.STARTING)) {
            exportJobExecutionEntity.setStatus(BatchStatus.STARTED);
        }
        return exportJobExecutionEntity;
    }

    private ExportJobExecutionEntity getExecutionEntityForRunningJob(final JobExecutionEntity jobExecution) {
        final ExportJobExecutionEntity exportJobExecutionEntity = ExportJobExecutionEntity.create(jobExecution);
        exportJobExecutionEntity.setFileName(EMPTY_STRING);
        final String userId = jobExecution.getJobParameters().getProperty(ATTR_USER_ID);
        exportJobExecutionEntity.setUserId(userId);
        setNodeCounters(jobExecution, exportJobExecutionEntity);
        return exportJobExecutionEntity;
    }

    private void setNodeCounters(final JobExecutionEntity jobExecution, final ExportJobExecutionEntity exportJobExecutionEntity) {
        final PersistenceObject jobInputPo = getMasterJobInputPo(jobExecution);
        final List<PersistenceObject> nodeExportResulPos =
                retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<List<PersistenceObject>>() {
                    @Override
                    public List<PersistenceObject> execute(final RetryContext retryContext) throws Exception {
                        try {
                            return jobPersistenceService.findNodeExportResultPosByJobId(jobExecution.getExecutionId());
                        } catch (final RetriableCommandException e) {
                            logger.error(retryPolicy.getException(e).getMessage());
                            throw retryPolicy.getException(e);
                        }
                    }
                });
        setNodesCounts(jobInputPo, nodeExportResulPos, exportJobExecutionEntity);
    }

    private static void setNodesCounts(final PersistenceObject jobInputPo, final List<PersistenceObject> nodeExportResulPos,
            final ExportJobExecutionEntity exportJobExecutionEntity) {
        final Map<String, List<PersistenceObject>> nodeToPoMap = ExportStatsUtil.groupPersistenceObjectsIntoRootMos(nodeExportResulPos);
        final int nodesExportedCount = getNodesExportedCount(nodeToPoMap);
        final int nodesNotExportedCount = getNodesNotExportedCount(nodeToPoMap);
        final int expectedNodesCount = getExpectedNodesCount(jobInputPo);
        final int nodesNoMatchFoundCount = getNodesNoMatchFoundCount(jobInputPo);

        exportJobExecutionEntity.setNodesNotExported(nodesNotExportedCount);
        exportJobExecutionEntity.setNumberOfMosExported(getNumberOfMosExported(nodeExportResulPos));
        exportJobExecutionEntity.setExpectedNodesExported(expectedNodesCount);
        exportJobExecutionEntity.setNodesNoMatchFound(nodesNoMatchFoundCount);
        exportJobExecutionEntity.setNodesExported(nodesExportedCount);
    }

    public ExportResponse result(final JobExecutionEntity jobExecution) {
        final ExportResponse exportResponse = new ExportResponse();
        final long jobId = jobExecution.getExecutionId();

        final PersistenceObject jobInputPo = getMasterJobInputPo(jobExecution);

        final PersistenceObject jobOutputPo = retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<PersistenceObject>() {
            @Override
            public PersistenceObject execute(final RetryContext retryContext) throws Exception {
                try {
                    return jobPersistenceService.findJobOutputPoByJobId(jobId);
                } catch (final RetriableCommandException e) {
                    logger.error(retryPolicy.getException(e).getMessage());
                    throw retryPolicy.getException(e);
                }
            }
        });

        final List<PersistenceObject> nodeExportResulPos =
                retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<List<PersistenceObject>>() {
                    @Override
                    public List<PersistenceObject> execute(final RetryContext retryContext) throws Exception {
                        try {
                            return jobPersistenceService.findNodeExportResultPosByJobId(jobId);
                        } catch (final RetriableCommandException e) {
                            logger.error(retryPolicy.getException(e).getMessage());
                            throw retryPolicy.getException(e);
                        }
                    }
                });

        final Map<String, List<PersistenceObject>> nodeToPoMap = ExportStatsUtil.groupPersistenceObjectsIntoRootMos(nodeExportResulPos);
        final Set<ExportNodeResult> notExportedNodes = mapNotExportedNodes(nodeToPoMap);
        final Set<ExportNodeResult> exportedNodes = mapExportedNodes(nodeToPoMap);
        final Set<ExportNodeResult> noMathFoundNodes = mapNoMatchFoundNodes(jobInputPo);

        exportResponse.setJobId(jobId);
        exportResponse.getExportedNodesResult().addAll(exportedNodes);
        exportResponse.getNotExportedNodesResult().addAll(notExportedNodes);
        exportResponse.getNoMatchFoundResult().addAll(noMathFoundNodes);

        // If there is no JobOutput PO we can't get the fileName
        if (jobOutputPo != null) {
            final String fileName = jobOutputPo.getAttribute(ATTR_EXPORT_FILE);
            exportResponse.setFileName(fileName);
        }

        return exportResponse;
    }

    private PersistenceObject getMasterJobInputPo(final JobExecutionEntity jobExecution) {
        return retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<PersistenceObject>() {
            @Override
            public PersistenceObject execute(final RetryContext retryContext) throws Exception {
                try {
                    return jobPersistenceService.findPoById(Long.parseLong(jobExecution.getJobParameters().getProperty(JOB_INPUT_PO_ID)));
                } catch (final RetriableCommandException e) {
                    logger.error(retryPolicy.getException(e).getMessage());
                    throw retryPolicy.getException(e);
                }
            }
        });
    }

    private ExportJobExecutionEntity getStatusFromJobOutputPo(final PersistenceObject jobOutputPo) {
        return retryManager.executeCommand(retryPolicy.getRetryPolicy(), new RetriableCommand<ExportJobExecutionEntity>() {
            @Override
            public ExportJobExecutionEntity execute(final RetryContext retryContext) throws Exception {
                try {
                    return jobPersistenceService.mapJobOutputPoToExportJobExecutionEntity(jobOutputPo);
                } catch (final RetriableCommandException e) {
                    logger.error(retryPolicy.getException(e).getMessage());
                    throw retryPolicy.getException(e);
                }
            }
        });
    }

    private static int getExpectedNodesCount(final PersistenceObject jobInputPo) {
        if (jobInputPo == null) {
            return 0;
        }

        final List<String> nodesToExport = jobInputPo.getAttribute(ATTR_NODES_TO_EXPORT);
        return nodesToExport.size();
    }

    private static int getNodesExportedCount(final Map<String, List<PersistenceObject>> nodeToPoMap) {
        return mapExportedNodes(nodeToPoMap).size();
    }

    private static PersistenceObject getExportedProcessResult(final List<PersistenceObject> persistenceObjects) {
        PersistenceObject nodeExportResultForNodeRootFdn = null;
        for (final PersistenceObject persistenceObject : persistenceObjects) {
            final String exportStatus = persistenceObject.getAttribute(ATTR_EXPORT_STATUS);
            if (exportStatus.equals(ExportNodeResultStatus.EXPORTED.toString())) {
                nodeExportResultForNodeRootFdn = persistenceObject;
            }
        }
        return nodeExportResultForNodeRootFdn;
    }

    private static PersistenceObject getNodeExportForNodeRootFdnIfFound(final PersistenceObject nodeExportResult,
            final List<PersistenceObject> persistenceObjects) {
        PersistenceObject nodeExportResultForNodeRootFdn = null;
        for (final PersistenceObject persistenceObject : persistenceObjects) {
            final String fdn = persistenceObject.getAttribute(ATTR_FDN);
            final String nodeRootFdn = persistenceObject.getAttribute(ATTR_NODE_ROOT_FDN);
            if (fdn.equals(nodeRootFdn)) {
                nodeExportResultForNodeRootFdn = persistenceObject;
            }
        }
        if (nodeExportResultForNodeRootFdn == null) {
            nodeExportResultForNodeRootFdn = nodeExportResult;
        }
        return nodeExportResultForNodeRootFdn;
    }

    private static int getNodesNotExportedCount(final Map<String, List<PersistenceObject>> nodeToPoMap) {
        return mapNotExportedNodes(nodeToPoMap).size();
    }

    private static long getNumberOfMosExported(final List<PersistenceObject> nodeExportResulPos) {
        long numberOfMosExported = 0;
        for (final PersistenceObject nodeExportResult : nodeExportResulPos) {
            final long numberOfMos = nodeExportResult.getAttribute(ATTR_NUMBER_OF_MOS);
            numberOfMosExported += numberOfMos;
        }
        return numberOfMosExported;
    }

    private static Set<ExportNodeResult> mapExportedNodes(final Map<String, List<PersistenceObject>> nodeToPoMap) {
        final Set<ExportNodeResult> exportedNodes = new HashSet<>();
        final Set<String> allRoots = nodeToPoMap.keySet();
        for (final String root : allRoots) {
            int notExportedCount = 0;
            int failedTransformedCount = 0;
            final List<PersistenceObject> persistenceObjects = nodeToPoMap.get(root);
            for (final PersistenceObject nodeExportResult : persistenceObjects) {
                if (ExportStatsUtil.persistenceObjectContainsFailedStatusMessage(nodeExportResult)) {
                    failedTransformedCount++;
                } else if (!ExportStatsUtil.persistenceObjectContainsFileNameAndSuccessfulTransformation(nodeExportResult)) {
                    notExportedCount++;
                }
            }
            if (!ExportStatsUtil.isFailedExportNodeForGivenPersistenceObjects(failedTransformedCount, notExportedCount, persistenceObjects)) {
                final PersistenceObject nodeExportResultForNodeCounters = getExportedProcessResult(persistenceObjects);
                if (nodeExportResultForNodeCounters != null) {
                    exportedNodes.add(mapExportNodeResult(nodeExportResultForNodeCounters));
                }
            }
        }
        return exportedNodes;
    }

    private static Set<ExportNodeResult> mapNotExportedNodes(final Map<String, List<PersistenceObject>> nodeToPoMap) {
        final Set<ExportNodeResult> notExportedNodes = new HashSet<>();
        final Set<String> allRoots = nodeToPoMap.keySet();
        for (final String root : allRoots) {
            PersistenceObject nodeExportResultFound = null;
            int notExportedCount = 0;
            int failedTransformedCount = 0;
            final List<PersistenceObject> persistenceObjects = nodeToPoMap.get(root);
            for (final PersistenceObject nodeExportResult : persistenceObjects) {
                nodeExportResultFound = nodeExportResult;
                if (ExportStatsUtil.persistenceObjectContainsFailedStatusMessage(nodeExportResult)) {
                    failedTransformedCount++;
                } else if (!ExportStatsUtil.persistenceObjectContainsFileNameAndSuccessfulTransformation(nodeExportResult)) {
                    notExportedCount++;
                }
            }
            if (ExportStatsUtil.isFailedExportNodeForGivenPersistenceObjects(failedTransformedCount, notExportedCount, persistenceObjects)) {
                final PersistenceObject nodeExportResultForNodeCounters =
                        getNodeExportForNodeRootFdnIfFound(nodeExportResultFound, persistenceObjects);
                if (nodeExportResultForNodeCounters != null) {
                    notExportedNodes.add(mapExportNodeResult(nodeExportResultForNodeCounters));
                }
            }
        }
        return notExportedNodes;
    }

    private static ExportNodeResult mapExportNodeResult(final PersistenceObject nodeExportResult) {
        final String nodeName = nodeExportResult.getAttribute(ATTR_NODE_NAME);
        final String fdn = nodeExportResult.getAttribute(ATTR_NODE_ROOT_FDN);
        final String exportStatus = nodeExportResult.getAttribute(ATTR_EXPORT_STATUS);
        final String statusMessage = nodeExportResult.getAttribute(ATTR_STATUS_MESSAGE);
        final String fileName = nodeExportResult.getAttribute(ATTR_FILE_NAME);
        return new ExportNodeResult(nodeName, fdn, ExportNodeResultStatus.valueOf(exportStatus), statusMessage, fileName);
    }

    private static int getNodesNoMatchFoundCount(final PersistenceObject jobInputPo) {
        return nodesNoMatchFound(jobInputPo).size();
    }

    private static Set<ExportNodeResult> mapNoMatchFoundNodes(final PersistenceObject jobInputPo) {
        final Set<ExportNodeResult> noMatchFoundNodes = new HashSet<>();
        for (final Map<String, Object> noMatchFoundResult : nodesNoMatchFound(jobInputPo)) {
            noMatchFoundNodes.add(mapNoMatchFoundResult(noMatchFoundResult));
        }
        return noMatchFoundNodes;
    }

    private static List<Map<String, Object>> nodesNoMatchFound(final PersistenceObject jobInputPo) {
        List<Map<String, Object>> noMatchFoundNodes = new ArrayList<>();
        if (jobInputPo != null) {
            noMatchFoundNodes = jobInputPo.getAttribute(ATTR_NODES_NO_MATCH_FOUND);
        }
        return noMatchFoundNodes;
    }

    private static ExportNodeResult mapNoMatchFoundResult(final Map<String, Object> noMatchFoundResult) {
        final String nodeName = (String) noMatchFoundResult.get(ATTR_NODE_NAME);
        String fdn = EMPTY_STRING;
        if (noMatchFoundResult.get(ATTR_NODE_ROOT_FDN) != null) {
            fdn = (String) noMatchFoundResult.get(ATTR_NODE_ROOT_FDN);
        }
        if (fdn.isEmpty() && (noMatchFoundResult.get(ATTR_FDN) != null)) {
            fdn = (String) noMatchFoundResult.get(ATTR_FDN);
        }
        final String exportStatus = (String) noMatchFoundResult.get(ATTR_EXPORT_STATUS);
        final String statusMessage = (String) noMatchFoundResult.get(ATTR_STATUS_MESSAGE);
        return new ExportNodeResult(nodeName, fdn, ExportNodeResultStatus.valueOf(exportStatus), statusMessage, EMPTY_STRING);
    }
}
