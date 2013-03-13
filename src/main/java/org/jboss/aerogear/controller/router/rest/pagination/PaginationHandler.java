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
package org.jboss.aerogear.controller.router.rest.pagination;

import static org.jboss.aerogear.controller.util.ParameterExtractor.extractArguments;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.router.Consumer;
import org.jboss.aerogear.controller.router.EndpointInvoker;
import org.jboss.aerogear.controller.router.InvocationResult;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.RouteProcessor;

/**
 * PaginationHandler is a CDI Decorator that decorates a {@link RouteProcessor} and is responsible for handling
 * paginated invocations on endpoints that support it.
 */
@Decorator
public class PaginationHandler implements RouteProcessor {

    private final RouteProcessor delegate;
    private final PaginationStrategy pagingStrategy;
    private final Map<String, Consumer> consumers = new HashMap<String, Consumer>();
    private final EndpointInvoker endpointInvoker;

    /**
     * Sole contructor which will have its parameters injected by CDI.
     * 
     * @param delegate the {@link RouteProcessor} that this class decorates.
     * @param pagingStrategies a CDI {@link Instance} of {@link PaginationStrategy}s enabling the strategy to be configured.
     * @param consumers CDI {@link Instance} of {@link Consumer} that are used for unmarshalling a HTTP request body into
     *      a Java Object representation. 
     * @param endpointInvoker {@link EndpointInvoker} which is responsible for invoking endpoints.
     */
    @Inject
    public PaginationHandler(final @Delegate RouteProcessor delegate, final Instance<PaginationStrategy> pagingStrategies,
            final Instance<Consumer> consumers, final EndpointInvoker endpointInvoker) {
        this.delegate = delegate;
        this.pagingStrategy = pagingStrategies.isUnsatisfied() ? defaultPagingStrategy() : pagingStrategies.get();
        this.endpointInvoker = endpointInvoker;
        for (Consumer consumer : consumers) {
            this.consumers.put(consumer.mediaType(), consumer);
        }
    }

    @Override
    public InvocationResult process(final RouteContext routeContext) throws Exception {
        if (hasPaginatedAnnotation(routeContext.getRoute())) {
            final Map<String, Object> requestArgs = extractArguments(routeContext, consumers);
            final PaginationInfo paginationInfo = pagingStrategy.createPaginationInfo(routeContext, requestArgs);
            final Object[] args = pagingStrategy.preInvocation(paginationInfo, requestArgs);
            final Collection<?> results = (Collection<?>) endpointInvoker.invoke(routeContext, args);
            return new InvocationResult(pagingStrategy.postInvocation(results, routeContext, paginationInfo), routeContext);
        } else {
            return delegate.process(routeContext);
        }
    }

    private boolean hasPaginatedAnnotation(final Route route) {
        return route.getTargetMethod().getAnnotation(Paginated.class) != null;
    }

    public static PaginationStrategy defaultPagingStrategy() {
        return new AbstractPaginationStrategy() {
            @Override
            public void setResponseHeaders(final PaginationMetadata metadata, final HttpServletResponse response,
                    final int resultSize) {
                for (Entry<String, String> entry : metadata.getHeaders(resultSize).entrySet()) {
                    response.setHeader(entry.getKey(), entry.getValue());
                }
            }
        };
    }

}
