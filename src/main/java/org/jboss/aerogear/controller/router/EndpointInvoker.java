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

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * EndpointInvoker is responsible for invoking target endpoint methods in AeroGear Controller.
 */
public class EndpointInvoker {

    private final BeanManager beanManager;
    private final ControllerFactory controllerFactory;

    @Inject
    public EndpointInvoker(final ControllerFactory controllerFactory, final BeanManager beanManager) {
        this.controllerFactory = controllerFactory;
        this.beanManager = beanManager;
    }

    /**
     * Invokes the target endpoint method for the passed-in {@code RouteContext}.
     * 
     * @param routeContext the {@link RouteContext} for route to be invoked.
     * @param args the arguments for the route's target endpoint method.
     * @return {@code Object} the result from invoking the endpoint method, if any.
     * @throws Exception if an error occurs while invoking the target method.
     */
    public Object invoke(final RouteContext routeContext, final Object[] args) throws Exception {
        final Route route = routeContext.getRoute();
        return route.getTargetMethod().invoke(getController(route), args);
    }

    private Object getController(Route route) {
        return controllerFactory.createController(route.getTargetClass(), beanManager);
    }

}
