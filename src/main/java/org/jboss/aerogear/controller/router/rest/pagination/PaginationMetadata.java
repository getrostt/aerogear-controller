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

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

/**
 * Represents information about pagination in AeroGear Controller. 
 * </p> 
 * Provides access to "raw" {@link Links}, and {@link PaginationProperties} to enable developers to assemble pagination 
 * information in whatever way they choose.
 */
public class PaginationMetadata {

    public static final String DEFAULT_HEADER_PREFIX = "AG-";
    private final Links links;
    private final WebLinking webLinking;
    private final PaginationProperties params;
    private final Optional<String> headerPrefix;

    public PaginationMetadata(final PaginationProperties paging, final RequestPathParser requestPathParser) {
        this(paging, requestPathParser, Optional.<String> absent());
    }

    public PaginationMetadata(final PaginationProperties paging, final RequestPathParser requestPathParser,
            final String headerPrefix) {
        this(paging, requestPathParser, Optional.of(headerPrefix));
    }

    private PaginationMetadata(final PaginationProperties params, final RequestPathParser requestPathParser,
            final Optional<String> headerPrefix) {
        this.params = params;
        this.headerPrefix = headerPrefix;
        links = new Links(requestPathParser, params);
        webLinking = new WebLinking(links);
    }

    public Links getLinks() {
        return links;
    }

    public Map<String, String> getHeaders(final int resultsSize) {
        final Map<String, String> headers = new HashMap<String, String>();
        if (headerPrefix.isPresent()) {
            if (!firstpage()) {
                headers.put(headerPrefix.get() + "Links-Previous", links.getPrevious());
            }
            if (fullpage(resultsSize)) {
                headers.put(headerPrefix.get() + "Links-Next", links.getNext());
            }
        } else {
            if (firstpage() && fullpage(resultsSize)) {
                headers.put(webLinking.getLinkHeaderName(), webLinking.getNext());
            } else if (!firstpage() && !fullpage(resultsSize)) {
                headers.put(webLinking.getLinkHeaderName(), webLinking.getPrevious());
            } else if (fullpage(resultsSize)) {
                headers.put(webLinking.getLinkHeaderName(), webLinking.getLinkHeaders());
            }
        }
        return headers;
    }
    
    private boolean firstpage() {
        return params.isFirstOffset();
    }
    
    private boolean fullpage(final int results) {
        return results == params.limit();
    }

    public WebLinking getWebLinking() {
        return webLinking;
    }

    @Override
    public String toString() {
        return "PagingMetadata[params=" + params + ", links=" + links + ", headerPrefix=" + headerPrefix + ", webLinking="
                + webLinking + "]";
    }

}
