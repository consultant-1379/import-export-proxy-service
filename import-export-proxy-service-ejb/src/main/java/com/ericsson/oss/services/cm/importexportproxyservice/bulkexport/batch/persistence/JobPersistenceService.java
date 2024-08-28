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

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.cm.export.api.ExportJobExecutionEntity;
import com.ericsson.oss.services.cm.export.transformer.api.ProcessResult;
import com.ericsson.oss.services.cm.export.transformer.api.TransformerUserFilter;

/**
 * Service to persist and retrieve job information using DPS.
 */
@Local
public interface JobPersistenceService {

    /**
     * Gets PO.
     *
     * @param poId
     *            Id of the persistence object
     * @return PO
     */
    PersistenceObject findPoById(long poId);

    /**
     * Gets an array of nodes to export.
     *
     * @param poId
     *            JobInput PO ID
     * @return array of nodes to export
     */
    String[] getNodesToExportFromJobInputPo(long poId);

    /**
     * Gets a list of nodes to export.
     *
     * @param poId
     *            JobInput PO ID
     * @return list of nodes to export
     */
    List<String> getNodesToExportListFromJobInputPo(long poId);

    /**
     * Finds the Node FDNs of a particular partition querying the associated JobInput PO.
     *
     * @param poId
     *            Id of the JobInput PO
     * @param partitionIndex
     *            Index of the partition
     * @return The list of node FDNs of the partition
     */
    List<String> findPartitionedNodes(long poId, int partitionIndex);

    /**
     * Finds the Node FDNs Map querying the Master JobInput PO.
     *
     * @param poId
     *            Id of the MasterJobInput PO
     * @return The map of clusterMemberId and node FDNs
     */
    Map<String, Object> findPartitionedNodesForSlaveMap(long poId);

    /**
     * Creates a new JobInputPo into DPS.
     * This method is executed in a new transaction context.
     *
     * @param attributes
     *            attributes of the JobInputPo
     * @return the poId Id of the PO created
     */
    long createJobInputPo(Map<String, Object> attributes);

    /**
     * Creates a new MasterJobInputPo into DPS.
     * This method is executed in a new transaction context.
     *
     * @param attributes
     *            attributes of the JobInputPo
     * @return the poId Id of the PO created
     */
    long createMasterJobInputPo(Map<String, Object> attributes);

    /**
     * Updates the Master JobInput PO with the partitioned map attribute.
     *
     * @param poId
     *            ID of the JobInput
     * @param jobId
     *            Job Id of the export job
     * @param partitionedNodesMap
     *            Partitioned Map attribute
     * @param nodeSizeCategoryCountPerSlave
     *            map of large, medium, and regular node counts for slave
     * @param masterNodeSizeCategoryCount
     *            map of large, medium, regular and unknown nodes for master
     */
    void updateMasterJobInput(long poId, long jobId, Map<String, Object> partitionedNodesMap, Map<String, Object> nodeSizeCategoryCountPerSlave,
                              Map<String, Object> masterNodeSizeCategoryCount);

    /**
     * Updates the JobInput PO with the partitioned map attribute.
     *
     * @param poId
     *            ID of the JobInput
     * @param jobId
     *            Job Id of the export job
     * @param partitionedNodesMap
     *            Partitioned Map attribute
     */
    void updateJobInputPartitionedNodesAttribute(long poId, long jobId, Map<Integer, Object> partitionedNodesMap);

    /**
     * Creates a new NodeExportResult PO into DPS.
     * This method is executed in a new transaction context.
     *
     * @param attributes
     *            attributes of the JobInputPo
     * @return the poId created
     */
    long createNodeExportResultPo(Map<String, Object> attributes);

    /**
     * Creates a new JobOutputPo into DPS.
     * This method is executed in a new transaction context.
     *
     * @param attributes
     *            attributes of the JobInputPo
     * @return the poId
     */
    long createJobOutputPo(Map<String, Object> attributes);

    /**
     * Finds the list of NodeExportResult PO's of a particular export job.
     *
     * @param jobId
     *            Job Id of the export job
     * @return the list of NodeExportResult PO's of the job
     */
    List<PersistenceObject> findNodeExportResultPosByJobId(long jobId);

    /**
     * Finds the JobOutput PO of an export job.
     *
     * @param jobId
     *            Job Id of the export job
     * @return the JobOutput PO of the export job or null if is not found into the DPS
     */
    PersistenceObject findJobOutputPoByJobId(long jobId);

    /**
     * Maps the list of NodeExportResult POs to a list {@code ProcessResult}.
     *
     * @param jobId
     *            Job Id of the export job
     * @return the list of ProcessResuls mapped.
     */
    List<ProcessResult> mapNodeExportResultPosToProcessResults(final long jobId);

    /**
     * Gets the export type attribute of the JobInput PO.
     *
     * @param poId
     *            JobInput PO ID
     * @return the export type attribute
     */
    String getExportTypeFromJobInputPo(long poId);

    /**
     * Gets the config name attribute of the JobInput PO.
     *
     * @param poId
     *            JobInput PO ID
     * @return the config name attribute
     */
    String getConfigNameFromJobInputPo(long poId);

