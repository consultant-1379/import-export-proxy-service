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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.scheduler.blockedjob;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.oss.services.cm.export.api.BatchExportConstants.JOB_NAME;
import static com.ericsson.oss.services.cm.export.api.BatchExportConstants.SERVER_ID;

/**
 * Class used to encapsulate job data from Postgres.
 */
public class MasterExportJobData {
    private static final int AFTER_EQUALS = 1;
    private static final String EXPORT_TYPE = "exportType";
    private static final String EQUALS = "=";
    private static final String NEW_LINE = "\n";
    private static final String NOT_SET = "";

    private String masterJobExecutionId;
    private String createTime;
    private String jobName;
    private String exportType;
    private final List<String> slaveJobExecutionIds = new ArrayList<>();
    private final List<String> slaveServerIds = new ArrayList<>();

    /**
     * method used to get the master job's jobExecutionId.
     *
     * @return
     *         the masterJobExecutionId.
     */
    public String getMasterJobExecutionId() {
        return masterJobExecutionId;
    }

    /**
     * method used to set the master job's jobExecutionId.
     *
     * @param masterJobExecutionId
     *            the masterJobExecutionId to set
     */
    public void setMasterJobExecutionId(final String masterJobExecutionId) {
        this.masterJobExecutionId = masterJobExecutionId;
    }

    /**
     * method used to get the job execution Id's of the slave jobs.
     *
     * @return
     *         the slaveJobExecutionIds
     */
    public List<String> getSlaveJobExecutionIds() {
        return slaveJobExecutionIds;
    }

    /**
     * method used to add a slave job's jobExecutionId.
     *
     * @param slaveJobExecutionId
     *            jobExecutionId for slave job.
     */
    public void addSlaveJobExecutionId(final String slaveJobExecutionId) {
        slaveJobExecutionIds.add(slaveJobExecutionId);
    }

    /**
     * method used to get the serverIds for the hosts on which the job is running.
     *
     * @return
     *         the serverIds
     */
    public List<String> getServerIds() {
        return slaveServerIds;
    }

    /**
     * method used to add a host on which the job is running.
     *
     * @param jobParameters
     *            determine the serverId from the job parameters string.
     */
    public void addServerId(final String jobParameters) {
        slaveServerIds.add(getParamValue(SERVER_ID, jobParameters));
    }

    /**
     * method used to get the ExportType stored in Postgres.
     *
     * @return
     *         the type of export, can be 3GPP or dynamic.
     */
    public String getExportType() {
        return exportType;
    }

    /**
     * method used to set the ExportType job parameters field in Postgres.
     *
     * @param jobParameters
     *            determine the export type from the job parameters string.
     */
    public void setExportType(final String jobParameters) {
        exportType = getParamValue(EXPORT_TYPE, jobParameters);
    }

    /**
     * method used to get the startTime stored in Postgres.
     *
     * @return
     *         the startTime when the export started.
     */
    public String getCreateTime() {
        return createTime;
    }

    /**
     * method used to set the startTime from Postgres.
     *
     * @param startTime
     *            determine the start time when the export started from the startTime string.
     */
    public void setCreateTime(final String startTime) {
        createTime = startTime;
    }

    /**
     * method used to get the jobName stored in Postgres.
     *
     * @return
     *         the jobName
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * method used to set the jobName from Postgres.
     *
     * @param jobParameters
     *            the jobName is determined from the jobParameters string.
     */
    public void setJobName(final String jobParameters) {
        jobName = getParamValue(JOB_NAME, jobParameters);
    }

    public static String getParamValue(final String paramName, final String jobParameters) {
        String paramValue = NOT_SET;
        final String[] jobParamArray = jobParameters.split(NEW_LINE);
        for (final String jobParam : jobParamArray) {
            if (jobParam.contains(paramName)) {
                final String[] paramNameValuePair = jobParam.split(EQUALS);
                paramValue = paramNameValuePair[AFTER_EQUALS];
            }
        }
        return paramValue.trim();
    }

    /**
     * This method which enables user to see all data within this object.
     */
    @Override
    public String toString() {
        return "MasterExportJobData jobId = " + masterJobExecutionId + " job name = " + jobName
                + " export type = " + exportType + " create time = " + createTime + " hosts = " + slaveServerIds;
    }
}
