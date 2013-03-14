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

/**
 * WebLinking provides support for generating the HTTP response header in accordance to 
 * <a href="http://tools.ietf.org/html/draft-nottingham-http-link-header-10">Web Linking</a>.
 * 
 * @see Links
 */
public class WebLinking {

    public static final String PREVIOUS = "previous";
    public static final String NEXT = "next";
    public static final String LINK_HEADER = "Link";

    private final Links links;

    /**
     * Sole constructor.
     * 
     * @param links the {@link Links} instance used to get the next, previous link links. 
     */
    public WebLinking(final Links links) {
        this.links = links;
    }

    public String getLinkHeaderName() {
        return LINK_HEADER;
    }

    public String getNext() {
        return oneLink(links.getNext(), NEXT);
    }

    public String getPrevious() {
        return oneLink(links.getPrevious(), PREVIOUS);
    }

    public String getLinkHeaders() {
        final StringBuilder sb = new StringBuilder();
        sb.append(oneLink(links.getPrevious(), PREVIOUS)).append(",");
        sb.append(getNext());
        return sb.toString();
    }

    private String oneLink(final String link, final String type) {
        return new StringBuilder("<").append(link).append(">; ").append("rel=").append("\"").append(type).append("\"")
                .toString();
    }

    @Override
    public String toString() {
        return "WebLinking[" + LINK_HEADER + ":" + getLinkHeaders() + "]";
    }

}
