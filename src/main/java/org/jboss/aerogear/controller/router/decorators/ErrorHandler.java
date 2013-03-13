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

import java.lang.reflect.Method;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.jboss.aerogear.controller.router.EndpointInvoker;
import org.jboss.aerogear.controller.router.InvocationResult;
import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.RouteProcessor;
import org.jboss.aerogear.controller.router.error.ErrorRoute;
import org.jboss.aerogear.controller.spi.HttpStatusAwareException;

import com.google.common.base.Throwables;

/**
 * ErrorHandler is a CDI Decorator that decorates a {@link RouteProcessor}. 
 * </p> 
 * By wrapping the call to {@link RouteProcessor#process(RouteContext)} with a try catch block, this class will handle any 
 * exception thrown and either forward to a the appropriate error route configured, or if no error route exists, forward to 
 * the default error view.
 */
@Decorator
public class ErrorHandler implements RouteProcessor {

    private final RouteProcessor delegate;
    private EndpointInvoker endpointInvoker;

    @Inject
    public ErrorHandler(final @Delegate RouteProcessor delegate, final EndpointInvoker endpointInvoker) {
        this.delegate = delegate;
        this.endpointInvoker = endpointInvoker;
    }

    @Override
    public InvocationResult process(final RouteContext routeContext) throws Exception {
        try {
            return delegate.process(routeContext);
        } catch (final Throwable t) {
            if (t instanceof HttpStatusAwareException) {
                routeContext.getResponse().setStatus(((HttpStatusAwareException) t).getStatus());
            }
            final Throwable rootCause = Throwables.getRootCause(t);
            final RouteContext errorContext = errorContext(rootCause, routeContext);
            final Object result = invokeErrorMethod(errorContext, rootCause);
            routeContext.getRequest().setAttribute(ErrorRoute.DEFAULT.getExceptionAttrName(), rootCause);
            return new InvocationResult(result, errorContext);
        }
    }

    private Object invokeErrorMethod(final RouteContext errorContext, final Throwable rootCause) throws Exception {
        return endpointInvoker.invoke(errorContext, getMethodArguments(errorContext, rootCause));
    }
    
    private Object[] getMethodArguments(final RouteContext routeContext, final Throwable t) {
        final Method targetMethod = routeContext.getRoute().getTargetMethod();
        return targetMethod.getParameterTypes().length == 0 ? new Object[] {} : new Object[] { t };
    }

    private RouteContext errorContext(final Throwable rootCause, final RouteContext orgContext) {
        final Route errorRoute = orgContext.getRoutes().routeFor(rootCause);
        return new RouteContext(errorRoute, wrapRequest(orgContext), orgContext.getResponse(), orgContext.getRoutes());
    }
    
    private HttpServletRequest wrapRequest(final RouteContext routeContext) {
        final String acceptAny = appendAnyMediaTypeToAcceptHeader(routeContext.getRequest());
        return new HttpServletRequestWrapper(routeContext.getRequest()) {
            @Override
            public String getHeader(final String name) {
                if (name.equalsIgnoreCase("accept")) {
                    return acceptAny;
                }
                return super.getHeader(name);
            }
        };
    }
    
    private String appendAnyMediaTypeToAcceptHeader(final HttpServletRequest request) {
        final String acceptHeader = request.getHeader("Accept");
        return acceptHeader == null ? MediaType.ANY : acceptHeader + "," + MediaType.ANY;
    }


}
