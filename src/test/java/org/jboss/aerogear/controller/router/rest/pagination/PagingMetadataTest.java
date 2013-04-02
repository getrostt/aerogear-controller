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

import static org.fest.assertions.Assertions.assertThat;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.aerogear.controller.Util;
import org.junit.Before;
import org.junit.Test;

public class PagingMetadataTest {

    private final Pattern offsetLimitValue = Pattern.compile(".*\\?offset=([0-9]*)&limit=([0-9]*).*");
    private RequestPathParser requestPathParser;

    @Before
    public void setup() {
        final PaginationInfo paginationInfo = PaginationInfo.offset(0).limit(10).build();
        requestPathParser = new RequestPathParser(paginationInfo, "cars?offset=0&limit=10");
    }

    @Test
    public void links() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser);
        final Links links = metadata.getLinks();
        assertThat(links.getFirst()).isEqualTo("cars?offset=0&limit=10");
        assertThat(links.getPrevious()).isEqualTo("cars?offset=0&limit=10");
    }
    
    @Test
    public void customHeadersNoData() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser, "TEST-");
        final Map<String, String> headers = metadata.getHeaders(0);
        assertThat(headers.isEmpty()).isTrue();
    }
    
    @Test
    public void customHeadersNoFullPageAvailable() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser, "TEST-");
        final Map<String, String> headers = metadata.getHeaders(5);
        assertThat(headers.isEmpty()).isTrue();
    }
    
    @Test
    public void customHeadersOnePageAvailable() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser, "TEST-");
        final Map<String, String> headers = metadata.getHeaders(10);
        assertThat(headers.get("TEST-Links-Next")).isEqualTo("cars?offset=10&limit=10");
        assertThat(headers.size()).isEqualTo(1);
    }

    @Test
    public void navigateForward() {
        PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 5), requestPathParser);
        Links links = metadata.getLinks();
        assertThat(links.getNext()).isEqualTo("cars?offset=5&limit=5");

        metadata = new PaginationMetadata(new PaginationProperties(parseOffset(links.getNext()), 5), requestPathParser);
        links = metadata.getLinks();
        assertThat(links.getNext()).isEqualTo("cars?offset=10&limit=5");
        assertThat(links.getPrevious()).isEqualTo("cars?offset=0&limit=5");

        metadata = new PaginationMetadata(new PaginationProperties(parseOffset(links.getNext()), 5), requestPathParser);
        links = metadata.getLinks();
        assertThat(links.getNext()).isEqualTo("cars?offset=15&limit=5");
    }

    @Test
    public void navigateBackwards() {
        PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(15, 5), requestPathParser);
        Links links = metadata.getLinks();
        assertThat(links.getPrevious()).isEqualTo("cars?offset=10&limit=5");

        metadata = new PaginationMetadata(new PaginationProperties(parseOffset(links.getPrevious()), 5), requestPathParser);
        links = metadata.getLinks();
        assertThat(links.getPrevious()).isEqualTo("cars?offset=5&limit=5");

        metadata = new PaginationMetadata(new PaginationProperties(parseOffset(links.getPrevious()), 5), requestPathParser);
        links = metadata.getLinks();
        assertThat(links.getPrevious()).isEqualTo("cars?offset=0&limit=5");
    }
    
    @Test
    public void webLinkingFirst() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser);
        final Map<String, String> responseHeaders = metadata.getHeaders(10);
        final Map<String, String> headers = Util.parseWebLinkHeader(responseHeaders.get(WebLinking.LINK_HEADER));
        assertThat(headers.get(WebLinking.NEXT)).isEqualTo("cars?offset=10&limit=10");
        assertThat(headers.get(WebLinking.PREVIOUS)).isNull();
    }

    @Test
    public void webLinkingMiddle() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(10, 5), requestPathParser);
        final Map<String, String> responseHeaders = metadata.getHeaders(5);
        final Map<String, String> headers = Util.parseWebLinkHeader(responseHeaders.get(WebLinking.LINK_HEADER));
        assertThat(headers.get(WebLinking.PREVIOUS)).isEqualTo("cars?offset=5&limit=5");
        assertThat(headers.get(WebLinking.NEXT)).isEqualTo("cars?offset=15&limit=5");
    }

    @Test
    public void webLinkingLast() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(5, 5), requestPathParser);
        final Map<String, String> responseHeaders = metadata.getHeaders(3);
        final Map<String, String> headers = Util.parseWebLinkHeader(responseHeaders.get(WebLinking.LINK_HEADER));
        assertThat(headers.get(WebLinking.PREVIOUS)).isEqualTo("cars?offset=0&limit=5");
        assertThat(headers.get(WebLinking.NEXT)).isNull();
    }
    
    @Test
    public void webLinkingNoData() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser);
        final Map<String, String> responseHeaders = metadata.getHeaders(0);
        assertThat(responseHeaders.isEmpty()).isTrue();
    }
    
    @Test
    public void webLinkingNoFullPageAvailable() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser);
        final Map<String, String> responseHeaders = metadata.getHeaders(5);
        assertThat(responseHeaders.isEmpty()).isTrue();
    }
    
    @Test
    public void webLinkingOneFullPageAvailable() {
        final PaginationMetadata metadata = new PaginationMetadata(new PaginationProperties(0, 10), requestPathParser);
        final Map<String, String> responseHeaders = metadata.getHeaders(10);
        final Map<String, String> headers = Util.parseWebLinkHeader(responseHeaders.get(WebLinking.LINK_HEADER));
        assertThat(headers.get(WebLinking.NEXT)).isEqualTo("cars?offset=10&limit=10");
    }

    private int parseOffset(final String header) {
        final Matcher matcher = offsetLimitValue.matcher(header);
        matcher.find();
        final String offset = matcher.group(1);
        return Integer.valueOf(offset);
    }

}
