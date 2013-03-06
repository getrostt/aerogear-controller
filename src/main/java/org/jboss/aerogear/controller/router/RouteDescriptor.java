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

import static org.jboss.aerogear.controller.router.parameter.Parameter.constant;
import static org.jboss.aerogear.controller.router.parameter.Parameter.param;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.jboss.aerogear.controller.router.RouteBuilder.TargetEndpoint;
import org.jboss.aerogear.controller.router.parameter.Parameter;
import org.jboss.aerogear.controller.router.rest.pagination.Paginated;
import org.jboss.aerogear.controller.util.RequestUtils;

/**
 * Describes/configures a single route in AeroGear controller.
 */
public class RouteDescriptor implements RouteBuilder.OnMethods, RouteBuilder.TargetEndpoint {
    private String path;
    private Method targetMethod;
    private Object[] args;
    private RequestMethod[] methods;
    private Class<?> targetClass;
    private String[] roles;
    private final List<String> consumes = new LinkedList<String>();
    private final List<Parameter<?>> parameters = new LinkedList<Parameter<?>>();
    private MediaType[] produces;
    private Set<Class<? extends Throwable>> throwables;
    private final static FinalizeFilter FINALIZE_FILTER = new FinalizeFilter();

    public RouteDescriptor() {
    }

    /**
     * Set the path for this instance. </p> A RouteDescriptor may have an empty path if it is an error route.
     * 
     * @param path the from path for this route.
     */
    public RouteDescriptor setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public RouteBuilder.TargetEndpoint on(RequestMethod... methods) {
        this.methods = methods;
        return this;
    }

    @Override
    public RouteBuilder.OnMethods roles(String... roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public <T> T to(Class<T> clazz) {
        this.targetClass = clazz;
        try {
            Object o = Enhancer.create(clazz, null, FINALIZE_FILTER, new Callback[] { new MyMethodInterceptor(this),
                    NoOp.INSTANCE });
            return (T) o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPath() {
        return path;
    }

    public RequestMethod[] getMethods() {
        return methods;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String[] getRoles() {
        return roles;
    }

    private static class MyMethodInterceptor implements MethodInterceptor {
        private final RouteDescriptor routeDescriptor;

        public MyMethodInterceptor(RouteDescriptor routeDescriptor) {
            this.routeDescriptor = routeDescriptor;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            this.routeDescriptor.targetMethod = method;
            this.routeDescriptor.args = args;
            
            final List<Parameter<?>> methodParams = new LinkedList<Parameter<?>>();
            processPaginatedAnnotation(method, methodParams);
            mergeRequestParamsWithConstants(args, methodParams);
            return null;
        }
        
        private void processPaginatedAnnotation(Method method, List<Parameter<?>> methodParams) {
            if (method.getAnnotation(Paginated.class) != null) {
                final Paginated paginated = method.getAnnotation(Paginated.class);
                methodParams.add(param(paginated.offsetParamName(), String.valueOf(paginated.defaultOffset()), String.class));
                methodParams.add(param(paginated.limitParamName(), String.valueOf(paginated.defaultLimit()), String.class));
            }
        }
    
        /*
         * Request parameters represent parameters that were specified using the param("identifier")
         * method. Those methods are called prior to this interceptor method since they are parameters
         * to the target method. So those parameters have already been added to the underlying route descriptors
         * parameter list. Below, we are combining those params with any constant parameter that were supplied.
         */
        private void mergeRequestParamsWithConstants(Object[] args, List<Parameter<?>> destination) {
            final List<Parameter<?>> requestParams = routeDescriptor.getParameters();
            final boolean hasRequestParams = !requestParams.isEmpty();
            for (int i = 0, requestParam = 0; i < args.length; i++ ) {
                final Object arg = args[i];
                if (arg == null && hasRequestParams) {
                    destination.add(requestParams.get(requestParam++));
                } else {
                    if (arg instanceof String) {
                        final String str = (String) arg;
                        final Set<String> extractParams = RequestUtils.extractPlaceHolders(str);
                        if (!extractParams.isEmpty()) {
                            destination.add(Parameter.replacementParam(str, extractParams, Set.class));
                            continue;
                        } 
                    } 
                    destination.add(constant(arg, Object.class));
                }
            }
            requestParams.clear();
            requestParams.addAll(destination);
        }
    }

    @Override
    public String toString() {
        return "RouteDescriptor{" + "path='" + path + '\'' + ", targetMethod=" + targetMethod + ", args="
                + (args == null ? null : Arrays.asList(args)) + '}';
    }

    public RouteDescriptor setThrowables(Set<Class<? extends Throwable>> throwables) {
        this.throwables = throwables;
        return this;
    }

    public Set<Class<? extends Throwable>> getThrowables() {
        return throwables;
    }

    public List<Parameter<?>> getParameters() {
        return parameters;
    }

    @Override
    public TargetEndpoint produces(MediaType... produces) {
        this.produces = produces;
        return this;
    }

    public MediaType[] getProduces() {
        return produces;
    }

    @Override
    public TargetEndpoint consumes(String... consumes) {
        this.consumes.addAll(Arrays.asList(consumes));
        return this;
    }

    @Override
    public TargetEndpoint consumes(MediaType... consumes) {
        this.consumes.addAll(toStrings(consumes));
        return this;
    }

    private List<String> toStrings(MediaType... mediaTypes) {
        final List<String> strings = new LinkedList<String>();
        for (MediaType mediaType : mediaTypes) {
            strings.add(mediaType.getType());
        }
        return strings;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void addParameter(final Parameter<?> parameter) {
        parameters.add(parameter);
    }

    private static class FinalizeFilter implements CallbackFilter {

        /* Indexes into the callback array */
        private static final int OUR_INTERCEPTOR = 0;
        private static final int NO_OP = 1;

        @Override
        public int accept(Method method) {
            return method.getName().equals("finalize") ? NO_OP : OUR_INTERCEPTOR;
        }
    }

}
