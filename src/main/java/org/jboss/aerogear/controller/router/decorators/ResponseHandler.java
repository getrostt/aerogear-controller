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
import javax.inject.Inject;

import org.jboss.aerogear.controller.router.InvocationResult;
import org.jboss.aerogear.controller.router.Responders;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.RouteProcessor;

/**
 * ResponseHandler is a CDI Decorator that decorates a {@link RouteProcessor} and is responsible for handling
 * the result from the invoked endpoint. 
 */
@Decorator
public class ResponseHandler implements RouteProcessor {

    private final RouteProcessor delegate;
    private final Responders responders;

    /**
     * Sole contructor which will have its parameters injected by CDI.
     * 
     * @param delegate the {@link RouteProcessor} that this class decorates.
     * @param responders the {@link Responders} class that will handle the response processing.
     */
    @Inject
    public ResponseHandler(final @Delegate RouteProcessor delegate, final Responders responders) {
        this.delegate = delegate;
        this.responders = responders;
    }

    @Override
    public InvocationResult process(final RouteContext routeContext) throws Exception {
        final InvocationResult result = delegate.process(routeContext);
        responders.respond(result.getRouteContext(), result.getResult());
        return result;
    }

}
