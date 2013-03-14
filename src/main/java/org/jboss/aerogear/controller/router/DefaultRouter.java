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

package org.jboss.aerogear.controller.router;

import static org.jboss.aerogear.controller.util.RequestUtils.extractMethod;
import static org.jboss.aerogear.controller.util.RequestUtils.extractPath;
import static org.jboss.aerogear.controller.util.RequestUtils.extractAcceptHeader;

import java.util.Collections;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.util.RequestUtils;

/**
 * Default implementation of {@link Router}. 
 * </p> 
 * This implementation uses Context and Dependency Injection (CDI) to have various parts injected into it. Of<br>
 * particular interest for end users is the {@link RoutingModule} which is described in more detail in the section below.
 * 
 * <h3>RoutingModule</h3> 
 * The CDI implementation will scan for an instance of {@link RoutingModule} upon deployment, and its<br>
 * {@link RoutingModule#build()} method will be called to assemble the routes configured for this application.<br>
 * To simplify this process {@link AbstractRoutingModule} is provided, please refer its javadoc for sample usage.
 */
public class DefaultRouter implements Router {

    private Routes routes;
    private RouteProcessor routeProcessor;

    public DefaultRouter() {
    }

    @Inject
    public DefaultRouter(Instance<RoutingModule> instance, RouteProcessor routeProcessor) {
        this.routes = instance.isUnsatisfied() ? Routes.from(Collections.<RouteBuilder> emptyList()) : instance.get().build();
        this.routeProcessor = routeProcessor;
    }

    @Override
    public boolean hasRouteFor(HttpServletRequest request) {
        return routes.hasRouteFor(extractMethod(request), extractPath(request), extractAcceptHeader(request));
    }

    @Override
    public void dispatch(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException {
        try {
            String requestPath = RequestUtils.extractPath(request);
            Route route = routes.routeFor(extractMethod(request), requestPath, extractAcceptHeader(request));
            routeProcessor.process(new RouteContext(route, requestPath, request, response, routes));
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

}
