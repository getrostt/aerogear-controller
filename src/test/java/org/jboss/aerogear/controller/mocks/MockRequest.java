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
package org.jboss.aerogear.controller.mocks;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Joiner;

public class MockRequest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private ServletContext servletContext;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher requestDispatcher;

    private final Map<String, String[]> params = new LinkedHashMap<String, String[]>();
    private final List<Cookie> cookies = new ArrayList<Cookie>();
    private StringWriter stringWriter;

    public MockRequest() {
        MockitoAnnotations.initMocks(this);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    public MockRequest requestMethod(final RequestMethod httpMethod) {
        when(request.getMethod()).thenReturn(httpMethod.toString());
        return this;
    }

    public MockRequest acceptHeader(final String acceptHeader) {
        when(request.getHeader("Accept")).thenReturn(acceptHeader);
        return this;
    }

    public MockRequest body(final String body) {
        try {
            when(request.getInputStream()).thenReturn(inputStream(body));
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return this;
    }

    private ServletInputStream inputStream(final String json) {
        final ByteArrayInputStream ba = new ByteArrayInputStream(json.getBytes());
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return ba.read();
            }
        };
    }

    public void setRequestURL(String path) {
        final StringBuffer requestURL = new StringBuffer("http://localhost:8080" + request.getRequestURI());
        when(request.getRequestURL()).thenReturn(requestURL);
    }

    public void setRequestURI(final String path) {
        if (servletContext.getContextPath() == null) {
            servletContext("/test");
        }
        final String requestUri = servletContext.getContextPath() + path;
        when(request.getRequestURI()).thenReturn(requestUri);
    }

    public String extractQueryParameters(final String path) {
        final int indexOf = path.indexOf("?");
        if (indexOf != -1) {
            final String substring = path.substring(indexOf + 1);
            final String[] split = substring.split("&");
            for (String string : split) {
                final String[] param = string.split("=");
                params.put(param[0], new String[] { param[1] });
            }
            return path.substring(0, indexOf);
        }
        return path;
    }

    public void prepareProcessing() {
        setRequestParams();
        setCookies();
        setupWriter();
    }

    public void setupWriter() {
        try {
            stringWriter = new StringWriter();
            when(response.getWriter()).thenReturn(printWriter(stringWriter));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PrintWriter printWriter(StringWriter writer) {
        return new PrintWriter(writer);
    }

    public void setCookies() {
        when(request.getCookies()).thenReturn(cookies.toArray(new Cookie[] {}));
    }

    @SuppressWarnings("unchecked")
    public void setRequestParams() {
        when(request.getParameterMap()).thenReturn(params);
        final StringBuilder sb = new StringBuilder();
        final Object[] array = params.entrySet().toArray();
        for (int i = 0; i < array.length; i++) {
            final Entry<String, String[]> entry = (Entry<String, String[]>) array[i];
            sb.append(entry.getKey()).append("=").append(entry.getValue()[0]);
            if (i < (array.length - 1)) {
                sb.append("&");
            }
        }
        when(request.getQueryString()).thenReturn(sb.toString());
    }

    public void servletContext(final String context) {
        when(servletContext.getContextPath()).thenReturn(context);
        when(request.getServletContext()).thenReturn(servletContext);
    }

    public MockRequest param(final String name, final String value) {
        params.put(name, new String[] { value });
        return this;
    }

    public MockRequest header(final String name, final String value) {
        when(request.getHeader(name)).thenReturn(value);
        return this;
    }

    public MockRequest cookie(final String name, final String value) {
        final Cookie cookie = mock(Cookie.class);
        when(cookie.getName()).thenReturn(name);
        when(cookie.getValue()).thenReturn(value);
        cookies.add(cookie);
        return this;
    }
    
    public MockRequest contentType(final String contentType) {
        when(request.getContentType()).thenReturn(contentType);
        return this;
    }

    public StringWriter getStringWriter() {
        return stringWriter;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

}
