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
package org.jboss.aerogear.controller.router.parameter;

import java.util.Set;


/**
 * Represents a single string parameter containing variable replacements ({paramName}).
 * </p>
 */
public class ReplacementParameter<T> extends Parameter<T> {

    private final Set<String> paramNames;
    private final String str;

    public ReplacementParameter(final String str, final Set<String> paramNames, final Class<T> type) {
        super(Parameter.Type.REPLACEMENT, type);
        this.paramNames = paramNames;
        this.str = str;
    }
    
    public Set<String> getParamNames() {
        return paramNames;
    }
    
    public String getString() {
        return str;
    }
    
    @Override
    public String toString() {
        return "ReplacementParameter[paramNames=" + paramNames + ", str=" + str + ", type=" + getType() + "]";
    }

}
