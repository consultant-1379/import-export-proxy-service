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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Request interface.
 *
 * @param <T> content type
 */
public class Response<T> {
    private final T content;
    private List<ResponseMessage> messages;

    protected Response(final T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public List<ResponseMessage> getMessages() {
        return messages != null ? Collections.unmodifiableList(messages) : null;
    }

    public void setMessages(final List<ResponseMessage> messages) {
        this.messages = messages;
    }

    public void addMessages(final ResponseMessage... messages) {
        addMessages(Arrays.asList(messages));
    }

    public void addMessages(final List<ResponseMessage> messages) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        for (final ResponseMessage message : messages) {
            if (message != null) {
                this.messages.add(message);
            }
        }
    }
}
