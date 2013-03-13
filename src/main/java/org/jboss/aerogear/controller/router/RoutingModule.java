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

/**
 * A RoutingModule is used by the AeroGear runtime to retrieve the routes defined by the current application. 
 * </p>
 * 
 * End users may implement this interface directly or use {@link AbstractRoutingModule} which has convenience methods.
 * 
 * @see AbstractRoutingModule
 */
public interface RoutingModule {

    /**
     * Returns a {@link Routes} instance containing all the {@link Route}s configured in the application.
     * 
     * @return {@link Routes} populated with all the configured {@link Route}s.
     */
    Routes build();
}
