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

import static java.text.MessageFormat.format;

import static com.ericsson.oss.services.cm.export.api.BatchExportConstants.JOB_NAME;
import static com.ericsson.oss.services.cm.export.api.ExportServiceConstants.INVALID_JOB_ID;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.batch.runtime.BatchStatus;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;

import com.ericsson.oss.services.cm.export.api.ExportCompressionType;
import com.ericsson.oss.services.cm.export.api.JobExecutionEntity;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.metrics.annotation.Timed;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.scheduler.blockedjob.MasterExportJobData;

/**
 * Implements methods to perform operations in the job repository.
 */
@Stateless
@Timed(group = "jobRepository", name = "totalTimeSpentInPostgres")
@SuppressWarnings("unchecked")
public class JobRepositoryBean {
    private static final String MASTER_JOB_ID = "masterJobId";
    private static final int SLAVE_JOB_PARAMETERS_OFFSET = 1;
    private static final int JOB_PARAMETERS_OFFSET = 2;
    private static final int CREATE_TIME_OFFSET = 1;
    private static final int MASTER_JOB_ID_OFFSET = 0;
    private static final int SLAVE_JOB_ID_OFFSET = 0;
    private static final int AFTER_EQUALS = 1;
    private static final int BEFORE_EQUALS = 0;
    private static final String EQUALS = "=";
    private static final String NEW_LINE = "\n";
    private static final String DELETE_JOB_SQL_QUERY = "DELETE FROM JOB_INSTANCE WHERE JOBINSTANCEID = ? AND JOBNAME = ?";
    private static final String GET_JOB_ID_SQL_QUERY = "SELECT JOBEXECUTIONID FROM JOB_DETAIL WHERE NAME = ?";
    private static final String INSERT_JOB_DETAIL_SQL_QUERY = "INSERT INTO JOB_DETAIL (NAME, SERVERID) VALUES (?, ?)";
    private static final String UPDATE_JOB_DETAIL_SQL_QUERY = "UPDATE JOB_DETAIL SET JOBEXECUTIONID = ?  WHERE NAME = ?";
    private static final String DELETE_JOB_DETAIL_SQL_QUERY = "DELETE FROM JOB_DETAIL WHERE NAME = ?";
    private static final String APPLICATION_NAME = "%export%";
    private static final String GET_JOB_DETAILS_FOR_BLOCKED_EXPORT_JOBS =
            "SELECT JOBEXECUTIONID, CREATETIME, JOBPARAMETERS FROM JOB_EXECUTION INNER JOIN JOB_INSTANCE ON "
                    + "JOB_EXECUTION.JOBINSTANCEID = JOB_INSTANCE.JOBINSTANCEID WHERE JOB_INSTANCE.APPLICATIONNAME LIKE ? "
                    + "AND (JOB_EXECUTION.BATCHSTATUS = 'STARTED' OR JOB_EXECUTION.BATCHSTATUS = 'STARTING') AND "
                    + "(JOB_EXECUTION.CREATETIME < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.MS') - INTERVAL '1' second * ?)";

    private static final String GET_RUNNING_JOBS_SQL_QUERY =
            "SELECT COUNT(*) FROM JOB_EXECUTION WHERE (BATCHSTATUS = 'STARTING' OR BATCHSTATUS = 'STARTED') AND (JOBPARAMETERS LIKE ?)";
    private static final String GET_RUNNING_MASTER_JOBS_SQL_QUERY =
            "SELECT JOBEXECUTIONID FROM JOB_EXECUTION WHERE (BATCHSTATUS = 'STARTING' OR BATCHSTATUS = 'STARTED') "
                    + "AND (JOBPARAMETERS LIKE ?) AND (CREATETIME < to_timestamp(? ,'YYYY-MM-DD HH24:MI:SS.MS'))";
    private static final String GET_JOB_INSTANCE_ID_FROM_JOB_ID = "SELECT JOBINSTANCEID FROM JOB_EXECUTION WHERE JOBEXECUTIONID = ?";
    private static final String GET_JOB_INSTANCE_BATCH_STATUS_FROM_JOB_ID = "SELECT BATCHSTATUS FROM JOB_EXECUTION WHERE JOBEXECUTIONID = ?";
    private static final String GET_JOB_INSTANCE_EXIT_STATUS_FROM_JOB_ID = "SELECT EXITSTATUS FROM JOB_EXECUTION WHERE JOBEXECUTIONID = ?";

