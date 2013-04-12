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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.enterprise.inject.Instance;

import org.jboss.aerogear.controller.router.Consumer;
import org.jboss.aerogear.controller.router.DefaultRouteProcessor;
import org.jboss.aerogear.controller.router.EndpointInvoker;
import org.jboss.aerogear.controller.router.InvocationResult;
import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.Responder;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.RouteProcessor;
import org.jboss.aerogear.controller.router.Routes;
import org.jboss.aerogear.controller.router.RoutingModule;
import org.jboss.aerogear.controller.router.decorators.ErrorHandler;
import org.jboss.aerogear.controller.router.decorators.ResponseHandler;
import org.jboss.aerogear.controller.router.decorators.SecurityHandler;
import org.jboss.aerogear.controller.router.error.ErrorTarget;
import org.jboss.aerogear.controller.router.error.ErrorViewResponder;
import org.jboss.aerogear.controller.router.rest.JsonConsumer;
import org.jboss.aerogear.controller.router.rest.JsonResponder;
import org.jboss.aerogear.controller.router.rest.pagination.PaginationHandler;
import org.jboss.aerogear.controller.router.rest.pagination.PaginationStrategy;
import org.jboss.aerogear.controller.spi.SecurityProvider;
import org.jboss.aerogear.controller.util.RequestUtils;
import org.jboss.aerogear.controller.view.JspViewResponder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RouteTester {

    @Mock
    private Instance<Consumer> consumers;
    @Mock
    private SecurityProvider securityProvider;
    @Mock
    private Instance<SecurityProvider> securityProviderInstance;
    @Mock
    private Instance<PaginationStrategy> pagingInstance;
    private final MockRequest mockRequest;
    private final MockResponder mockResponders;
    private final MockEndpointInvoker mockInvoker;
    private final Routes routes;
    private Object controller;
    private RouteProcessor routeProcessor;

    private RouteTester(final RoutingModule routingModule) {
        MockitoAnnotations.initMocks(this);
        routes = routingModule.build();
        mockResponders = new MockResponder();
        mockRequest = new MockRequest();
        mockInvoker = new MockEndpointInvoker();
        instrumentConsumers();
        instrumentSecurityProviders();
        instrumentPagination();
    }

    public static RouteTester from(final RoutingModule routingModule) {
        return new RouteTester(routingModule);
    }

    public RouteTester addResponder(final Responder responder) {
        mockResponders.addResponder(responder);
        return this;
    }

    public RouteTester requestMethod(final RequestMethod httpMethod) {
        mockRequest.requestMethod(httpMethod);
        return this;
    }

    public RouteTester acceptHeader(final String acceptHeader) {
        mockRequest.acceptHeader(acceptHeader);
        return this;
    }

    public RouteTester acceptHeader(final MediaType mediaType) {
        mockRequest.acceptHeader(mediaType.getType());
        return this;
    }

    public RouteTester body(final String body) {
        mockRequest.body(body);
        return this;
    }

    public Route routeFor(final String path) {
        mockRequest.setRequestURI(path);
        mockRequest.setRequestURL(path);
        return routes.routeFor(RequestMethod.valueOf(mockRequest.getRequest().getMethod()), path,
                RequestUtils.extractAcceptHeader(mockRequest.getRequest()));
    }

    public InvocationResult processGetRequest(final String path) throws Exception {
        requestMethod(RequestMethod.GET);
        return process(path);
    }

    public InvocationResult processPostRequest(final String path) throws Exception {
        requestMethod(RequestMethod.POST);
        return process(path);
    }

    public InvocationResult process(final String path) throws Exception {
        final String trimmed = mockRequest.extractQueryParameters(path);
        final Route route = routeFor(trimmed);
        return process(route);
    }

    public InvocationResult process(final Route route) throws Exception {
        setController(route);
        mockRequest.prepareProcessing();
        final RouteContext routeContext = new RouteContext(route, mockRequest.getRequest(), mockRequest.getResponse(), routes);
        return createRouteProcessor().process(routeContext);
    }

    private RouteProcessor createRouteProcessor() {
        if (routeProcessor == null) {
            final EndpointInvoker endpointInvoker = mockInvoker.getEndpointInvoker();
            final RouteProcessor defaultRouteProcessor = new DefaultRouteProcessor(consumers, endpointInvoker);
            final RouteProcessor paginationHandler = new PaginationHandler(defaultRouteProcessor, pagingInstance, consumers,
                    endpointInvoker);
            final RouteProcessor securityHandler = new SecurityHandler(paginationHandler, securityProviderInstance);
            final RouteProcessor errorHandler = new ErrorHandler(securityHandler, endpointInvoker);
            routeProcessor = new ResponseHandler(errorHandler, mockResponders.getResponders());
            return routeProcessor;
        } else {
            return routeProcessor;
        }
    }

    public void servletContext(final String context) {
        mockRequest.servletContext(context);
    }

    @SuppressWarnings("unchecked")
    public <T> T getController() {
        return (T) controller;
    }

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    public JsonResponder jsonResponder() {
        return mockResponders.getJsonResponder();
    }

    public JspViewResponder jspResponder() {
        return mockResponders.getJspResponder();
    }

    public ErrorViewResponder errorViewResponder() {
        return mockResponders.getErrorViewResponder();
    }

    public RouteTester param(final String name, final String value) {
        mockRequest.param(name, value);
        return this;
    }

    public RouteTester header(final String name, final String value) {
        mockRequest.header(name, value);
        return this;
    }
    
    public RouteTester contentType(final String value) {
        mockRequest.contentType(value);
        return this;
    }

    public RouteTester cookie(final String name, final String value) {
        mockRequest.cookie(name, value);
        return this;
    }

    private void instrumentPagination() {
        when(pagingInstance.isUnsatisfied()).thenReturn(true);
    }

    private void instrumentSecurityProviders() {
        final Iterator<SecurityProvider> iterator = new HashSet<SecurityProvider>(Arrays.asList(securityProvider)).iterator();
        when(securityProviderInstance.iterator()).thenReturn(iterator);
        when(securityProviderInstance.get()).thenReturn(securityProvider);
    }

    private void instrumentConsumers() {
        final Iterator<Consumer> iterator = new HashSet<Consumer>(Arrays.asList(new JsonConsumer())).iterator();
        when(consumers.iterator()).thenReturn(iterator);
    }

    public RouteTester spyController(final Object controller) {
        this.controller = spy(controller);
        return this;
    }

    public RouteTester setController(final Object controller) {
        this.controller = controller;
        return this;
    }

    private void setController(Route route) {
        controller = mockInvoker.setController(controller, route);
    }

    public ErrorTarget getErrorTarget() {
        return mockInvoker.getErrorTarget();
    }

    public StringWriter getStringWriter() {
        return mockRequest.getStringWriter();
    }

    public RouteProcessor getRouteProcessor() {
        return spy(createRouteProcessor());
    }

    static {
        InputStream inputStream = null;
        try {
            inputStream = RouteTester.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (final IOException e) {
            Logger.getAnonymousLogger().severe("Could not load logging.properties file: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

}
