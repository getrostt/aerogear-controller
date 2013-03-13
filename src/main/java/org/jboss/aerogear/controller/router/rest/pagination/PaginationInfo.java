/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.controller.router.rest.pagination;

import com.google.common.base.Optional;

/**
 * Holds information related to pagination in AeroGear Controller. 
 * </p> 
 * The parameter names will be provided using the {@code Paginated} annotation and the values will be the values contained in 
 * the current request. If the configured parameters are missing from the request, the default values specified in 
 * {@link Paginated} will be used.
 * 
 * @see Paginated
 */
public class PaginationInfo {

    public static final String DEFAULT_OFFSET_PARAM_NAME = "offset";
    public static final String DEFAULT_LIMIT_PARAM_NAME = "limit";

    private final String offsetParamName;
    private final int offset;
    private final String limitParamName;
    private final int limit;
    private final boolean webLinking;
    private final Optional<String> headerPrefix;

    public PaginationInfo(final String offsetParamName, final int offset, final String limitParamName, final int limit) {
        this(offsetParamName, offset, limitParamName, limit, true, Optional.<String> absent());
    }

    public PaginationInfo(final String offsetParamName, final int offset, final String limitParamName, final int limit,
            final String headerPrefix) {
        this(offsetParamName, offset, limitParamName, limit, false, Optional.fromNullable(headerPrefix));
    }

    private PaginationInfo(final String offsetParamName, final int offset, final String limitParamName, final int limit,
            final boolean webLinking, final Optional<String> headerPrefix) {
        this.offsetParamName = offsetParamName;
        this.offset = offset;
        this.limitParamName = limitParamName;
        this.limit = limit;
        this.webLinking = webLinking;
        this.headerPrefix = headerPrefix;
    }

    public String getOffsetParamName() {
        return offsetParamName;
    }

    public int getOffset() {
        return offset;
    }

    public String getLimitParamName() {
        return limitParamName;
    }

    public int getLimit() {
        return limit;
    }

    public boolean webLinking() {
        return webLinking;
    }

    @Override
    public String toString() {
        return "PaginationInfo[offsetParamName=" + offsetParamName + ", offset=" + offset + ", limitParamName="
                + limitParamName + ", limit=" + limit + ", webLinking=" + webLinking + ", headerPrefix=" + headerPrefix + "]";
    }

    public Optional<String> getHeaderPrefix() {
        return headerPrefix;
    }

    public static PaginationInfoBuilder offset(int value) {
        return offset(PaginationInfo.DEFAULT_OFFSET_PARAM_NAME, value);
    }

    public static PaginationInfoBuilder offset(final String offsetParamName, final int value) {
        return new PaginationInfoBuilderImpl().offset(offsetParamName, value);
    }

    public static interface PaginationInfoBuilder {
        PaginationInfoBuilder offset(String paramName, int value);

        PaginationInfoBuilder limit(String paramName, int value);

        PaginationInfoBuilder limit(int value);

        PaginationInfoBuilder customHeaders();

        PaginationInfoBuilder customHeadersPrefix(String prefix);

        PaginationInfoBuilder webLinking(boolean enabled);

        PaginationInfo build();
    }

    public static class PaginationInfoBuilderImpl implements PaginationInfoBuilder {

        private String offsetParamName = PaginationInfo.DEFAULT_OFFSET_PARAM_NAME;
        private String limitParamName = PaginationInfo.DEFAULT_LIMIT_PARAM_NAME;
        private String headerPrefix;
        private int offset;
        private int limit;
        private boolean webLinking;

        @Override
        public PaginationInfoBuilder offset(final String paramName, final int value) {
            offsetParamName = paramName;
            offset = value;
            return this;
        }

        @Override
        public PaginationInfoBuilder limit(final String paramName, final int value) {
            limitParamName = paramName;
            limit = value;
            return this;
        }

        @Override
        public PaginationInfoBuilder customHeadersPrefix(final String prefix) {
            this.headerPrefix = prefix;
            return this;
        }

        @Override
        public PaginationInfoBuilder customHeaders() {
            this.headerPrefix = PaginationMetadata.DEFAULT_HEADER_PREFIX;
            return this;
        }

        @Override
        public PaginationInfoBuilder limit(int value) {
            limit = value;
            return this;
        }

        @Override
        public PaginationInfoBuilder webLinking(boolean enabled) {
            webLinking = enabled;
            return this;
        }

        public PaginationInfo build() {
            if (webLinking) {
                return new PaginationInfo(offsetParamName, offset, limitParamName, limit);
            } else {
                return new PaginationInfo(offsetParamName, offset, limitParamName, limit, headerPrefix);
            }
        }

    }

}
