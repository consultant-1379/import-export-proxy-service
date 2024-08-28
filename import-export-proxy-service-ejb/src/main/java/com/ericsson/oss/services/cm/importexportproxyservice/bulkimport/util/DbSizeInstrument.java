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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.instrument.annotation.InstrumentedBean;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.Interval;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.Units;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.Visibility;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.job.persistence.ImportPersistenceManagerBean;

/**
 *  DbSizeInstrument class containing the metrics data and methods annotated with @MonitoredAttributes which are used to poll the importdb size.
 */
@ApplicationScoped
@InstrumentedBean(description = "Collects metrics about the importdb occupied size percentage", displayName = "Importdb percentage Metrics")
public class DbSizeInstrument {

    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(DbSizeInstrument.class));

    @Inject
    ImportPersistenceManagerBean importPersistenceManagerBean;

    /**
    * Monitored method: It lets DDC grab the importdb occupied size percentage .
    *
    * @return the dbsize percentage
    */
    @MonitoredAttribute(visibility = Visibility.EXTERNAL, displayName = "Importdb occupied size in percentage", interval = Interval.FIFTEEN_MIN,
            units = Units.PERCENTAGE)
    public int getDbSize() {
        return importPersistenceManagerBean.getdbPercentage();
    }

}