    /**
     * Gets list of MOs to filter on.
     *
     * @param poId
     *            JobInput PO ID
     * @return the list of MOs
     */
    List<String> getMosToFilterFromJobInputPo(long poId);

    /**
     * Gets the enum translate attribute of the JobInput PO.
     *
     * @param poId
     *            JobInput PO ID
     * @return the enum translate attribute
     */
    boolean getEnumTranslateFromJobInputPo(long poId);

    /**
     * Gets all the attributes of the JobInput PO.
     *
     * @param poId
     *            JobInput PO ID
     * @return all the attributes
     */
    Map<String, Object> getAllAtributesFromJobInputPo(long poId);

    /**
     * Gets filter PO from JobInput PO.
     *
     * @param poId
     *            JobInput PO ID
     * @return the filter PO
     */
    Map<String, Object> getFilterPoFromJobInputPo(long poId);

    /**
     * Gets user filter PO from JobInput PO.
     *
     * @param poId
     *            JobInput PO ID
     * @return the filter PO
     */
    Map<String, Object> getUserFilterPoFromJobInputPo(long poId);

    /**
     * Gets a List of user-defined filter MO specifications from JobInput PO.
     *
     * @param poId
     *            JobInput PO ID
     * @return the List of user-defined filter MO specifications
     */
    List<Map<String, Object>> getUserFilterMoSpecificationsFromJobInputPo(long poId);

    /**
     * updates the user filter PO for given JobInput PO with List of updated MO specifications.
     *
     * @param poId
     *            JobInput PO ID
     * @param moSpecifications
     *            updated List of MO specifications
     */
    void updateMoSpecificationsForUserFilterPo(final long poId, final List<Map<String, Object>> moSpecifications);

    /**
     * Gets transformer user filter dto from user filter map retrieved from job input po.
     *
     * @param userFilterMap
     *            user filter map from jobinput po
     * @return the transformer dto for the user filter
     */
    TransformerUserFilter getTransformerUserFilterFromUserFilterMap(final Map<String, Object> userFilterMap);

    /**
     * Get the export file path from JobOutput PO.
     *
     * @param poId
     *            JobOutput PO ID
     * @return the file path for the export file
     */
    String getExportFileFromJobOutputPo(long poId);

    /**
     * Deletes the POs created during an export job.
     *
     * @param masterJobId
     *            ID of the master export job
     * @param slaveJobIds
     *            IDs of the slave export jobs
     */
    void deleteJobPo(long masterJobId, List<Long> slaveJobIds);

    /**
     * Deletes a PO.
     *
     * @param poId
     *            ID of the PO
     */
    void deletePo(long poId);

    /**
     * Maps the list of JobOutput POs to a Map which job IDs as key and {@code ExportJobExecutionEntity} as values.
     *
     * @return the map of ExportJobExecutionEntity.
     */
    Map<Long, ExportJobExecutionEntity> mapJobOutputPosToExportJobExecutionEntities();

    /**
     * Map a JobOutput PO to a {@code ExportJobExecutionEntity}.
     *
     * @param jobOutputPo
     *            PO to map
     * @return the ExportJobExecutionEntity with the information of the result of an export job
     */
    ExportJobExecutionEntity mapJobOutputPoToExportJobExecutionEntity(PersistenceObject jobOutputPo);

    /**
     * Gets a count of the node size categories for a slave for the given PO ID.
     *
     * @param jobInputPoId
     *            ID of the JobInput
     * @return count of the node size categories for the slave
     */
    Map<String, Object> getNodeSizeCategoryCountFromJobInputPo(long jobInputPoId);

    /**
     * Gets the required attributes from the MasterJobInputPo for starting a slave job.
     *
     * @param masterJobInputPoId
     *            ID of the MasterJobInput PO
     * @param slaveId
     *            ID of the slave
     * @return the MasterJobInputPo attributes
     */
    MasterJobInputPoAttributes getMasterJobInputPoAttributesForStartingSlaveJob(long masterJobInputPoId, String slaveId);

    /**
     * Updates the required attributes on the NodeExportResultPo for ignoring nodes having Preliminary attributes.
     *
     * @param jobInputPoId
     *            ID of the NodeExportResult PO
     * @param attributes
     *            PO attributes
     */
    void updateSkipNodeExportResultPo(Map<String, Object> attributes, long jobInputPoId);

    /**
     * Gets the required nodes from the NodeExportResultPo for ignoring nodes having Preliminary attributes.
     *
     * @param masterJobPoId
     *            ID of the masterJobPoId PO
     * @param nodeName
     *            Skip Preliminary attribute nodeName
     * @param dataCategory
     *            Skip Preliminary attribute dataCategory
     * @return the NodeExportResult POID
     */
    long getNodeExportResultPo(final String nodeName, final String dataCategory, final long masterJobPoId);

    /**
     * Gets the required boolean for ignoring nodes having Preliminary attributes.
     *
     * @param jobInputPoId
     *            ID of the nodeExportResult PO
     * @return the Boolean to skipPreliminary
     */
    Boolean getSkipPreliminary(final long jobInputPoId);
}
