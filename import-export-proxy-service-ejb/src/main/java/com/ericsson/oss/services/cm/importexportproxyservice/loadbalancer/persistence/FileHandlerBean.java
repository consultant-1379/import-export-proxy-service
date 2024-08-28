/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.loadbalancer.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.cm.bulkimport.file.exception.ImportFileNotFoundException;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.BulkCmImportConfiguration;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.util.FormatLogger;

/**
 * Stateless EJB to handle file data provided as input stream.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class FileHandlerBean {
    private static final FormatLogger LOGGER = new FormatLogger(LoggerFactory.getLogger(FileHandlerBean.class));
    private static final String ZIP = "zip";
    private static final String TXT = "txt";
    private static final String CSV = "csv";
    private static final String MODIFIER = "modifier";
    private static final String ELEMENT = "xn:VsDataContainer";
    private static final String FDN = "FDN";

    @Inject
    BulkCmImportConfiguration bulkCmImportConfiguration;

    @Inject
    FileResourceProvider fileResourceProvider;

    public boolean isLargeJob(final String filePath) {
        final Resource resource = getFileResource(filePath);
        final String fileExtension = FilenameUtils.getExtension(resource.getName());
        if (getOperationCountFromFile(filePath, fileExtension) > bulkCmImportConfiguration.getCountOfOperationsInLargeJob()) {
            return true;
        }
        return false;
    }

    private Resource getFileResource(final String filePath) {
        // N.B. Due to limitations of the SFWK resource adapter, only ONE resource should be created in each tx.
        // See SFWK SDK for more details.
        final Resource resource = fileResourceProvider.getFileResource(filePath);
        if (resource == null) {
            throw new ImportFileNotFoundException(FilenameUtils.getName(filePath));
        }
        return resource;
    }

    private long getOperationCountFromFile(final String filePath, final String fileExtension)  {
        if (ZIP.equals(fileExtension)) {
            return getOperationCountFromZipFile(filePath);
        } else if (TXT.equals(fileExtension) || CSV.equals(fileExtension)) {
            return getOperationCountFromTxtFile(filePath);
        } else {
            return getOperationCountFromXmlFile(filePath);
        }
    }

    private long getOperationCountFromTxtFile(final String filePath) {
        long wordcount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] words = line.split(" ");
                for (final String word : words) {
                    if (FDN.equals(word)) {
                        wordcount = wordcount + 1;
                    }
                }
            }
            LOGGER.info("Operation Count in EDFF file: {}", wordcount);
        } catch (final Exception exception) {
            LOGGER.error("Exception occured while reading the file", exception);
        }
        return wordcount;
    }

    private long getOperationCountFromXmlFile(final String filePath) {
        long wordCount = 0;
        try {
            final File xmlFile = new File(filePath);
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            final NodeList textNodes = doc.getElementsByTagName(ELEMENT);
            for (int i = 0; i < textNodes.getLength(); i++) {
                final Node textNode = textNodes.item(i);
                if (textNode.getNodeType() == Node.ELEMENT_NODE) {
                    final Element dataElement = (Element) textNode;
                    final String attributeValue = dataElement.getAttribute(MODIFIER);
                    if (!attributeValue.isEmpty()) {
                        wordCount = wordCount + 1;
                    }
                }
            }
            LOGGER.info("Operation Count in XML file: {}", wordCount);
        } catch (final Exception exception) {
            LOGGER.error("Exception occured while reading the file", exception);
        }
        return wordCount;
    }

    private long getOperationCountFromZipFile(final String filePath) {
        try (ZipFile zipFile = new ZipFile(filePath)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            if (entries.hasMoreElements()) {
                final ZipEntry firstEntry = entries.nextElement();
                final String targetDirectory = "/ericsson/config_mgt/import_files";
                final String targetFilePath = targetDirectory + File.separator + firstEntry.getName();
                final File targetDir = new File(targetDirectory);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                try (InputStream inputStream = zipFile.getInputStream(firstEntry);
                FileOutputStream outputStream = new FileOutputStream(targetFilePath)) {
                    final byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    if (firstEntry.getName().endsWith(".xml")) {
                        return getOperationCountFromXmlFile(targetFilePath);
                    } else if (firstEntry.getName().endsWith(".txt") || firstEntry.getName().endsWith(".csv")) {
                        return getOperationCountFromTxtFile(targetFilePath);
                    }
                }
            }
        } catch (final Exception exception) {
            LOGGER.error("Exception occured while reading the file", exception);
        }
        return 0;
    }
}
