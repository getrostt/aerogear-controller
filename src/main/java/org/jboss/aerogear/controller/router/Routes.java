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

import org.jboss.aerogear.controller.log.AeroGearLogger;
import org.jboss.aerogear.controller.log.ExceptionBundle;
import org.jboss.aerogear.controller.router.error.ErrorRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Routes is a collection of {@link Route} instances that are able to handle certain {@link RequestMethod}/requestURI
 * combinations. 
 * </p>
 * 
 * This class also provides static factory methods for creating Routes instances.
 */
public class Routes {

    private final List<Route> routes = new ArrayList<Route>();

    private Routes(List<RouteBuilder> routeBuilders) {
        for (RouteBuilder routeBuilder : routeBuilders) {
            routes.add(routeBuilder.build());
        }
    }

    /**
     * Simple Factory method for creating a {@link RouteBuilder} which is used to define a route.
     * 
     * @return {@link RouteBuilder} which can be used to gather information about a Route.
     */
    public static RouteBuilder route() {
        return new RouteBuilderImpl();
    }

    /**
     * Factory method that constructs a {@link Routes} instance using the list of {@link RouteBuilder}s provided.
     * 
     * @param routes the list of {@link RouteBuilder}s which will be used to create the {@link Route}s.
     * @return {@link Routes} with the {@link Route}s from the passed in list of {@link RouteBuilder}s.
     */
    public static Routes from(List<RouteBuilder> routes) {
        return new Routes(routes);
    }

    @Override
    public String toString() {
        return "Routes{" + "routes=" + routes + '}';
    }

    /**
     * Determines is there is a Route for the {@link RequestMethod}/URI combination.
     * 
     * @param method the HTTP {@link RequestMethod}.
     * @param requestURI the URI.
     * @param acceptHeaders the accept headers provided, or an empty set if none were provided.
     * @return {@code true} if this Routes has a {@link Route} for the specified {@link RequestMethod}/URI combination
     */
    public boolean hasRouteFor(RequestMethod method, String requestURI, Set<String> acceptHeaders) {
        AeroGearLogger.LOGGER.requestedRoute(method, requestURI);
        for (Route route : routes) {
            if (route.matches(method, requestURI, acceptHeaders)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@link Route} for the specified {@link RequestMethod}/URI combination.
     * 
     * @param method the HTTP {@link RequestMethod}.
     * @param requestURI the URI.
     * @param acceptHeaders the accept headers provided, or an empty set if none were provided.
     * @return {@link Route} configured to server the {@link RequestMethod}/URI combination. Will throw a RuntimeException if
     *         the specified RequestMethod/URI combination is not supported by this Routes instance.
     */
    public Route routeFor(RequestMethod method, String requestURI, Set<String> acceptHeaders) {
        for (Route route : routes) {
            if (route.matches(method, requestURI, acceptHeaders)) {
                return route;
            }
        }
        throw ExceptionBundle.MESSAGES.routeNotFound(method, requestURI, acceptHeaders);
    }

    /**
     * Returns the {@link Route} for the specified {@link Throwable}.
     * 
     * @param throwable the {@link Throwable} to match with a {@link Route}
     * @return {@link Route} an error {@link Route} that can the type of the passed-in {@link Throwable}, or if no error route
     *         was specified a {@link ErrorRoute#DEFAULT} will be returned.
     */
    public Route routeFor(Throwable throwable) {
        for (Route route : routes) {
            if (route.canHandle(throwable)) {
                return route;
            }
        }
        return ErrorRoute.DEFAULT.getRoute();
    }
}
