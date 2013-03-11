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

package org.jboss.aerogear.controller.util;

import br.com.caelum.iogi.Iogi;
import br.com.caelum.iogi.reflection.Target;
import br.com.caelum.iogi.util.DefaultLocaleProvider;
import br.com.caelum.iogi.util.NullDependencyProvider;

import com.google.common.base.Optional;

import org.jboss.aerogear.controller.log.AeroGearLogger;
import org.jboss.aerogear.controller.log.ExceptionBundle;
import org.jboss.aerogear.controller.router.Consumer;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.rest.pagination.PaginationInfo;
import org.jboss.aerogear.controller.util.StringUtils;
import org.jboss.aerogear.controller.router.parameter.Parameter;
import org.jboss.aerogear.controller.router.parameter.RequestParameter;

import javax.servlet.http.Cookie;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ParameterExtractor {

    private static final Iogi IOGI = new Iogi(new NullDependencyProvider(), new DefaultLocaleProvider());

    /**
     * Extracts the arguments from the current request for the target route.
     * 
     * @param routeContext the {@link org.jboss.aerogear.controller.router.RouteContext}.
     * @return {@code Object[]} an array of Object matching the route targets parameters.
     */
    public static Map<String, Object> extractArguments(final RouteContext routeContext, final Map<String, Consumer> consumers) throws Exception {
        final Map<String, Object> args = new LinkedHashMap<String, Object>();
        for (Parameter<?> parameter : routeContext.getRoute().getParameters()) {
            switch (parameter.getParameterType()) {
                case ENTITY:
                    if (PaginationInfo.class.isAssignableFrom(parameter.getType())) {
                        break;
                    }
                    if (!addIfPresent(extractIogiParam(routeContext), "entity", args)) {
                        args.put("entity", extractBody(routeContext, parameter, consumers));
                    }
                    break;
                case REQUEST:
                    final RequestParameter<?> requestParameter = (RequestParameter<?>) parameter;
                    if (addIfPresent(extractParam(routeContext, requestParameter), requestParameter.getName(), args)) {
                        break;
                    }
                    if (addIfPresent(extractHeaderParam(routeContext, requestParameter), requestParameter.getName(), args)) {
                        break;
                    }
                    if (addIfPresent(extractCookieParam(routeContext, requestParameter), requestParameter.getName(), args)) {
                        break;
                    }
                    if (addIfPresent(requestParameter.getDefaultValue(), requestParameter.getName(), args)) {
                        break;
                    }
                    if (addIfPresent(extractPathParam(routeContext,parameter.getType()), requestParameter.getName(), args)) {
                        break;
                    }
                    throw ExceptionBundle.MESSAGES.missingParameterInRequest(requestParameter.getName());
            }
        }
        return args;
    }

    private static Object extractBody(final RouteContext routeContext, final Parameter<?> parameter,
            final Map<String, Consumer> consumers) {
        final Set<String> mediaTypes = routeContext.getRoute().consumes();
        for (String mediaType : mediaTypes) {
            final Consumer consumer = consumers.get(mediaType);
            if (consumer != null) {
                return consumer.unmarshall(routeContext.getRequest(), parameter.getType());
            }
        }
        throw ExceptionBundle.MESSAGES.noConsumerForMediaType(parameter, consumers.values(), mediaTypes);
    }

    /**
     * Extracts a path parameter from the passed in request path.
     * 
     *
     * @param routeContext the {@link org.jboss.aerogear.controller.router.RouteContext} to extract a path parameter from.
     * @param type
     * @return {@code Optional<String>} containing the extracted path param if present in the request path.
     */
    public static Optional<?> extractPathParam(final RouteContext routeContext, Class<?> type) throws Exception {
        final String requestPath = routeContext.getRequestPath();
        final int paramOffset = routeContext.getRoute().getPath().indexOf('{');
        if (paramOffset != -1 && paramOffset < requestPath.length()) {
            String pathParam = requestPath.subSequence(paramOffset, requestPath.length()).toString();
            return Optional.of(createInstance(type,pathParam));
        }
        return Optional.absent();
    }

    /**
     * Returns an instance of the type used in the parameter names using Iogi. </p> For example, having form parameters named
     * 'car.color', 'car.brand', this method would try to use those values to instantiate a new Car instance.
     * 
     * @return {@link com.google.common.base.Optional} may contain the instantiated instance, else isPresent will return false.
     */
    public static Optional<?> extractIogiParam(final RouteContext routeContext) {
        final LinkedList<br.com.caelum.iogi.parameters.Parameter> parameters = new LinkedList<br.com.caelum.iogi.parameters.Parameter>();
        for (Map.Entry<String, String[]> entry : routeContext.getRequest().getParameterMap().entrySet()) {
            final String[] value = entry.getValue();
            if (value.length == 1) {
                parameters.add(new br.com.caelum.iogi.parameters.Parameter(entry.getKey(), value[0]));
            } else {
                AeroGearLogger.LOGGER.multivaluedParamsUnsupported();
            }
        }
        if (!parameters.isEmpty()) {
            final Class<?>[] parameterTypes = routeContext.getRoute().getTargetMethod().getParameterTypes();
            final Class<?> parameterType = parameterTypes[0];
            final Target<?> target = Target.create(parameterType, StringUtils.downCaseFirst(parameterType.getSimpleName()));
            return Optional.fromNullable(IOGI.instantiate(target,
                    parameters.toArray(new br.com.caelum.iogi.parameters.Parameter[parameters.size()])));
        }
        return Optional.absent();
    }

    private static boolean addIfPresent(final Optional<?> op, final String paramName, final Map<String, Object> args) {
        if (op.isPresent()) {
            args.put(paramName, op.get());
            return true;
        }
        return false;
    }

    private static Optional<?> extractHeaderParam(final RouteContext routeContext, final RequestParameter<?> parameter) {
        return Optional.fromNullable(routeContext.getRequest().getHeader(parameter.getName()));
    }

    private static Optional<?> extractCookieParam(final RouteContext routeContext, final RequestParameter<?> parameter) {
        final Cookie[] cookies = routeContext.getRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(parameter.getName())) {
                    return Optional.fromNullable(cookie.getValue());
                }
            }
        }
        return Optional.absent();
    }

    private static Optional<?> extractParam(final RouteContext routeContext, final RequestParameter<?> parameter) {
        final String[] values = routeContext.getRequest().getParameterMap().get(parameter.getName());
        if (values != null) {
            if (values.length == 1) {
                return Optional.of(values[0]);
            } else {
                throw ExceptionBundle.MESSAGES.multivaluedParamsUnsupported(parameter.getName());
            }
        }
        return Optional.absent();
    }

    private static Object createInstance( Class<?> type, String arg) throws Exception {
         Constructor constructor = type.getDeclaredConstructor(String.class);
         return constructor.newInstance(arg);
    }
 }
