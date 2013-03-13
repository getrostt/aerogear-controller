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
 * RouteBuilder builds a {@link Route} using a fluent API. 
 * </p> 
 * Sample Usage:
 * <pre>
 * {@code 
 *     RouteBuilder routeBuilder = ...;
 *     routeBuilder.from("/home")
 *                 .on(RequestMethod.GET)
 *                 .to(SampleController.class).index();
 *     Route route = routeBuilder.build();
 * }
 * </pre>
 */
public interface RouteBuilder {

    /**
     * Specifies that this route should be able to handle any of the types of exceptions passed-in. The method is used to define
     * an error route.
     * 
     * @param exception a single class of type, or subtype, {@link Throwable}.
     * @param exceptions zero or more classes of type, or subtypes, of {@link Throwable}.
     * @return {@link TargetEndpoint} to enable further configuration of the route, such as specifying the target class and
     *         method that will be called when the exception(s) are thrown.
     */
    TargetEndpoint on(Class<? extends Throwable> exception, Class<?>... exceptions);

    /**
     * Specifies the request path that the {@link Route} will handle request from.
     * 
     * @param path the request path that the {@link Route} will handle request from.
     * @return {@link OnMethods} which enables further specialization of the types of requests that can be handled by the
     *         {@link Route}.
     */
    OnMethods from(String path);

    /**
     * A fluent API for further specializing the {@link Route}'s destination/endpoint.
     */
    public static interface OnMethods {

        /**
         * Specifies which {@link RequestMethod}s should be supported by the {@link Route}.
         * 
         * @param methods the {@link RequestMethod}s that should be supported.
         * @return {@link TargetEndpoint} which is a builder for the destination for this {@link Route}.
         */
        TargetEndpoint on(RequestMethod... methods);

        /**
         * Specifies the roles that are allowed to invoke the target endpoint
         * 
         * @param roles the roles.
         * @return {@link OnMethods} to support method chaining.
         */
        OnMethods roles(String... roles);
    }

    /**
     * Describes the target destination for the {@link Route}.
     */
    public static interface TargetEndpoint {

        /**
         * Specifies the media types that this endpoint can consumes.
         * 
         * @param mediaTypes the media types that this endpoint method can consume.
         * @return {@link TargetEndpoint} to support method chaining.
         */
        TargetEndpoint consumes(String... mediaTypes);

        /**
         * Specifies the media types that this endpoint can consumes.
         * 
         * @param mediaTypes the media types that this endpoint method can consume.
         * @return {@link TargetEndpoint} to support method chaining.
         */
        TargetEndpoint consumes(MediaType... mediaTypes);

        /**
         * Specifies the MediaType's that this endpoint produces.
         * 
         * @param mediaTypes the {@link MediaType}s that this endpoint method can produce.
         * @return {@link TargetEndpoint} to support method chaining.
         */
        TargetEndpoint produces(MediaType... mediaTypes);

        /**
         * Specifies the target Class for the {@link Route}.
         * 
         * @param clazz The class that will be the used as the target endpoint by the {@link Route}.
         * @return T the type of the class.
         */
        <T> T to(Class<T> clazz);
    }

    /**
     * Builds a {@link Route} using the information gathered from this builder.
     * 
     * @return {@link Route} a Route instance configured by the this builder.
     */
    Route build();
}
