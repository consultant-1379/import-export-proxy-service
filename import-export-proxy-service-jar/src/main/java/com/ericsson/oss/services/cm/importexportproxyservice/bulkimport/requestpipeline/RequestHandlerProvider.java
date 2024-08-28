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

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;

/**
 * Request handler provider.
 */
public class RequestHandlerProvider {

    @Inject
    BeanManager beanManager;

    @SuppressWarnings("unchecked")
    public <RESPT extends Response<?>, REQT extends Request<RESPT>> RequestHandler<REQT, RESPT> getHandler(final REQT request) {
        final Set<Bean<?>> beans = beanManager.getBeans(Object.class, new HandleAnnotationQualifier(request));
        final Bean<Object> bean = (Bean<Object>) beans.iterator().next();
        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
        return (RequestHandler<REQT, RESPT>) beanManager.getReference(bean, Object.class, creationalContext);
    }

    /**
     * Qualifier for request handler.
     */
    static class HandleAnnotationQualifier extends AnnotationLiteral<Handle> implements Handle {
        private static final long serialVersionUID = -3655521616718839400L;

        private final Class<? extends Request> clazz;

        HandleAnnotationQualifier(final Request<?> request) {
            this.clazz = request.getClass();
        }

        @Override
        public Class<? extends Request> value() {
            return clazz;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof HandleAnnotationQualifier)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            final HandleAnnotationQualifier otherObject = (HandleAnnotationQualifier) obj;
            return new EqualsBuilder()
                    .appendSuper(super.equals(otherObject))
                    .append(this.serialVersionUID, otherObject.serialVersionUID)
                    .append(this.clazz, otherObject.clazz)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(399, 1097)
                    .appendSuper(super.hashCode())
                    .append(this.serialVersionUID)
                    .append(this.clazz)
                    .toHashCode();
        }
    }
}
