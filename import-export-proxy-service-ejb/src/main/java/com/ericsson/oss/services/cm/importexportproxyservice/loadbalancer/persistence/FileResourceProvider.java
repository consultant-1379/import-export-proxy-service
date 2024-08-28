/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cm.importexportproxyservice.loadbalancer.persistence;

import java.io.Closeable;
import javax.enterprise.context.RequestScoped;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * Bean to encapsulate SFWK Resource.
 */
@RequestScoped
public class FileResourceProvider {
    public Resource getFileResource(final String uri) {
        return Resources.getFileSystemResource(uri);
    }

    void close(final Closeable closeable) {
        Resources.safeClose(closeable);
    }
}
