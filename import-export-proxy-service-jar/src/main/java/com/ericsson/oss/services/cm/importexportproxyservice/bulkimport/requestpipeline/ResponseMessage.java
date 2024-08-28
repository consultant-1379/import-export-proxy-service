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

import static com.ericsson.oss.services.cm.bulkimport.api.domain.MessageType.ERROR;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.MessageType.INFO;
import static com.ericsson.oss.services.cm.bulkimport.api.domain.MessageType.WARNING;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.services.cm.bulkimport.api.domain.MessageType;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.CmBulkImportServiceErrors;

/**
 * Response Message class.
 */
public class ResponseMessage {
    private final String uuid;
    private final MessageType type;
    private final String code;
    private final int internalCode;
    private final String message;
    private final Map<String, Object> parameters;

    public ResponseMessage(final ResponseMessageBuilder builder) {
        this.uuid = builder.uuid;
        this.type = builder.type;
        this.code = builder.code;
        this.internalCode = builder.internalCode;
        this.message = builder.message;
        this.parameters = builder.parameters;
    }

    public String getUuid() {
        return uuid;
    }

    public MessageType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public int getInternalCode() {
        return internalCode;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getParameters() {
        return parameters != null ? Collections.unmodifiableMap(parameters) : null;
    }

    public static ResponseMessageBuilder responseMessage() {
        return new ResponseMessageBuilder();
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocMethod", "checkstyle:JavadocType", "checkstyle:OverloadMethodsDeclarationOrder"})
    public static class ResponseMessageBuilder {
        private String uuid;
        private MessageType type;
        private String code;
        private int internalCode;
        private String message;
        private Map<String, Object> parameters;

        public ResponseMessage build() {
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            return new ResponseMessage(this);
        }

        @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
        public ResponseMessageBuilder withUUID(final String uuid) {
            this.uuid = uuid;
            return this;
        }

        public ResponseMessageBuilder withType(final MessageType type) {
            this.type = type;
            return this;
        }

        public ResponseMessageBuilder error(final String message) {
            this.message = message;
            this.type = ERROR;
            return this;
        }

        public ResponseMessageBuilder warning(final String message) {
            this.message = message;
            this.type = WARNING;
            return this;
        }

        public ResponseMessageBuilder info(final String message) {
            this.message = message;
            this.type = INFO;
            return this;
        }

        public ResponseMessageBuilder error(final CmBulkImportServiceErrors error, final Object... args) {
            this.type = MessageType.ERROR;
            this.code = error.getCode();
            this.internalCode = error.getInternalCode();
            this.message = error.message(args);
            return this;
        }

        public ResponseMessageBuilder warning(final CmBulkImportServiceErrors error, final Object... args) {
            this.type = MessageType.WARNING;
            this.code = error.getCode();
            this.internalCode = error.getInternalCode();
            this.message = error.message(args);
            return this;
        }

        public ResponseMessageBuilder info(final CmBulkImportServiceErrors error, final Object... args) {
            this.type = MessageType.INFO;
            this.code = error.getCode();
            this.internalCode = error.getInternalCode();
            this.message = error.message(args);
            return this;
        }

        public ResponseMessageBuilder withCode(final String code) {
            this.code = code;
            return this;
        }

        public ResponseMessageBuilder withInternalCode(final int internalCode) {
            this.internalCode = internalCode;
            return this;
        }

        public ResponseMessageBuilder withMessage(final String message) {
            this.message = message;
            return this;
        }

        public ResponseMessageBuilder withParameters(final Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public ResponseMessageBuilder withParameter(final String name, final Object value) {
            if (this.parameters == null) {
                this.parameters = new HashMap<>();
            }
            this.parameters.put(name, value);
            return this;
        }
    }
}
