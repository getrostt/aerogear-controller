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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.router.RouteContext;

/**
 * A PaginationStrategy that expects the target endpoint method to take a {@link PaginationInfo} as a parameter. 
 * </p> 
 * Subclasses can implement {@link #setResponseHeaders(PaginationMetadata, HttpServletResponse, int)} to set the HTTP Response 
 * headers.
 */
public abstract class AbstractPaginationStrategy implements PaginationStrategy {

    public abstract void setResponseHeaders(final PaginationMetadata metadata, final HttpServletResponse response,
            final int resultSize);

    @Override
    public PaginationInfo createPaginationInfo(final RouteContext routeContext, final Map<String, Object> args) {
        final Paginated paginated = routeContext.getRoute().getTargetMethod().getAnnotation(Paginated.class);
        final String customHeader = paginated.customHeadersPrefix();
        return PaginationInfo.offset(paginated.offsetParamName(), argAsInt(args, paginated.offsetParamName()))
                .limit(paginated.limitParamName(), argAsInt(args, paginated.limitParamName()))
                .customHeadersPrefix(customHeader).webLinking(paginated.webLinking()).build();
    }

    @Override
    public Object[] preInvocation(final PaginationInfo paginationInfo, final Map<String, Object> arguments) {
        return merge(paginationInfo, arguments).toArray();
    }

    private List<Object> merge(final PaginationInfo paginationInfo, final Map<String, Object> arguments) {
        final List<Object> methodArguments = new ArrayList<Object>();
        arguments.remove(paginationInfo.getOffsetParamName());
        arguments.remove(paginationInfo.getLimitParamName());
        methodArguments.add(paginationInfo);
        methodArguments.addAll(arguments.values());
        return methodArguments;
    }

    @Override
    public Object postInvocation(final Collection<?> results, final RouteContext routeContext, final PaginationInfo pagingInfo) {
        final PaginationMetadata pagingMetadata = createMetadata(routeContext, pagingInfo);
        setResponseHeaders(pagingMetadata, routeContext.getResponse(), results.size());
        return results;
    }

    private PaginationMetadata createMetadata(final RouteContext routeContext, final PaginationInfo pagingInfo) {
        final RequestPathParser requestPathParser = new RequestPathParser(pagingInfo, getResourcePath(routeContext));
        final PaginationProperties pagingProperties = new PaginationProperties(pagingInfo.getOffset(), pagingInfo.getLimit());
        if (pagingInfo.webLinking()) {
            return new PaginationMetadata(pagingProperties, requestPathParser);
        } else {
            return new PaginationMetadata(pagingProperties, requestPathParser, pagingInfo.getHeaderPrefix().get());
        }
    }

    private String getResourcePath(final RouteContext routeContext) {
        final HttpServletRequest request = routeContext.getRequest();
        final String resourcePath = request.getRequestURL().toString() + "?" + routeContext.getRequest().getQueryString();
        if (resourcePath.startsWith("/")) {
            return resourcePath.substring(1);
        }
        return resourcePath;
    }

    private int argAsInt(final Map<String, Object> args, final String argName) {
        return Integer.valueOf((String) args.get(argName)).intValue();
    }

}
