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

/**
 * A constant parameter is specified directly to an endpoint method (not using that param("id") method that is).
 * </p>
 * A ConstantParameter only has a type and a value.
 * 
 */
public class ConstantParameter<T> extends Parameter<T> {

    private final T value;

    public ConstantParameter(final T value, final Class<T> type) {
        super(Parameter.Type.CONSTANT, type);
        this.value = value;
    }
    
    public T getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "ConstantParameter[value=" + value + ", type=" + getType() + "]";
    }

}
