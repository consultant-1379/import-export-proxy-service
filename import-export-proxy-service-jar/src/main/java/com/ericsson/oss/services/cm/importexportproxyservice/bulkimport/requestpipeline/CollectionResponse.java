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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * CollectionResponse base class.
 *
 * @param <T> type
 */
public class CollectionResponse<T> extends Response<List<T>> {

    private final Long totalCount;

    protected CollectionResponse(final CollectionResponseBuilder<T, ?> builder) {
        super(builder.content);
        this.totalCount = builder.totalCount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public static <T> CollectionResponse<T> collectItems(final List<T> items) {
        return collectionResponse(items).build();
    }

    public static <T> CollectionResponse<T> collectItems(final List<T> items, final long totalCount) {
        return collectionResponse(items)
                .withTotalCount(totalCount)
                .build();
    }

    public static <T> CollectionResponse<T> noContent() {
        return new Builder<T>().noContent().build();
    }

    public static <T> CollectionResponse<T> noContent(final List<T> items) {
        return new Builder<T>().noContent().build();
    }

    public static <T> CollectionResponse<T> noContent(final Class<T> clazz) {
        return new Builder<T>().noContent().build();
    }

    public static <T> Builder<T> collectionResponse() {
        return new Builder<>();
    }

    public static <T> Builder<T> collectionResponse(final Class<T> clazz) {
        return new Builder<>();
    }

    public static <T> Builder<T> collectionResponse(final List<T> items) {
        return new Builder<T>().withContent(items);
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class Builder<T> extends CollectionResponseBuilder<T, Builder<T>> {
    }

    @SuppressWarnings({"unchecked", "checkstyle:JavadocType", "checkstyle:JavadocMethod"})
    public static class CollectionResponseBuilder<T, B extends CollectionResponseBuilder<T, B>> {
        private List<T> content = new ArrayList<>();
        private Long totalCount;

        public CollectionResponse<T> build() {
            return new CollectionResponse<>(this);
        }

        public B noContent() {
            this.content = new ArrayList<>();
            this.totalCount = null;
            return (B) this;
        }

        public B withContent(final List<T> content) {
            checkNotNull(content);
            this.content = content;
            return (B) this;
        }

        public B withTotalCount(final Long totalCount) {
            this.totalCount = totalCount;
            return (B) this;
        }
    }
}