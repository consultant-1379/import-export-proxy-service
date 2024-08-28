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

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.ericsson.oss.services.cm.bulkimport.api.domain.MessageType;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.exception.RequestValidationException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Request pipeline impl.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class RequestPipelineBean implements RequestPipeline {

    @Inject
    RequestHandlerProvider handlersProvider;

    @Inject
    PageableRequestValidator pageableRequestValidator;

    @Override
    public <R extends Request<T>, T extends Response<?>> T process(final R request) {
        final RequestHandler<R, T> handler = handlersProvider.getHandler(request);
        final Iterable<RequestValidator<R>> validators = handler.validators();
        final List<ResponseMessage> validationMessages = validate(request, validators);
        final T response = handler.handle(request);
        if (!validationMessages.isEmpty()) {
            response.addMessages(validationMessages);
        }
        return response;
    }

    private <T extends Response<?>, R extends Request<T>> List<ResponseMessage> validate(
            final R request,
            final Iterable<RequestValidator<R>> validators) {
        final List<ResponseMessage> validationMessages = new ArrayList<>();
        if (validators != null) {
            for (final RequestValidator<R> validator : validators) {
                final List<ResponseMessage> validationErrors = validator.validate(request);
                validationMessages.addAll(validationErrors);
            }
        }
        if (request instanceof Pageable) {
            final List<ResponseMessage> validationErrors = pageableRequestValidator.validate((Pageable) request);
            validationMessages.addAll(validationErrors);
        }
        if (hasOneOrMoreErrors(validationMessages)) {
            throw new RequestValidationException(validationMessages);
        }
        return validationMessages;
    }

    private boolean hasOneOrMoreErrors(final List<ResponseMessage> messages) {
        return Iterables.any(messages, new Predicate<ResponseMessage>() {
            @Override
            public boolean apply(final ResponseMessage error) {
                return error.getType() == MessageType.ERROR;
            }
        });
    }
}
