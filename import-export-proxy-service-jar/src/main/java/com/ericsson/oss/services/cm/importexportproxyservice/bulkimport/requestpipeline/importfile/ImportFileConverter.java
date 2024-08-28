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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importfile;

import com.ericsson.oss.services.cm.bulkimport.api.dto.ImportFileDto;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportJobSpecification;
import com.ericsson.oss.services.cm.bulkimport.persistence.entities.ImportJob;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.Converter;

/**
 * Class to convert ImportFile.
 */
public class ImportFileConverter implements Converter<ImportJob, ImportFileDto> {

    @Override
    public ImportFileDto convert(final ImportJob entity) {
        final ImportJobSpecification jobSpecification = entity.getJobSpecification();
        return ImportFileDto.importFile()
                .withId(entity.getId())
                .withJobId(entity.getId())
                .withName(jobSpecification.getFileName())
                .withOriginalName(jobSpecification.getFileOriginalName())
                .withFormat(jobSpecification.getFormat())
                .build();
    }
}
