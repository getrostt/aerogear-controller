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

package org.jboss.aerogear.controller.router.rest;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.Responder;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.error.ErrorResponse;

/**
 * A RESTFul {@link Responder} that is able to return JSON responses. </p> This implementation uses Jackson for JSON support.
 */
public class JsonResponder extends AbstractRestResponder {

    private final ObjectMapper mapper;

    public JsonResponder() {
        mapper = new ObjectMapper();
    }

    @Override
    public void writeResponse(final Object entity, final RouteContext routeContext) throws Exception {
        if (entity instanceof ErrorResponse) {
            final ErrorResponse errorResponse = (ErrorResponse) entity;
            routeContext.getResponse().setStatus(errorResponse.statusCode());
            writeJsonResponse(errorResponse.content(), routeContext);
        } else {
            if (entity != null) {
                writeJsonResponse(entity, routeContext);
            }
        }
    }

    private void writeJsonResponse(final Object entity, final RouteContext routeContext) throws Exception {
        String requestMethod = routeContext.getRequest().getMethod() ;
        if (requestMethod != null && !requestMethod.equalsIgnoreCase(RequestMethod.OPTIONS.toString())) {
            mapper.writeValue(routeContext.getResponse().getWriter(), entity);
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.aerogear.controller.router.Responder#getMediaType()
     */
    @Override
    public MediaType getMediaType() {
        return MediaType.JSON;
    }

}
