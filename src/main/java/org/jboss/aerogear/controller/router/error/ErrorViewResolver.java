/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.aerogear.controller.router.error;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.view.ViewResolver;

/**
 * A {@link ViewResolver} that decorates a ViewResolver to enable short-circuiting the call to resolve the views path. 
 * </p>
 * This {@link ViewResolver} used only for error handling and specifically in the situation when a custom error route has not
 * been defined.
 */
public class ErrorViewResolver implements ViewResolver {

    private final ViewResolver delegate;

    /**
     * Sole constructor.
     * 
     * @param delegate the {@link ViewResolver} that this instance will delegate to.
     */
    public ErrorViewResolver(final ViewResolver delegate) {
        checkNotNull(delegate, "'delegate' ViewResolver must not be null");
        this.delegate = delegate;
    }

    /**
     * Returns the path of the passed-in {@link Route} if it is a default error Route, and if not, will let the delegate
     * handle resolving the path for the Route
     * 
     * @param route the route for which a view is to be resolved
     * @return {@code String} the view for the passed in Route.
     */
    @Override
    public String resolveViewPathFor(final Route route) {
        if (route.getTargetClass().equals(ErrorTarget.class)) {
            return route.getPath();
        }
        return delegate.resolveViewPathFor(route);
    }

}
