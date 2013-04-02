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

import static org.fest.assertions.Assertions.assertThat;

import org.jboss.aerogear.controller.router.parameter.Parameter;
import org.jboss.aerogear.controller.router.parameter.RequestParameter;
import org.junit.Test;

public class RequestParameterTest {

    @Test
    public void pathParameterWithName() {
        final RequestParameter<String> parameter = new RequestParameter<String>("model", Parameter.Type.REQUEST, String.class);
        assertThat(parameter.getName()).isEqualTo("model");
        assertThat(parameter.getParameterType()).isEqualTo(Parameter.Type.REQUEST);
        assertThat(parameter.getDefaultValue().isPresent()).isFalse();
    }

    @Test
    public void pathParameterWithDefaultValue() {
        final RequestParameter<String> parameter = new RequestParameter<String>("model", Parameter.Type.REQUEST, "def",
                String.class);
        assertThat(parameter.getName()).isEqualTo("model");
        assertThat(parameter.getParameterType()).isEqualTo(Parameter.Type.REQUEST);
        assertThat(parameter.getDefaultValue().get()).isEqualTo("def");
    }

    @Test
    public void pathParameterWithDefaultValueWithType() {
        final RequestParameter<Long> parameter = new RequestParameter<Long>("model", Parameter.Type.REQUEST, new Long(5),
                Long.class);
        assertThat(parameter.getName()).isEqualTo("model");
        assertThat(parameter.getParameterType()).isEqualTo(Parameter.Type.REQUEST);
        assertThat(parameter.getDefaultValue().get()).isEqualTo(new Long("5"));
    }

}
