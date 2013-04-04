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

package org.jboss.aerogear.controller.router.decorators;

import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.aerogear.controller.router.MediaType.JSON;
import static org.jboss.aerogear.controller.router.MediaType.JSP;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.SampleController;
import org.jboss.aerogear.controller.SampleControllerException;
import org.jboss.aerogear.controller.mocks.RouteTester;
import org.jboss.aerogear.controller.router.AbstractRoutingModule;
import org.jboss.aerogear.controller.router.InvocationResult;
import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.error.ErrorViewResolver;
import org.junit.Test;

public class ErrorHandlerTest {

    @Test 
    public void testOnException() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() throws Exception {
                route()
                        .on(IllegalStateException.class)
                        .produces(JSP)
                        .to(SampleController.class).errorPage();
                route()
                        .from("/home")
                        .on(RequestMethod.GET, RequestMethod.POST)
                        .to(SampleController.class).throwIllegalStateException();
            }
        }).acceptHeader(JSP).spyController(new SampleController());
        routeTester.processGetRequest("/home");
        verify(routeTester.<SampleController>getController()).errorPage();
        verify(routeTester.jspResponder()).respond(anyObject(), any(RouteContext.class));
    }

    @Test
    public void testOnExceptions() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() throws Exception {
                route()
                        .on(SampleControllerException.class, IllegalStateException.class)
                        .produces(JSP)
                        .to(SampleController.class).error(param(Exception.class));
                route().from("/home").on(RequestMethod.GET, RequestMethod.POST).to(SampleController.class)
                        .throwSampleControllerException();
            }
        }).acceptHeader(MediaType.JSP).spyController(new SampleController());
        routeTester.processGetRequest("/home");
        verify(routeTester.<SampleController>getController()).error(any(IllegalArgumentException.class));
        verify(routeTester.jspResponder()).respond(anyObject(), any(RouteContext.class));
        verify(routeTester.jsonResponder(), never()).respond(anyObject(), any(RouteContext.class));
    }

    @Test
    public void testDefaultErrorRoute() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() throws Exception {
                route()
                        .from("/home").on(RequestMethod.GET, RequestMethod.POST)
                        .to(SampleController.class).throwSampleControllerException();
            }
        }).acceptHeader(JSP).spyController(new SampleController());
        routeTester.processGetRequest("/home");
        verify(routeTester.getErrorTarget()).error(any(SampleControllerException.class));
        verify(routeTester.errorViewResponder()).respond(anyObject(), any(RouteContext.class));
        assertThat(routeTester.errorViewResponder().getViewResolver()).isInstanceOf(ErrorViewResolver.class);
    }

    @Test
    public void testJsonResponseOnException() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() throws Exception {
                route()
                        .on(IllegalStateException.class)
                        .produces(JSON)
                        .to(SampleController.class).errorResponse();
                route()
                        .from("/home")
                        .on(RequestMethod.GET, RequestMethod.POST)
                        .produces(JSON)
                        .to(SampleController.class).throwIllegalStateException();
            }
        }).acceptHeader(JSON).spyController(new SampleController());
        final InvocationResult processResult = routeTester.processGetRequest("/home");
        verify(routeTester.<SampleController>getController()).errorResponse();
        verify(routeTester.jsonResponder()).respond(anyObject(), any(RouteContext.class));
        verify(processResult.getRouteContext().getResponse()).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertThat(routeTester.getStringWriter().toString()).isEqualTo("[]");
    }
    
    @Test
    public void testDefaultErrorRouteJsonRequested() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() throws Exception {
                route()
                        .from("/home")
                        .on(RequestMethod.GET)
                        .produces(JSON)
                        .to(SampleController.class).throwIllegalStateException();
            }
        }).acceptHeader(JSON).spyController(new SampleController());
        routeTester.processGetRequest("/home");
        verify(routeTester.getErrorTarget()).error(any(SampleControllerException.class));
        verify(routeTester.errorViewResponder()).respond(anyObject(), any(RouteContext.class));
    }
    
}
