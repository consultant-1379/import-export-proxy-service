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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.cm.bulkimport.util.FormatLogger;

/**
 * Fetch the global properties from the deploymnets.
 */
public final class PropertiesUtils {
    public static final String GLOBAL_PROPERTIES_FILE = "/ericsson/tor/data/global.properties";
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(PropertiesUtils.class));

    private PropertiesUtils() {
    }

    public static String fetchProperty(final String propertyName) {
        return fetchFromGlobalProperties(propertyName);
    }

    private static String fetchFromGlobalProperties(final String propertyName) {
        try (
            InputStream in = new FileInputStream(GLOBAL_PROPERTIES_FILE);
            Reader inputReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputReader)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(propertyName + "=")) {
                    return line.substring(line.indexOf('=') + 1);
                }
            }
        } catch (final Exception exception) {
            LOGGER.info("I/O Problem during Global Properties file reading: {}", exception);
        }
        return null;
    }
}