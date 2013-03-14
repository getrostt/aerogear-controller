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

package org.jboss.aerogear.controller.view;

import org.jboss.aerogear.controller.view.ViewResolver;
import org.jboss.aerogear.controller.router.Route;

/**
 * An Abstract {@link ViewResolver} pattern for resolving a view is as follows:
 * <pre>
 * /WEB-INF/pages/TargetClassName/TargetMethodName<.suffix>
 * </pre>
 * 
 * @see View
 */
public abstract class AbstractViewResolver implements ViewResolver {

    private static final String DEFAULT_PREFIX = "/WEB-INF/pages";
    private final String suffix;

    public AbstractViewResolver(final String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String resolveViewPathFor(Route route) {
        String folder = route.getTargetClass().getSimpleName();
        String name = route.getTargetMethod().getName();
        return String.format("%s/%s/%s%s", DEFAULT_PREFIX, folder, name, suffix);
    }
}