    private static final String MASTER_JOB_ID_PARAMETER_CONDITION = "%masterJobId = {0}%";
    private static final String GET_SLAVE_JOBS_SQL_QUERY = "SELECT JOBEXECUTIONID FROM JOB_EXECUTION WHERE (JOBPARAMETERS ~* ?)";
    private static final String GET_SLAVE_JOBS_PARAMETERS_SQL_QUERY =
            "SELECT JOBEXECUTIONID,JOBPARAMETERS FROM JOB_EXECUTION WHERE (JOBPARAMETERS LIKE ?)";
    private static final String UPDATE_BLOCKED_JOBS_SQL_QUERY =
            "UPDATE JOB_EXECUTION SET BATCHSTATUS = ''FAILED'', "
                    + "ENDTIME =''{0}'', "
                    + "EXITSTATUS = ''{1}'' "
                    + "FROM JOB_INSTANCE WHERE JOB_EXECUTION.JOBINSTANCEID = JOB_INSTANCE.JOBINSTANCEID "
                    + "AND JOB_INSTANCE.APPLICATIONNAME like ? "
                    + "AND (JOB_EXECUTION.BATCHSTATUS = ''STARTED'' OR JOB_EXECUTION.BATCHSTATUS = ''STARTING'')"
                    + "AND (JOB_EXECUTION.CREATETIME < to_timestamp(?,''YYYY-MM-DD HH24:MI:SS.MS'') - INTERVAL ''1'' second * ?)";
    private static final String UPDATE_JOBS_WHEN_OFFLINE_SQL_QUERY =
            "UPDATE JOB_EXECUTION SET BATCHSTATUS = ''FAILED'', "
                    + "ENDTIME = ''{0}'', "
                    + "EXITSTATUS = ''{1}'' "
                    + "WHERE JOBEXECUTIONID = ? ";
    private static final String UPDATE_BLOCKED_JOBS_LIST_SQL_QUERY =
            "UPDATE JOB_EXECUTION SET BATCHSTATUS = 'FAILED', "
                    + "EXITSTATUS ='FAILED Export failed as one instance of the impexpserv has restarted' "
                    + "WHERE JOBEXECUTIONID IN";
    private static final String GET_RUNNING_JOBS_FOR_EXPORT_TYPE_WITH_FILE_DETAILS_SQL_QUERY =
            "SELECT JOBEXECUTIONID FROM JOB_EXECUTION WHERE (BATCHSTATUS = 'STARTING' OR BATCHSTATUS = 'STARTED') AND (JOBPARAMETERS LIKE ?) "
                    + "AND (JOBPARAMETERS LIKE ?) AND (JOBPARAMETERS LIKE ?)";

    private static final String GET_COMPRESSED_RUNNING_JOBS_SQL_QUERY =
            "SELECT JOBEXECUTIONID FROM JOB_EXECUTION WHERE (BATCHSTATUS = 'STARTING' OR BATCHSTATUS = 'STARTED') AND (JOBPARAMETERS LIKE ?) "
                    + "AND ((JOBPARAMETERS LIKE ?) OR (JOBPARAMETERS LIKE ?)) AND (JOBPARAMETERS LIKE ?)";

    private static final String GET_JOB_EXCUTION_ID = "SELECT JOB_EXECUTION.JOBEXECUTIONID FROM JOB_EXECUTION INNER JOIN JOB_INSTANCE ON "
            + "JOB_EXECUTION.JOBINSTANCEID=JOB_INSTANCE.JOBINSTANCEID WHERE JOB_INSTANCE.JOBNAME = ?";

