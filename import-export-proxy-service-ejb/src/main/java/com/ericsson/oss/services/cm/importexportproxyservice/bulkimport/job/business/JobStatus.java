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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportJobDetails;
import com.ericsson.oss.services.cm.async.bulkimport.dto.ImportOperationDetails;
import com.ericsson.oss.services.cm.bulkimport.api.domain.JobType;
import com.ericsson.oss.services.cm.bulkimport.entities.Job;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.persistence.helper.PersistenceJobGetter;

/**
 * The bean that responsible to fetch status information from persitance layer and convert them to business objects when required.
 * <p>
 * TODO: At the moment conversion of objects happens in ImportPersistenceManagerBean, a bean that need to be removed.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class JobStatus {
    @Inject
    PersistenceJobGetter persistenceJobGetter;

    public ImportJobDetails getJobDetails(final long jobId) {
        final List<ImportJobDetails> importJobDetailsDtoList = persistenceJobGetter.getJobDetails(jobId);
        if (!importJobDetailsDtoList.isEmpty()) {
            return importJobDetailsDtoList.get(0);
        }
        return null;
    }

    public List<ImportJobDetails> getAllJobDetails(final Date beginTime, final Date endTime) {
        return persistenceJobGetter.getAllJobDetails(beginTime, endTime);
    }

    public List<ImportOperationDetails> getAllOperationDetailsForJob(final long jobId) {
        final Job job = persistenceJobGetter.getJob(jobId);
        if (!JobType.IMPORT.getType().equalsIgnoreCase(job.getJobType())) {
            return new ArrayList<>();
        }
        return persistenceJobGetter.getAllImportOperationDetailsForJob(jobId);
    }

    public List<ImportOperationDetails> getAllValidationDetailsForJob(final long jobId) {
        return persistenceJobGetter.getAllImportOperationDetailsForJob(jobId);
    }
}
