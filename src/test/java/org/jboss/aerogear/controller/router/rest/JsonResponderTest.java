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

package org.jboss.aerogear.controller.router.rest;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.Routes;
import org.jboss.aerogear.controller.router.error.ErrorResponseImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JsonResponderTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Route route;
    @Mock
    private Routes routes;
    @InjectMocks
    private RouteContext routeContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void accepts() {
        assertThat(new JsonResponder().accepts(MediaType.JSON.getType())).isTrue();
    }

    @Test
    public void acceptsEmpty() {
        assertThat(new JsonResponder().accepts("")).isFalse();
    }

    @Test
    public void acceptsNull() {
        assertThat(new JsonResponder().accepts(null)).isFalse();
    }

    @Test
    public void respond() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getMethod()).thenReturn(RequestMethod.GET.toString());
        new JsonResponder().respond(new Entity("Larry", 38), routeContext);
        verify(response).getWriter();
        verify(response).setCharacterEncoding("UTF-8");
        assertThat(stringWriter.toString()).isEqualTo("{\"name\":\"Larry\",\"age\":38}");
        verify(response).setContentType(MediaType.JSON.getType());
        verify(response).setHeader("Entity-Name", "Larry");
        verify(response).setHeader("Entity-Age", "38");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void errorResponse() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getMethod()).thenReturn(RequestMethod.GET.toString());

        ErrorResponseImpl errorResponse = new ErrorResponseImpl(HttpServletResponse.SC_NOT_FOUND, new Exception("not found"));
        new JsonResponder().respond(errorResponse, routeContext);
        verify(response).getWriter();
        verify(response).setCharacterEncoding("UTF-8");
        final ObjectMapper mapper = new ObjectMapper();
        final Map readValue = mapper.readValue(stringWriter.toString(), Map.class);
        assertThat(readValue.get("message")).isEqualTo("not found");
        verify(response).setContentType(MediaType.JSON.getType());
    }

}
