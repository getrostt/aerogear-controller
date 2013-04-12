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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.aerogear.controller.log.ExceptionBundle;
import org.jboss.aerogear.controller.util.RequestUtils;

import com.google.common.base.Optional;

/**
 * Handles responding from a Route invocation by delegating to the appropriate {@link Responder}.
 * 
 * @see Responder
 */
public class Responders {

    private final Map<MediaType, Responder> responders = new LinkedHashMap<MediaType, Responder>();

    @Inject
    public Responders(final Instance<Responder> responders) {
        for (Responder responder : responders) {
            this.responders.put(responder.getMediaType(), responder);
        }
    }

    /**
     * Responds to the Route in the passed-in RouteContext using an appropriate Responder. 
     * </p> 
     * The {@link Responder} used to respond is determined by inspecting the HTTP Accept header values and matching these with 
     * the media types that the Route is capable of producing ({@link Route#produces()}) <br>
     * If no match is found for the values in the Accept header, or if the Accept header was empty or "*&#47;*" then any
     * Responder that accepts "*&#47;*" will be used to respond.
     * 
     * @param routeContext the current route context
     * @param result the result from invoking the Route's target endpoint method.
     * @throws Exception if an exception is thrown while trying to respond.
     */
    public void respond(final RouteContext routeContext, final Object result) throws Exception {
        final Set<String> acceptHeaders = RequestUtils.extractAcceptHeader(routeContext.getRequest());
        final Set<MediaType> routeMediaTypes = routeContext.getRoute().produces();
        final Optional<MediaType> optionalMediaType = RequestUtils.getAcceptedMediaType(acceptHeaders, routeMediaTypes);
        if (optionalMediaType.isPresent()) {
            if (respond(optionalMediaType.get(), result, routeContext)) {
                return;
            }
        }

        if (acceptHeaders.contains(MediaType.ANY) || acceptHeaders.isEmpty()) {
            respondAny(routeMediaTypes, result, routeContext);
        } else {
            throw ExceptionBundle.MESSAGES.noResponderForRequestedMediaType(routeContext.getRequest().getHeader("Accept"), this);
        }
    }

    private boolean respond(final MediaType mediaType, final Object result, final RouteContext routeContext) throws Exception {
        if (responders.containsKey(mediaType)) {
            responders.get(mediaType).respond(result, routeContext);
            return true;
        }
        return false;
    }

    private void respondAny(final Set<MediaType> mediaTypes, final Object result, final RouteContext routeContext)
            throws Exception {
        for (MediaType mediaType : mediaTypes) {
            if (respond(mediaType, result, routeContext)) {
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Responders[" + responders + "]";
    }

}
