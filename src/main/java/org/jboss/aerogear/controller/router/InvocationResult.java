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

/**
 * Represents the result of invoking a Route in AeroGear Controller.
 */
public class InvocationResult {

    private final Object result;
    private final RouteContext routeContext;

    /**
     * Sole constructor.
     * 
     * @param result the result from invoking the route's target endpoint method, if any.
     * @param routeContext the {@link RouteContext} for the current route.
     */
    public InvocationResult(final Object result, final RouteContext routeContext) {
        this.result = result;
        this.routeContext = routeContext;
    }

    /**
     * Gets the result from this invocation.
     * 
     * @return {@code Object} the result for this invocation.
     */
    public Object getResult() {
        return result;
    }

    /**
     * The {@code RouteContext} for this invocation.
     * 
     * @return {@code RouteContext} the {@link RouteContext} for this invocation.
     */
    public RouteContext getRouteContext() {
        return routeContext;
    }
    
    public String toString() {
        return "InvocationResult[result=" + result + ", context=" + routeContext + "]";
    }

}
