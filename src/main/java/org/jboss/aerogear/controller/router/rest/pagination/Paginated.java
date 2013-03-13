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
package org.jboss.aerogear.controller.router.rest.pagination;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
/**
 * Specifies that target endpoint method in AeroGear Controller returns a paginated response.
 * </p>
 * The annotation can take optional configuration setting to configure the names of query parameters used, there default 
 * values if they are missing from the request, and also how links to next/previous should be returned to the client.
 * 
 */
public @interface Paginated {

    /**
     * The name of query parameter that will be used as the 'offset' in a pagination strategy using offset/limit.
     * 
     * @return {@code String} the name of the query parameter used for the 'offset'. If not specified defaults to 'offset'.
     */
    String offsetParamName() default "offset";

    /**
     * The value to be used as the default 'offset' if no query parameter by the name specified in 'offsetParamName' is included
     * in the HTTP Request.
     * 
     * @return {@code int} the value to be used as the default 'offset' if no query parameter named 'offsetParamName' was
     *         included in the request.
     */
    int defaultOffset() default 0;

    /**
     * The name of query parameter that will be used as the 'limit' in a pagination strategy using offset/limit.
     * 
     * @return {@code String} the name of the query parameter used for the 'limit'. If not specified defaults to 'limit'.
     */
    String limitParamName() default "limit";

    /**
     * The value to be used as the default 'limit' if no query parameter by the name specified in 'limitParamName' is included
     * in the HTTP Request.
     * 
     * @return {@code int} the value to be used as the default 'limit' if no query parameter named 'limitParamName' was included
     *         in the request.
     */
    int defaultLimit() default 10;

    /**
     * The prefix to be used when custom HTTP response headers are used to provide link to next/previous. 
     * </p> 
     * For example, if you specify the prefix as 'XYZ-" the following response headers could possibly be returned:
     * <pre>
     * XYZ-Links-Next "http://server/app/resource?offset=5?limit=5"
     * XYZ-Links-Previous "http://server/app/resource?offset=0?limit=5"
     * </pre>
     * 
     * Note, that this value is only used when Web Linking is disabled (webLinking = false).
     * 
     * @return {@code String} the prefix to be used for next/previous HTTP response headers. Defaults to 'AG-'.
     */
    String customHeadersPrefix() default "AG-";

    /**
     * Determines if <a href="http://tools.ietf.org/html/draft-nottingham-http-link-header-10">Web Linking</a> should be used.
     * 
     * @return {@code true} if Web Linking should be used, or false if custom HTTP headers should be used.
     */
    boolean webLinking() default true;
}
