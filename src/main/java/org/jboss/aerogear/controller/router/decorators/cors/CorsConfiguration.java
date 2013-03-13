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

package org.jboss.aerogear.controller.router.decorators.cors;

import java.util.List;
import java.util.Set;

/**
 * Configuration for Cross-Origin Resource Sharing (CORS).
 */
public interface CorsConfiguration {

    /**
     * Determines is support for CORS is enabled.
     * 
     * @return {@code true} if support for CORS is enabled, false otherwise.
     */
    boolean isCorsSupportEnabled();

    /**
     * Determines if there are any headers configured to be exposed to calling clients. 
     * </p>
     * 
     * @see #getExposeHeaders()
     * 
     * @return {@code true} if there are headers that should be exposed to clients.
     */
    boolean exposeHeaders();

    /**
     * Returns a comma separated string of headers to be exposed to calling clients. 
     * </p> 
     * During a simple CORS request only certain response headers are made available to a calling client:
     * <ul>
     * <li>Cache-Control</li>
     * <li>Content-Language</li>
     * <li>Content-Type</li>
     * <li>Expires</li>
     * <li>Last-Modified</li>
     * <li>Pragma</li>
     * </ul>
     * To expose other headers they need to be specified which what this method enables. 
     * </p>
     * 
     * @return {@code String} a comma separated string of headers to be exposed.
     */
    List<String> getExposeHeaders();

    /**
     * Determines is any origin, "*", is supported.
     * 
     * @return {@code true} if any origin is allowed.
     */
    boolean anyOrigin();

    /**
     * Determines if cookies are supported. 
     * </p> 
     * By default cookies are not included in CORS requests if allowCookies is true cookies will be added to CORS requests.
     * 
     * @return {@code true} if cookies are supported.
     */
    boolean allowCookies();

    /**
     * Determines is maxAge has been configured.
     * 
     * @see #getMaxAge()
     * 
     * @return {@code true} if maxAge has been set.
     */
    boolean hasMaxAge();

    /**
     * Gets the maxAge setting. 
     * </p> 
     * When making a preflight request the client has to perform two request with can be inefficient. This setting enables the 
     * caching of the preflight response for the specified time. During this time no preflight request will be made. 
     * </p>
     * 
     * @return {@code long} the time in seconds that a preflight request may be cached.
     */
    long getMaxAge();

    /**
     * Returns the allowed set of Request Methods.
     * 
     * @return {@code Set} strings that represent the allowed Request Methods.
     */
    Set<String> getValidRequestMethods();

    /**
     * Returns the allowed set of Request Headers.
     * 
     * @return {@code Set} of strings that represent the allowed Request Headers.
     */
    List<String> getValidRequestHeaders();

}