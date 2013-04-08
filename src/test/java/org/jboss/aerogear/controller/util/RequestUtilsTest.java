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

package org.jboss.aerogear.controller.util;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RequestUtilsTest {

    @Mock
    private ServletContext servletContext;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Route route;
    @Mock
    private RouteContext routeContext;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(request.getServletContext()).thenReturn(servletContext);
    }

    @Test
    public void extractPath() {
        when(servletContext.getContextPath()).thenReturn("/myapp");
        when(request.getRequestURI()).thenReturn("/myapp/cars/1");
        assertThat(RequestUtils.extractPath(request)).isEqualTo("/cars/1");
    }

    @Test
    public void extractPathDefaultWebApp() {
        when(servletContext.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/cars/1");
        assertThat(RequestUtils.extractPath(request)).isEqualTo("/cars/1");
    }

    @Test
    public void extractMethod() {
        when(request.getMethod()).thenReturn("GET");
        assertThat(RequestUtils.extractMethod(request)).isEqualTo(RequestMethod.GET);
        when(request.getMethod()).thenReturn("PUT");
        assertThat(RequestUtils.extractMethod(request)).isEqualTo(RequestMethod.PUT);
        when(request.getMethod()).thenReturn("POST");
        assertThat(RequestUtils.extractMethod(request)).isEqualTo(RequestMethod.POST);
        when(request.getMethod()).thenReturn("DELETE");
        assertThat(RequestUtils.extractMethod(request)).isEqualTo(RequestMethod.DELETE);
        when(request.getMethod()).thenReturn("OPTIONS");
        assertThat(RequestUtils.extractMethod(request)).isEqualTo(RequestMethod.OPTIONS);
        when(request.getMethod()).thenReturn("HEAD");
        assertThat(RequestUtils.extractMethod(request)).isEqualTo(RequestMethod.HEAD);
        when(request.getMethod()).thenReturn("PATCH");
        assertThat(RequestUtils.extractMethod(request)).isEqualTo(RequestMethod.PATCH);
    }

    @Test
    public void extractAcceptsHeaderMissing() {
        assertThat(RequestUtils.extractAcceptHeader(request).isEmpty()).isTrue();
    }

    @Test
    public void extractAcceptsHeader() {
        when(request.getHeader("Accept")).thenReturn("application/json, application/xml");
        assertThat(RequestUtils.extractAcceptHeader(request)).contains(MediaType.JSON.getType(), "application/xml");
        assertThat(RequestUtils.extractAcceptHeader(request).size()).isEqualTo(2);
    }
    
    @Test
    public void extractAcceptsHeaderWithQualityFactor() {
        when(request.getHeader("Accept")).thenReturn("application/json; q=0.3, application/xml");
        assertThat(RequestUtils.extractAcceptHeader(request)).contains(MediaType.JSON.getType(), "application/xml");
        assertThat(RequestUtils.extractAcceptHeader(request).size()).isEqualTo(2);
    }
    
    @Test
    public void extractAcceptsHeaderAny() {
        when(request.getHeader("Accept")).thenReturn("*/*,text/html;level=100;custom=someValue");
        assertThat(RequestUtils.extractAcceptHeader(request)).contains(MediaType.ANY, "text/html");
        assertThat(RequestUtils.extractAcceptHeader(request).size()).isEqualTo(2);
    }
    
    @Test
    public void extractPlaceHolders() {
        final Set<String> params = RequestUtils.extractPlaceHolders("/cars/{color}/{brand}");
        assertThat(params).contains("color", "brand");
        assertThat(params.size()).isEqualTo(2);
    }
    
    @Test
    public void extractPlaceHoldersWithSubpath() {
        final Set<String> params = RequestUtils.extractPlaceHolders("/cars/{color}/subpath/{brand}");
        assertThat(params).contains("color", "brand");
        assertThat(params.size()).isEqualTo(2);
    }
    
    @Test
    public void extractPlaceHoldersWithSubpathLast() {
        final Set<String> params = RequestUtils.extractPlaceHolders("/cars/{color}/{brand}/subpath");
        assertThat(params).contains("color", "brand");
        assertThat(params.size()).isEqualTo(2);
    }
    
    @Test
    public void extractPlaceHoldersNoParams() {
        Set<String> params = RequestUtils.extractPlaceHolders("/cars/");
        assertThat(params).isEmpty();
    }
    
    @Test
    public void extractPlaceHoldersMultipleParams() {
        final Set<String> params = RequestUtils.extractPlaceHolders("/cars/{id}?param1={firstname}");
        assertThat(params).contains("id", "firstname");
        assertThat(params.size()).isEqualTo(2);
    }
    
    @Test
    public void injectParamValues() {
        final String uri = "/cars/{id}?param1={firstname}";
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 10);
        map.put("firstname", "Fletch");
        final String processedUri = RequestUtils.injectParamValues(uri, map);
        assertThat(processedUri).isEqualTo("/cars/10?param1=Fletch");
    }
    
    @Test
    public void injectParamValuesWithProperties() {
        final String uri = "/cars/{id}?param1={firstname}?propertyName=propertyValue";
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 10);
        map.put("firstname", "Fletch");
        final String processedUri = RequestUtils.injectParamValues(uri, map);
        assertThat(processedUri).isEqualTo("/cars/10?param1=Fletch?propertyName=propertyValue");
    }
    
    @Test
    public void extractPathSegments() {
        final Map<Integer, String> params = RequestUtils.extractPathSegments("/cars/red/BMW");
        assertThat(params.get(0)).isEqualTo("cars");
        assertThat(params.get(1)).isEqualTo("red");
        assertThat(params.get(2)).isEqualTo("BMW");
        assertThat(params.size()).isEqualTo(3);
    }
    
    @Test
    public void extractPathSegmentsWithPlaceHolders() {
        final Map<Integer, String> params = RequestUtils.extractPathSegments("/cars/{color}/{brand}");
        assertThat(params.get(0)).isEqualTo("cars");
        assertThat(params.get(1)).isEqualTo("color");
        assertThat(params.get(2)).isEqualTo("brand");
        assertThat(params.size()).isEqualTo(3);
    }
    
    @Test
    public void extractPathSegmentWithPlaceHoldersAndSubpath() {
        final Map<Integer, String> params = RequestUtils.extractPathSegments("/cars/{color}/subpath/{brand}");
        assertThat(params.get(0)).isEqualTo("cars");
        assertThat(params.get(1)).isEqualTo("color");
        assertThat(params.get(2)).isEqualTo("subpath");
        assertThat(params.get(3)).isEqualTo("brand");
        assertThat(params.size()).isEqualTo(4);
    }
    
    @Test
    public void extractPathSegmentWithPlaceHoldersAndSubpathAndQueryParam() {
        final Map<Integer, String> params = RequestUtils.extractPathSegments("/cars/red/subpath/ferrari?name=Fletch");
        assertThat(params.get(0)).isEqualTo("cars");
        assertThat(params.get(1)).isEqualTo("red");
        assertThat(params.get(2)).isEqualTo("subpath");
        assertThat(params.get(3)).isEqualTo("ferrari");
        assertThat(params.size()).isEqualTo(4);
    }
    
    @Test
    public void mapPathParamsSingleParam() {
        final Map<String, String> params = RequestUtils.mapPathParams("/cars/red", "/cars/{color}");
        assertThat(params.get("color")).isEqualTo("red");
        assertThat(params.size()).isEqualTo(1);
    }
    
    @Test
    public void mapPathParamsMultipleParams() {
        final Map<String, String> params = RequestUtils.mapPathParams("/cars/red/subpath/ferrari", "/cars/{color}/subpath/{brand}");
        assertThat(params.get("color")).isEqualTo("red");
        assertThat(params.get("brand")).isEqualTo("ferrari");
        assertThat(params.size()).isEqualTo(2);
    }
    
    @Test
    public void mapPathParamsMultipleParamsWithQueryParam() {
        final Map<String, String> params = RequestUtils.mapPathParams("/cars/red/subpath/ferrari?name=Fletch", "/cars/{color}/subpath/{brand}");
        assertThat(params.get("color")).isEqualTo("red");
        assertThat(params.get("brand")).isEqualTo("ferrari");
        assertThat(params.size()).isEqualTo(2);
    }
    
    @Test
    public void mapPathParamsNoParams() {
        final Map<String, String> params = RequestUtils.mapPathParams("/cars/red", "/cars/red");
        assertThat(params.isEmpty()).isTrue();
    }
    
    @Test
    public void segmentsMatch() {
        final boolean matches = RequestUtils.segmentsMatch("/cars/segment1/segment2", "/cars/segment1/segment2/");
        assertThat(matches).isTrue();
    }
    
    @Test
    public void segmentsMatchDifferentPaths() {
        final boolean matches = RequestUtils.segmentsMatch("/cars/subpath", "/carz/subpath");
        assertThat(matches).isFalse();
    }
    
    @Test
    public void segmentsMatchWithPlaceholders() {
        final boolean matches = RequestUtils.segmentsMatch("/cars/{color}/{brand}", "/cars/red/BMW");
        assertThat(matches).isTrue();
    }
    
    @Test
    public void segmentsMatchTrailingSlash() {
        final boolean matches = RequestUtils.segmentsMatch("/cars/{color}/{brand}", "/cars/red/BMW/");
        assertThat(matches).isTrue();
    }
    
    @Test
    public void segmentsMatchNoMatch() {
        final boolean matches = RequestUtils.segmentsMatch("/cars/{color}", "/cars/");
        assertThat(matches).isFalse();
    }
    
    @Test
    public void segmentsMatchRealPathContainsMoreSegments() {
        final boolean matches = RequestUtils.segmentsMatch("/cars/{color}", "/cars/red/BMW/");
        assertThat(matches).isFalse();
    }
    
    @Test
    public void segmentsMatchNoMatchDifferentPaths() {
        final boolean matches = RequestUtils.segmentsMatch("/candy/{color}/brands", "/cars/red/brands");
        assertThat(matches).isFalse();
    }
    
}