    private static final String GET_JOB_EXECUTION_DETAILS_FOR_SPECIFIED_EXECUTION_ID =
            "SELECT JOBEXECUTIONID, CREATETIME, STARTTIME, ENDTIME, LASTUPDATEDTIME, BATCHSTATUS, EXITSTATUS, JOBPARAMETERS FROM JOB_EXECUTION "
                    + "INNER JOIN JOB_INSTANCE ON JOB_EXECUTION.JOBINSTANCEID = JOB_INSTANCE.JOBINSTANCEID "
                    + "WHERE JOB_INSTANCE.JOBNAME LIKE 'masterExportJob' AND JOB_EXECUTION.JOBEXECUTIONID = ?";

    private static final String EXPORT_TYPE_PARAMETER_CONDITION = "%exportType = {0}%";
    private static final String COMPRESSION_TYPE_PARAMETER_CONDITION = "%compressionType = {0}%";
    private static final String CATEGORY_ID_PARAMETER_CONDITION = "%category = {0}%";
    private static final String JOBPARAMETER_OR_CONDITION = " OR (JOBPARAMETERS LIKE ?)";
    private static final String PARAMETER_PLUS_BRACKET = "?)";
    private static final String PARAMETER_PLUS_COMMA = "?,";
    private static final String SPACE_BRACKET = " (";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
    private static final String BLOCKED_JOBS_EXIT_STATUS_MESSAGE = "FAILED as job is running for {0} {1} and is considered blocked.";

    @Inject
    Logger logger;

    @PersistenceContext(unitName = "batchJobRepository")
    EntityManager entityManager;

    /*
     * The rows in the other tables are deleted as well due to ON CASCADA DELETE constraints.
     */
    @Timed(group = "cleanup", name = "deleteJobInstance")
    public void deleteJobInstance(final Long jobId, final String jobName) {
        final Query query = entityManager.createNativeQuery(DELETE_JOB_SQL_QUERY);
        query.setParameter(1, jobId);
        query.setParameter(2, jobName);
        final int result = query.executeUpdate();
        logger.info("Batch Job Datastore, JobID: [{}] of [{}] deleted with result: [{}]", jobId, jobName, result);
    }

    @Timed(group = "jobNaming", name = "getJobInstanceId")
    public Long getJobId(final String jobName) {
        final Query query = entityManager.createNativeQuery(GET_JOB_ID_SQL_QUERY);
        query.setParameter(1, jobName);
        final List<BigInteger> results = query.getResultList();
        logger.debug("Batch Job Datastore, get job Id results: [{}] from job name: [{}]", results, jobName);
        if (results.size() == 1) {
            return results.get(0).longValue();
        } else {
            return INVALID_JOB_ID;
        }
    }

    @Timed(group = "jobNaming", name = "getJobInstanceIdFromJobId")
    public Long getJobInstanceIdFromJobId(final Long jobId) {
        final Query query = entityManager.createNativeQuery(GET_JOB_INSTANCE_ID_FROM_JOB_ID);
        query.setParameter(1, jobId);
        final List<BigInteger> results = query.getResultList();
        logger.debug("Batch Job Datastore, get job Instance Id results: [{}] from job Id: [{}]", results, jobId);
        if (results.size() == 1) {
            return results.get(0).longValue();
        } else {
            return INVALID_JOB_ID;
        }
    }

    @Timed(group = "jobNaming", name = "getJobStatusFromJobId")
    public String getJobStatusFromJobId(final Long jobId) {
        final Query query = entityManager.createNativeQuery(GET_JOB_INSTANCE_BATCH_STATUS_FROM_JOB_ID);
        query.setParameter(1, jobId);
        final String result = (String) query.getSingleResult();
        logger.debug("Batch Job Datastore, get job Status result: [{}] from job Id: [{}]", result, jobId);
        return result;
    }

