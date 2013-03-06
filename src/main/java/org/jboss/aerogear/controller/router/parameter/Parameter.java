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
 * Parameter represents a single target endpoint method parameter.
 * 
 * @param T the type of this parameter.
 */
public class Parameter<T> {

    public static <T> Parameter<T> param(final Class<T> type) {
        return new Parameter<T>(Type.ENTITY, type);
    }

    public static <T> Parameter<T> param(final String name, final Class<T> type) {
        return new RequestParameter<T>(name, Type.REQUEST, type);
    }

    public static <T> Parameter<T> param(final String name, final T defaultValue, final Class<T> type) {
        return new RequestParameter<T>(name, Type.REQUEST, defaultValue, type);
    }
    
    public static <T> Parameter<T> constant(final T value, final Class<T> type) {
        return new ConstantParameter<T>(value, type);
    }
    
    public static <T> Parameter<T> replacementParam(final String str, final Set<String> paramNames, final Class<T> type) {
        return new ReplacementParameter<T>(str, paramNames, type);
    }

    public enum Type {
        REQUEST, ENTITY, CONSTANT, REPLACEMENT
    }

    private final Type parameterType;
    private final Class<T> type;

    /**
     * Sole constructor.
     * 
     * @param parameterType the {@link Type} of request parameter.
     * @param type the expected type of the value of the request parameter. 
     */
    public Parameter(final Type parameterType, final Class<T> type) {
        this.parameterType = parameterType;
        this.type = type;
    }

    /**
     * Gets the {@link Type} of this parameter. The parameter migth come from a request query param, a form param,
     * a header param, a cookie param, or could be in the body of the request.
     * 
     * @return {@code Type} the {@link Type} of this parameter.
     */
    public Type getParameterType() {
        return parameterType;
    }

    /**
     * Gets the type of this parameter. This is the type of the value of the {@link Type}, for example the {@code Type} might
     * be a query param (REQUEST) and the value of that query param might be of type String.class.
     * 
     * @return {@code Class} the class type of this parameter.
     */
    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Parameter[type=" + parameterType + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameterType == null) ? 0 : parameterType.hashCode());
        result = prime * result + ((type == null) ? 0 : type.getName().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Parameter<?> other = (Parameter<?>) obj;
        if (parameterType != other.parameterType) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.getName().equals(other.type.getName())) {
            return false;
        }
        return true;
    }

}
