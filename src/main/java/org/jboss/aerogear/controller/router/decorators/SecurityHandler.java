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

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.jboss.aerogear.controller.router.InvocationResult;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.RouteProcessor;
import org.jboss.aerogear.controller.spi.SecurityProvider;

/**
 * SecurityHandler is a CDI Decorator that decorates a {@link RouteProcessor}. 
 */
@Decorator
public class SecurityHandler implements RouteProcessor {

    private final RouteProcessor delegate;
    private final SecurityProvider securityProvider;

    /**
     * Sole constructor which will have its parameters injected by CDI.
     * 
     * @param delegate the target {@link RouteProcessor}.
     * @param securityProviders the security provider to be used.
     */
    @Inject
    public SecurityHandler(final @Delegate RouteProcessor delegate, final Instance<SecurityProvider> securityProviders) {
        this.delegate = delegate;
        this.securityProvider = securityProviders.isUnsatisfied() ? defaultSecurityProvider() : securityProviders.get();
    }

    /**
     * This method will use the injected {@link SecurityProvider} to determine if access to the route is allowed. If access 
     * is allowed this methods simply delegates to the target {@link RouteProcessor}.
     * 
     * @throws Exception if access to the Route is denied.
     */
    @Override
    public InvocationResult process(final RouteContext routeContext) throws Exception {
        final Route route = routeContext.getRoute();
        if (route.isSecured()) {
            securityProvider.isRouteAllowed(route);
        }
        return delegate.process(routeContext);
    }

    private SecurityProvider defaultSecurityProvider() {
        return new SecurityProvider() {
            @Override
            public void isRouteAllowed(Route route) throws ServletException {
            }
        };
    }

}