    @Timed(group = "jobNaming", name = "getJobExitStatusFromJobId")
    public String getJobExitStatusFromJobId(final Long jobId) {
        final Query query = entityManager.createNativeQuery(GET_JOB_INSTANCE_EXIT_STATUS_FROM_JOB_ID);
        query.setParameter(1, jobId);
        final String result = (String) query.getSingleResult();
        logger.debug("Batch Job Datastore, get job exit Status result: [{}] from job Id: [{}]", result, jobId);
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Timed(group = "jobNaming", name = "insertJobDetail")
    public void insertJobDetail(final String jobName, final String serverId) {
        final Query query = entityManager.createNativeQuery(INSERT_JOB_DETAIL_SQL_QUERY);
        query.setParameter(1, jobName);
        query.setParameter(2, serverId);
        final int result = query.executeUpdate();
        logger.debug("Batch Job Datastore, Job detail with job name: [{}] and serverId: [{}] inserted with result: [{}]", jobName, serverId, result);
    }

    @Timed(group = "jobNaming", name = "updateJobDetail")
    public void updateJobDetail(final Long jobId, final String jobName) {
        final Query query = entityManager.createNativeQuery(UPDATE_JOB_DETAIL_SQL_QUERY);
        query.setParameter(1, jobId);
        query.setParameter(2, jobName);
        final int result = query.executeUpdate();
        logger.debug("Batch Job Datastore, Job detail with job name: [{}] and job ID: [{}] updated with result: [{}]", jobName, jobId, result);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Timed(group = "jobNaming", name = "deleteJobDetail")
    public void deleteJobDetail(final String jobName) {
        final Query query = entityManager.createNativeQuery(DELETE_JOB_DETAIL_SQL_QUERY);
        query.setParameter(1, jobName);
        final int result = query.executeUpdate();
        logger.debug("Batch Job Datastore, Job detail with job name: [{}] deleted with result: [{}]", jobName, result);
    }

    @Timed(group = "jobThreshold", name = "getRunningJobs")
    public long getRunningJobs(final String category) {
        final Query query = entityManager.createNativeQuery(GET_RUNNING_JOBS_SQL_QUERY);
        query.setParameter(1, format(CATEGORY_ID_PARAMETER_CONDITION, category));
        final long runningJobs = ((BigInteger) query.getSingleResult()).longValue();
        logger.info("Batch Job Datastore, get running jobs: [{}] for category: [{}]", runningJobs, category);
        return runningJobs;
    }

    public long getRunningJobs() {
        final Query query = entityManager.createNativeQuery(GET_RUNNING_JOBS_SQL_QUERY);
        query.setParameter(1, "%category =%");
        final long runningJobs = ((BigInteger) query.getSingleResult()).longValue();
        logger.info("Batch Job Datastore, get running jobs: [{}]", runningJobs);
        return runningJobs;
    }

    public JobExecutionEntity getJobExecution(final long jobId) {
        final Query query = entityManager.createNativeQuery(GET_JOB_EXECUTION_DETAILS_FOR_SPECIFIED_EXECUTION_ID);
        query.setParameter(1, jobId);
        final List<Object[]> jobExecutionDataList = query.getResultList();
        final JobExecutionEntity jobExecution = new JobExecutionEntity();
        if (jobExecutionDataList != null && !jobExecutionDataList.isEmpty()) {
            final Object[] jobExecutionData = jobExecutionDataList.get(0);
            jobExecution.setExecutionId(((BigInteger) jobExecutionData[0]).longValue());
            jobExecution.setCreateTime((Date) jobExecutionData[1]);
            jobExecution.setStartTime((Date) jobExecutionData[2]);
            jobExecution.setEndTime((Date) jobExecutionData[3]);
            jobExecution.setLastUpdatedTime((Date) jobExecutionData[4]);
            jobExecution.setBatchStatus(BatchStatus.valueOf(jobExecutionData[5].toString()));
            jobExecution.setExitStatus((String) jobExecutionData[6]);
            final Properties jobParameters = getJobParameters(jobExecutionData[7].toString());
            jobExecution.setJobParameters(jobParameters);
            jobExecution.setJobName(jobParameters.getProperty(JOB_NAME));
            return jobExecution;
        }
        return null;
    }

    @Timed(group = "jobThreshold", name = "getRunningJobsOfExportType")
    public List<BigInteger> getRunningJobsOfExportTypeWithFileDetails(final String exportType, final String compressionType, final String category) {
        final Query query = entityManager.createNativeQuery(GET_RUNNING_JOBS_FOR_EXPORT_TYPE_WITH_FILE_DETAILS_SQL_QUERY);
        query.setParameter(1, format(EXPORT_TYPE_PARAMETER_CONDITION, exportType));
        query.setParameter(2, format(COMPRESSION_TYPE_PARAMETER_CONDITION, compressionType));
        query.setParameter(3, format(CATEGORY_ID_PARAMETER_CONDITION, category));
        final List<BigInteger> results = query.getResultList();
        final int runningJobs = results.size();
        logger.info("Batch Job Datastore, get running jobs: [{}] jobs with export type: [{}], compression type: [{}], category: [{}]", runningJobs,
                exportType, compressionType, category);
        return results;
    }

    @Timed(group = "jobThreshold", name = "getCompressedRunningJobsOfExportType")
    public List<BigInteger> getCompressedRunningJobsOfExportTypeWithFileDetails(final String exportType, final String category) {
        final Query query = entityManager.createNativeQuery(GET_COMPRESSED_RUNNING_JOBS_SQL_QUERY);
        query.setParameter(1, format(EXPORT_TYPE_PARAMETER_CONDITION, exportType));
        query.setParameter(2, format(COMPRESSION_TYPE_PARAMETER_CONDITION, ExportCompressionType.ZIP.name()));
        query.setParameter(3, format(COMPRESSION_TYPE_PARAMETER_CONDITION, ExportCompressionType.GZIP.name()));
        query.setParameter(4, format(CATEGORY_ID_PARAMETER_CONDITION, category));
        final List<BigInteger> results = query.getResultList();
        final int runningJobs = results.size();
        logger.info("Batch Job Datastore, get ZIP or GZIP running jobs: [{}] jobs with export type: [{}], category: [{}]", runningJobs,
                exportType, category);
        return results;
    }

    @Timed(group = "jobNaming", name = "getSlaveJobs")
    public List<BigInteger> getSlaveJobs(final Long masterJobId) {
        final Query query = entityManager.createNativeQuery(GET_SLAVE_JOBS_SQL_QUERY);
        final String masterJobIdParameter = "masterJobId = " + masterJobId + "\\y";
        query.setParameter(1,  masterJobIdParameter);
        final List<BigInteger> results = query.getResultList();
        logger.debug("Batch Job Datastore, get slave jobs containing masterJobId: [{}] queryResult [{}]", masterJobId, results);
        return results;
    }

    @Timed(group = "updateBlockedJobs", name = "updateBlockedJobs")
    public void updateBlockedJobs(final long time, final TimeUnit unit) {
        final Date now = new Date();
        final String currentTime = new SimpleDateFormat(DATETIME_FORMAT).format(now);
        final String exitStatus = format(BLOCKED_JOBS_EXIT_STATUS_MESSAGE, time, unit.toString().toLowerCase());
        final Query query = entityManager.createNativeQuery(format(UPDATE_BLOCKED_JOBS_SQL_QUERY, currentTime, exitStatus));
        final long duration = unit.toSeconds(time);
        query.setParameter(1, APPLICATION_NAME);
        query.setParameter(2, currentTime);
        query.setParameter(3, duration);
        final int result = query.executeUpdate();
        logger.debug("Batch Job Datastore, update blocked jobs with duration: [{}] with result: [{}]", duration, result);
    }

    public List<BigInteger> getRunningMasterJobIds(final String timeOfOfflineEvent) {
        final Query query = entityManager.createNativeQuery(GET_RUNNING_MASTER_JOBS_SQL_QUERY);
        query.setParameter(1, "%category =%");
        query.setParameter(2, timeOfOfflineEvent);
        final List<BigInteger> results = query.getResultList();
        final int runningJobs = results.size();
        logger.info("Batch Job Datastore, get running master jobs: [{}]", runningJobs);
        return results;
    }

    public void updateBlockedMasterJob(final long masterJobId, final String nodeId) {
        final Date now = new Date();
        final String currentTime = new SimpleDateFormat(DATETIME_FORMAT).format(now);
        final String exitStatus = format("FAILED as the instance {0} is offline", nodeId);
        final Query query = entityManager.createNativeQuery(format(UPDATE_JOBS_WHEN_OFFLINE_SQL_QUERY, currentTime, exitStatus));
        query.setParameter(1, masterJobId);
        final int result = query.executeUpdate();
        logger.info("Batch Job Datastore, update master job: [{}] with exit status: [{}] queryResult: [{}]", masterJobId, exitStatus, result);
        updateBlockedSlaveJobs(masterJobId, nodeId);
    }

    public void updateBlockedSlaveJobs(final long masterJobId, final String nodeId) {
        final List<BigInteger> slaveJobIds = getSlaveJobs(masterJobId);
        for (final BigInteger currentSlaveId : slaveJobIds) {
            final Date now = new Date();
            final String currentTime = new SimpleDateFormat(DATETIME_FORMAT).format(now);
            final String exitStatus = format("FAILED as the instance {0} is offline", nodeId);
            final Query query = entityManager.createNativeQuery(format(UPDATE_JOBS_WHEN_OFFLINE_SQL_QUERY, currentTime, exitStatus));
            query.setParameter(1, currentSlaveId);
            final int result = query.executeUpdate();
            logger.info("Batch Job Datastore, update slave job: [{}] with exit status: [{}] queryResult: [{}]", currentSlaveId, exitStatus, result);
        }
    }

    @Timed(group = "getJobIdsForBlockedExportJobs", name = "getJobIdsForBlockedExportJobs")
    public List<MasterExportJobData> getExportBlockedJobDataJobs(final long time, final TimeUnit unit) {
        final List<MasterExportJobData> listOfBlockedJobs = new ArrayList<>();
        final Query getblockedJobsQuery = entityManager.createNativeQuery(GET_JOB_DETAILS_FOR_BLOCKED_EXPORT_JOBS);
        final long duration = unit.toSeconds(time);
        final String now = new SimpleDateFormat(DATETIME_FORMAT).format(new Date());
        getblockedJobsQuery.setParameter(1, APPLICATION_NAME);
        getblockedJobsQuery.setParameter(2, now);
        getblockedJobsQuery.setParameter(3, duration);
        final List<Object[]> listOfBlockedObjects = getblockedJobsQuery.getResultList();
        for (final Object[] blockedObjects : listOfBlockedObjects) {
            final MasterExportJobData blockedJobsData = new MasterExportJobData();
            blockedJobsData.setMasterJobExecutionId(blockedObjects[MASTER_JOB_ID_OFFSET].toString());
            blockedJobsData.setCreateTime(blockedObjects[CREATE_TIME_OFFSET].toString());
            blockedJobsData.setExportType(blockedObjects[JOB_PARAMETERS_OFFSET].toString());
            blockedJobsData.setJobName(blockedObjects[JOB_PARAMETERS_OFFSET].toString());
            listOfBlockedJobs.add(blockedJobsData);
        }
        logger.info("Batch Job Datastore, get export running job IDs result: [{}], current time [{}], duration [{}] seconds",
                listOfBlockedJobs.size(), now, duration);
        return listOfBlockedJobs;
    }

    @Timed(group = "getJobIdsForBlockedExportJobs", name = "getJobIdsForBlockedExportJobs")
    public Map<Long, MasterExportJobData> getJobDataForMasterExportJobs(final List<Long> masterJobIds) {
        final Map<Long, MasterExportJobData> masterExportJobsMap = initialiseMasterExportJobsMap(masterJobIds);
        if (masterJobIds.isEmpty()) {
            return masterExportJobsMap;
        }
        final StringBuilder queryStringBuilder = new StringBuilder(GET_SLAVE_JOBS_PARAMETERS_SQL_QUERY);
        for (int jobOffset = 1; jobOffset < masterJobIds.size(); jobOffset++) {
            queryStringBuilder.append(JOBPARAMETER_OR_CONDITION);
        }
        final Query getSlaveJobsDataQuery = entityManager.createNativeQuery(queryStringBuilder.toString());
        int parameterOffset = 1;
        for (final Long masterJobId : masterJobIds) {
            getSlaveJobsDataQuery.setParameter(parameterOffset, format(MASTER_JOB_ID_PARAMETER_CONDITION, masterJobId.toString()));
            parameterOffset++;
        }
        final List<Object[]> listOfJobDetailObjects = getSlaveJobsDataQuery.getResultList();
        addSlaveJobsDataToMasterExportJobsMap(masterExportJobsMap, listOfJobDetailObjects);
        return masterExportJobsMap;
    }

    private Map<Long, MasterExportJobData> initialiseMasterExportJobsMap(final List<Long> masterJobIds) {
        final Map<Long, MasterExportJobData> masterExportJobsMap = new HashMap<>();
        for (final Long masterJobId : masterJobIds) {
            final MasterExportJobData masterExportJobData = new MasterExportJobData();
            masterExportJobData.setMasterJobExecutionId(masterJobId.toString());
            masterExportJobsMap.put(masterJobId, masterExportJobData);
        }
        return masterExportJobsMap;
    }

    private void addSlaveJobsDataToMasterExportJobsMap(final Map<Long, MasterExportJobData> masterExportJobsMap,
            final List<Object[]> listOfJobDetailObjects) {
        for (final Object[] jobDetailObjects : listOfJobDetailObjects) {
            final Long masterJobId =
                    Long.valueOf(MasterExportJobData.getParamValue(MASTER_JOB_ID, jobDetailObjects[SLAVE_JOB_PARAMETERS_OFFSET].toString()));
            final MasterExportJobData masterExportJobData = masterExportJobsMap.get(masterJobId);
            masterExportJobData.addSlaveJobExecutionId(jobDetailObjects[SLAVE_JOB_ID_OFFSET].toString());
            masterExportJobData.addServerId(jobDetailObjects[SLAVE_JOB_PARAMETERS_OFFSET].toString());
            masterExportJobData.setJobName(jobDetailObjects[SLAVE_JOB_PARAMETERS_OFFSET].toString());
        }
    }

    @Timed(group = "updateBlockedJobList", name = "updateBlockedJobList")
    public void updateBlockedJobList(final List<Long> masterJobIds) {
        final String queryString = buildQueryStringForList(masterJobIds, UPDATE_BLOCKED_JOBS_LIST_SQL_QUERY);
        final Query query = entityManager.createNativeQuery(queryString);
        int parameterOffset = 1;
        for (final Long masterJobId : masterJobIds) {
            query.setParameter(parameterOffset, masterJobId);
            parameterOffset++;
        }
        final int result = query.executeUpdate();
        logger.info("Batch Job Datastore, update list of blocked jobs: [{}] with result: [{}]", masterJobIds, result);
    }

    private String buildQueryStringForList(final List listParameters, final String queryPrefix) {
        final StringBuilder queryStringBuilder = new StringBuilder(queryPrefix + SPACE_BRACKET);
        for (int jobOffset = 1; jobOffset <= listParameters.size(); jobOffset++) {
            if (jobOffset < listParameters.size()) {
                queryStringBuilder.append(PARAMETER_PLUS_COMMA);
            } else {
                queryStringBuilder.append(PARAMETER_PLUS_BRACKET);
            }
        }
        return queryStringBuilder.toString();
    }

    @Timed(group = "jobExecutions", name = "getJobExecutioIds")
    public List<Long> getJobExecutionIds(final String batchJobName) {
        final Query query = entityManager.createNativeQuery(GET_JOB_EXCUTION_ID);
        query.setParameter(1, batchJobName);
        final List<BigInteger> results = query.getResultList();
        final List<Long> jobExecutionIds = new ArrayList<>();
        for (final BigInteger id : results) {
            jobExecutionIds.add(id.longValue());
        }
        return jobExecutionIds;
    }

    public Properties getJobParameters(final String jobParameterString) {
        final Properties jobParameters = new Properties();
        final String[] jobParamArray = jobParameterString.split(NEW_LINE);
        for (final String jobParam : jobParamArray) {
            final String[] paramNameValuePair = jobParam.split(EQUALS);
            jobParameters.setProperty(paramNameValuePair[BEFORE_EQUALS].trim(), paramNameValuePair[AFTER_EQUALS].trim());
        }
        return jobParameters;
    }
}