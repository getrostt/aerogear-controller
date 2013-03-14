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

package org.jboss.aerogear.controller.router.error;

import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.controller.filter.ErrorFilter;
import org.jboss.aerogear.controller.router.DefaultRoute;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteDescriptor;

/**
 * A singleton {@link Route} that acts as a catch-all error {@link Route} which is will be used when no explicit error route has
 * been defined.
 */
public enum ErrorRoute {

    DEFAULT("org.jboss.aerogear.controller.exception");

    private final Route route;
    private final String exceptionAttributeName;

    @SuppressWarnings("unchecked")
    private ErrorRoute(final String exceptionAttributeName) {
        this.exceptionAttributeName = exceptionAttributeName;
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath(ErrorFilter.class.getAnnotation(WebFilter.class).urlPatterns()[0]);
        rd.setThrowables(new HashSet<Class<? extends Throwable>>(Arrays.asList(Throwable.class)));
        rd.on(RequestMethod.GET);
        rd.produces(ErrorViewResponder.MEDIA_TYPE);
        rd.to(ErrorTarget.class).error(param(Throwable.class));
        route = new DefaultRoute(rd);
    }

    /**
     * Returns an {@link Route} which is configured to route to an instance of {@link ErrorTarget}.
     * 
     * @return {@link Route} provided as a fallback when a route has no explicit error route defined.
     */
    public Route getRoute() {
        return route;
    }

    /**
     * Returns the name of the request attribute for this ErrorRoute, which will be accessible by calling
     * {@link HttpServletRequest#getAttribute(String)} method.
     * 
     * @return String the name of the request attribute to get hold of the target exception.
     */
    public String getExceptionAttrName() {
        return exceptionAttributeName;
    }

    private <T> T param(Class<T> type) {
        return null;
    }

}
