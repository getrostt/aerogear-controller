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
 * A Responder is capable of responding to a specific MediaType.
 */
public interface Responder {

    /**
     * The media type that this Responder can handle.
     * 
     * @return {@code MediaType} the media type.
     */
    MediaType getMediaType();

    /**
     * Determines whether this responder can respond to the passed-in @{code mediaType}
     * 
     * @param mediaType the mediaType that this responder supports.
     * @return @{code true} if this responder can handle the media type passed in, false otherwise.
     */
    boolean accepts(final String mediaType);

    /**
     * Responds to the current request in a why appropriate to the type of Responder (forward, return).
     * 
     * @param entity the entity returned from a {@link Route} endpoint.
     * @param routeContext the current {@link RouteContext}.
     * @throws Exception if an error occurs while responding.
     */
    void respond(final Object entity, final RouteContext routeContext) throws Exception;

}
