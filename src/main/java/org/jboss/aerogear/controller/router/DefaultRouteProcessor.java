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

import static org.jboss.aerogear.controller.util.ParameterExtractor.extractArguments;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Default implementation of {@link RouteProcessor} that is the target of a processing chain of CDI decorators. 
 * </p> 
 */
public class DefaultRouteProcessor implements RouteProcessor {

    private EndpointInvoker endpointInvoker;
    private final Map<String, Consumer> consumers = new HashMap<String, Consumer>();

    /**
     * No-args constructor required by CDI so that it can generate proxies for instances of this class.
     */
    public DefaultRouteProcessor() {
    }

    @Inject
    public DefaultRouteProcessor(Instance<Consumer> consumers, EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
        for (Consumer consumer : consumers) {
            this.consumers.put(consumer.mediaType(), consumer);
        }
    }

    @Override
    public InvocationResult process(RouteContext routeContext) throws Exception {
        final Map<String, Object> arguments = extractArguments(routeContext, consumers);
        return new InvocationResult(endpointInvoker.invoke(routeContext, arguments.values().toArray()), routeContext);
    }

}
