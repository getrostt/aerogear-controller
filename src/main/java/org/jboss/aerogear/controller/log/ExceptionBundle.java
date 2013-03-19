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

package org.jboss.aerogear.controller.log;

import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletException;

import org.jboss.aerogear.controller.router.Consumer;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.Responders;
import org.jboss.aerogear.controller.router.parameter.MissingRequestParameterException;
import org.jboss.aerogear.controller.router.parameter.Parameter;
import org.jboss.aerogear.controller.router.rest.pagination.PaginationRequestException;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * A JBoss-Logging MessageBundle containing translated Strings, Exceptions etc. 
 * </p> 
 * Contains no methods that perform logging. Refer to {@link AeroGearLogger} for that.
 */
@MessageBundle(projectCode = "AG_CONTROLLER")
public interface ExceptionBundle {
    ExceptionBundle MESSAGES = Messages.getBundle(ExceptionBundle.class);

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 1, value = "must be run inside a Servlet container")
    ServletException mustRunInsideAContainer();

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 4, value = "No route found for method: '%s', requested URI: '%s', Accept: '%s'")
    RuntimeException routeNotFound(RequestMethod method, String requestURI, Set<String> acceptHeaders);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 9, value = "oops, multivalued params not supported yet. Parameter name: '%s'")
    RuntimeException multivaluedParamsUnsupported(String parameterName);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 10, value = "Parameter: '%s' was missing from Request")
    MissingRequestParameterException missingParameterInRequest(String paramName);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 11, value = "No Responder was found that matched the Accept Header: '%s'. The following Responders are registered: '%s'")
    RuntimeException noResponderForRequestedMediaType(String acceptHeader, Responders responders);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 12, value = "No Consumer found for Parameter: '%s'. The registered Consumers were: '%s'. Please add a Consumer for one the media types supported by the route: %s.")
    RuntimeException noConsumerForMediaType(Parameter<?> parameter, Collection<Consumer> consumers,
            Set<String> supportedMediaTypes);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 13, value = "Invalid Paging Request: offset '%s', limit '%s'")
    PaginationRequestException invalidPagingRequest(long offset, long limit);
    
    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 14, value = "The class '%s', does not have a no-args constructor which is required by CDI.")
    RuntimeException responderDoesNotHaveNoArgsCtor(Class<?> javaClass);
    
    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 15, value = "No routes were found in the deployment. Please verify that you have implemented RoutingModule, and that the deployment has a beans.xml file in WEB-INF")
    RuntimeException noRoutesAvailable();

}
