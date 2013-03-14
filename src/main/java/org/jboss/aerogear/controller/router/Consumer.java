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

import javax.servlet.http.HttpServletRequest;

/**
 * A Consumer is capable of unmarshalling an Http Request Body into a Java Object representation.
 */
public interface Consumer {

    /**
     * The media type that this consumer can handle.
     * 
     * @return {@code String} the media type that this consumer can handle.
     */
    public String mediaType();

    /**
     * Will unmarshall the the HttpServletRequest into an instance of type T
     * 
     * @param request the {@link HttpServletRequest}.
     * @param type the type that the request should be unmarshalled to.
     * @return {@code T} an instance of type T.
     */
    <T> T unmarshall(HttpServletRequest request, Class<T> type);

}
