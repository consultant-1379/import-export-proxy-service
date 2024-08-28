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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkexport.ejb.transformer;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ericsson.oss.services.cm.export.transformer.api.Transformer;
import com.ericsson.oss.services.cm.export.transformer.api.TransformerConstants;

/**
 * Provider to retrieve an instance of a {@code Transformer} base on the
 * export type.
 */
@Stateless
public class TransformerProvider {
    /**
     * Retrieve a {@code TransformerProvider}.
     *
     * @param exportType
     *            the export type
     * @return TransformerProvider
     * @throws NamingException
     *             if the transformer is not found
     */
    public Transformer getTransformer(final String exportType) throws NamingException {
        final InitialContext context = new InitialContext();
        final Object transformer = context.lookup(TransformerConstants.TRANSFORMER_LOCAL_JNDI + exportType);
        return (Transformer) transformer;
    }
}
