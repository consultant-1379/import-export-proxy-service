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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.SYNC_STATUS_PENDING;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.SYNC_STATUS_UNSYNCHRONIZED;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.EXECUTED;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.EXECUTION_ERROR;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.EXECUTION_SKIPPED;
import static com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperationStatusV2.VALID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.bulkimport.persistence.ImportOperationPersistenceService;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportOperation;
import com.ericsson.oss.services.cm.bulkimport.util.FdnUtils;

/**
 * ImportNodesService class to get unique nodes in an Import job having operations valid for execution and executed.
 */
@Stateless
public class ImportNodesService {
    private static final String[] OPERATION_STATUS_VALID_FOR_EXECUTION_AND_EXECUTED = {VALID.name(), EXECUTION_ERROR.name(), EXECUTED.name()};
    private static final String[] OPERATION_STATUS_VALID_FOR_EXECUTION = { VALID.name(), EXECUTION_ERROR.name(), EXECUTION_SKIPPED.name() };
    private static final String[] SYNC_STATUS_FOR_UNSYNC_NODES = {SYNC_STATUS_PENDING,  SYNC_STATUS_UNSYNCHRONIZED};
    private static final String CM_FUNCTION_FDN = "NetworkElement=%s,CmFunction=1";
    private static final String SYNC_STATUS_ATTRIBUTE = "syncStatus";

    @Inject
    ImportOperationPersistenceService importOperationPersistenceService;

    @EServiceRef
    DataPersistenceService dps;

    public List<String> getExecutedAndExecutionEligibleNodes(final long jobId) {
        return getNodes(jobId, OPERATION_STATUS_VALID_FOR_EXECUTION_AND_EXECUTED);
    }

    public Map<String, String> getUnsyncNodeInfoForExecutionEligibleOperations(final long jobId) {
        final List<String> nodeNames = getNodes(jobId, OPERATION_STATUS_VALID_FOR_EXECUTION);
        return getUnsyncNodeStatusInfo(nodeNames);
    }

    public Map<String, String> getUnsyncNodeInfoForExecutedAndExecutionEligibleOperations(final long jobId) {
        final List<String> nodeNames = getNodes(jobId, OPERATION_STATUS_VALID_FOR_EXECUTION_AND_EXECUTED);
        return getUnsyncNodeStatusInfo(nodeNames);
    }

    private List<String> getNodes(final Long jobId, final String... importOperationStatus) {
        final List<ImportOperation> importOperations = importOperationPersistenceService.getAllImportOperations(jobId, importOperationStatus);
        final Set<String> neIds = new HashSet<>();
        for (final ImportOperation importOperation : importOperations) {
            if (!importOperation.getNeIds().isEmpty()) {
                neIds.addAll(FdnUtils.neIdsStringToList(importOperation.getNeIds()));
            }
        }
        return new ArrayList<>(neIds);
    }

    private Map<String, String> getUnsyncNodeStatusInfo(final List<String> nodeNames) {
        final Map<String, String> unsyncNodeDetails = new HashMap<>();
        final DataBucket dataBucket = dps.getLiveBucket();
        for (final String nodeName : nodeNames) {
            final ManagedObject cmFunctionMo = dataBucket.findMoByFdn(String.format(CM_FUNCTION_FDN, nodeName));
            if (cmFunctionMo != null) {
                final String nodeSyncStatus = cmFunctionMo.getAttribute(SYNC_STATUS_ATTRIBUTE);
                if (Arrays.asList(SYNC_STATUS_FOR_UNSYNC_NODES).contains(nodeSyncStatus)) {
                    unsyncNodeDetails.put(nodeName, nodeSyncStatus);
                }
            }
        }
        return unsyncNodeDetails;
    }
}
