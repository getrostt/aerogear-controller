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

import static com.google.common.base.Objects.firstNonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.aerogear.controller.router.parameter.Parameter;
import org.jboss.aerogear.controller.util.RequestUtils;

/**
 * An immutable implementation of {@link Route}.
 */
public class DefaultRoute implements Route {
    private final String path;
    private final Class<?> targetClass;
    private final Method targetMethod;
    private final Set<RequestMethod> methods;
    private final Set<String> roles;
    private final Set<String> consumes;
    private final Set<MediaType> produces;
    private final Set<Class<? extends Throwable>> throwables;
    private final List<Parameter<?>> parameters;

    /**
     * Constructs a Route with the specified {@code RouteDescriptor} configuration options.
     * 
     * @param descriptor the {@link RouteDescriptor} with the configured values.
     */
    public DefaultRoute(RouteDescriptor descriptor) {
        path = descriptor.getPath();
        methods = asSet(descriptor.getMethods());
        targetMethod = descriptor.getTargetMethod();
        targetClass = descriptor.getTargetClass();
        roles = asSet(firstNonNull(descriptor.getRoles(), new String[] {}));
        consumes = asSet(descriptor.getConsumes(), MediaType.HTML.getType());
        parameters = firstNonNull(descriptor.getParameters(), Collections.<Parameter<?>> emptyList());
        produces = asSet(firstNonNull(descriptor.getProduces(), defaultMediaTypes()));
        throwables = firstNonNull(descriptor.getThrowables(), emptyThrowableSet());
    }

    @Override
    public Set<RequestMethod> getMethods() {
        return Collections.unmodifiableSet(methods);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    @Override
    public boolean matches(RequestMethod method, String path, Set<String> acceptHeaders) {
        return this.methods.contains(method) && isPathCompatible(path) && matchesProduces(acceptHeaders);
    }

    private boolean matchesProduces(final Set<String> acceptHeaders) {
        if (acceptHeaders.isEmpty() || acceptHeaders.contains(MediaType.ANY)) {
            return true;
        }
        for (MediaType mediaType : produces) {
            if (acceptHeaders.contains(mediaType.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPathCompatible(String realPath) {
        return RequestUtils.segmentsMatch(this.path, realPath);
    }

    @Override
    public Method getTargetMethod() {
        return targetMethod;
    }

    @Override
    public boolean isParameterized() {
        return path.contains("{");
    }

    @Override
    public boolean isSecured() {
        return !roles.isEmpty();
    }

    @Override
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Override
    public boolean hasExceptionsRoutes() {
        return !throwables.isEmpty();
    }

    @Override
    public Set<MediaType> produces() {
        return Collections.unmodifiableSet(produces);
    }

    @Override
    public List<Parameter<?>> getParameters() {
        return Collections.<Parameter<?>> unmodifiableList(parameters);
    }

    public Set<String> consumes() {
        return Collections.unmodifiableSet(consumes);
    }

    @Override
    public boolean canHandle(final Throwable throwable) {
        for (Class<? extends Throwable> t : throwables) {
            if (t.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder("DefaultRoute[").append("path=").append(path)
                .append(", targetClass=").append(targetClass)
                .append(", targetMethod=").append(targetMethod)
                .append(", produces=").append(produces)
                .append(", parameters=").append(parameters)
                .append(", roles=").append(roles)
                .append(", throwables=").append(throwables)
                .append("]").toString();
    }

    private Set<RequestMethod> asSet(final RequestMethod[] methods) {
        return methods == null ? Collections.<RequestMethod> emptySet() : new HashSet<RequestMethod>(Arrays.asList(methods));
    }

    private Set<String> asSet(final String[] strings) {
        return new HashSet<String>(Arrays.asList(strings));
    }

    private Set<MediaType> asSet(final MediaType[] types) {
        return new LinkedHashSet<MediaType>(Arrays.asList(types));
    }

    private Set<String> asSet(final List<String> strings, final String defaultValue) {
        return strings.isEmpty() ? new HashSet<String>(Arrays.asList(defaultValue)) : new LinkedHashSet<String>(strings);
    }

    private static Set<Class<? extends Throwable>> emptyThrowableSet() {
        return Collections.emptySet();
    }

    private static MediaType[] defaultMediaTypes() {
        return new MediaType[] { MediaType.JSP };
    }

}
