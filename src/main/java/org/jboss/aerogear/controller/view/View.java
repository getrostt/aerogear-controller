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

import org.jboss.aerogear.controller.util.TypeNameExtractor;

/**
 * A view in AeroGear consists of a path to a resource and optionally a model. 
 * </p> 
 * The path could be to a jsp page, or any other type of template language file.
 * 
 * @see ViewResolver
 */
public class View {
    private final String viewPath;
    private final Object model;
    private final TypeNameExtractor nameExtractor = new TypeNameExtractor();

    public View(String viewPath) {
        this(viewPath, null);
    }

    public View(String viewPath, Object model) {
        this.viewPath = viewPath;
        this.model = model;
    }

    public String getViewPath() {
        return viewPath;
    }

    public String getModelName() {
        if (hasModelData()) {
            return nameExtractor.nameFor(this.model.getClass());
        }
        return null;
    }

    public Object getModel() {
        return model;
    }

    public boolean hasModelData() {
        return this.model != null;
    }
}
