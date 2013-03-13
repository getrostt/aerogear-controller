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

package org.jboss.aerogear.controller.router.decorators.cors;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.util.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Cors is a helper class for handling Cross-Origin Resource Sharing (CORS) in AeroGear Controller.
 */
public class Cors {

    public enum RequestHeader {
        ORIGIN("Origin"), 
        OPTIONS("OPTIONS"), 
        METHOD("Access-Control-Request-Method"), 
        HEADERS("Access-Control-Request-Headers");

        private final String headerName;

        private RequestHeader(final String headerName) {
            this.headerName = headerName;
        }

        @Override
        public String toString() {
            return headerName;
        }
    }

    public enum ResponseHeader {
        ALLOW_ORIGIN("Access-Control-Allow-Origin"), 
        ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"), 
        EXPOSE_HEADERS("Access-Control-Expose-Headers"), 
        ALLOW_METHODS("Access-Control-Allow-Methods"), 
        MAX_AGE("Access-Control-Max-Age"), 
        ALLOW_HEADERS("Access-Control-Allow-Headers");

        private final String headerName;

        private ResponseHeader(final String headerName) {
            this.headerName = headerName;
        }

        @Override
        public String toString() {
            return headerName;
        }
    }

    private final HttpServletRequest request;
    private final CorsConfiguration corsConfig;

    /**
     * Sole constructor.
     * 
     * @param corsConfig The {@link CorsConfig} containing configuration options.
     * @param request The {@link HttpServletRequest} that this instance will use.
     */
    public Cors(final CorsConfiguration corsConfig, final HttpServletRequest request) {
        this.request = request;
        this.corsConfig = corsConfig;
    }

    /**
     * Determines if the current {@link HttpServletRequest} is a CORS request. 
     * </p> 
     * See <a href="http://www.w3.org/TR/cors/#http-origin">http-origin</a> section of the specification.
     * 
     * @return {@code true} if the current request has an 'Origin' request header, otherwise false.
     */
    public boolean isCorsRequest() {
        return hasOriginHeader();
    }

    /**
     * Determines if this instance can handle CORS requests. 
     * </p> 
     * This is simply a convenience method and is the equivalent of calling {@link Cors#isCorsSupportEnabled()} 
     * && {@link Cors#isCorsRequest()}
     * 
     * @return {@code true} is CORS support has been enabled and if the current request is a CORS request.
     * 
     */
    public boolean canHandleRequest() {
        return isCorsSupportEnabled() && isCorsRequest();
    }

    /**
     * Determines if core support has been enabled by the {@link CorsConfig} instance.
     * 
     * @return {@code true} if support for CORS is enabled.
     */
    public boolean isCorsSupportEnabled() {
        return corsConfig.isCorsSupportEnabled();
    }

    /**
     * Determines if the current {@link HttpServletRequest} has an 'Origin' request header.
     * 
     * @return {@code true} if the current request has an 'Origin' request header, otherwise false.
     */
    public boolean hasOriginHeader() {
        return hasHeader(RequestHeader.ORIGIN.toString());
    }

    /**
     * Determines if the the current {@link HttpServletRequest}'s http method is 'OPTIONS'.
     * 
     * @return {@code true} if the current request' http method is 'OPTIONS', otherwise false.
     */
    public boolean isOptionsMethod() {
        return request.getMethod().equals(RequestHeader.OPTIONS.toString());
    }

    /**
     * Determines if the current {@link HttpServletRequest} qualifies as a 'Preflight' request. 
     * </p> 
     * See <a href="http://www.w3.org/TR/cors/#preflight-request">preflight-request</a> section of the specification.
     * 
     * @return {@code true} if the current request qualifies as a preflight, otherwise false.
     */
    public boolean isPreflightRequest() {
        return hasOriginHeader() && isOptionsMethod() && hasHeader(RequestHeader.METHOD.toString());
    }

    /**
     * Checks that the preflight request method ({@link RequestHeader#METHOD}) is supported.
     * 
     * @param validMethods a set of methods that are allowed.
     * @return {@code true} if the current request method is one of the allowed http methods.
     */
    public boolean isRequestMethodValid(final Set<String> validMethods) {
        final String method = request.getHeader(RequestHeader.METHOD.toString());
        return validMethods.contains(method);
    }

    /**
     * Checks that the preflight request method ({@link RequestHeader#METHOD}) is supported according to the
     * {@link CorsConfiguration} settings.
     * 
     * @return {@code true} if the current request method is one of the allowed http methods.
     */
    public boolean isRequestMethodValid() {
        return isRequestMethodValid(corsConfig.getValidRequestMethods());
    }

    /**
     * Determines if the current {@link HttpServletRequest} has a {@link RequestHeader#HEADERS} request header.
     * 
     * @return {@code true} if the current request methods has a RequestHeader.HEADERS header.
     */
    public boolean hasRequestHeaders() {
        return hasHeader(RequestHeader.HEADERS.toString());
    }

    /**
     * Returns the {@link RequestHeader#HEADERS} request header.
     * 
     * @return {@code String} if the current request methods has a RequestHeader.HEADERS header.
     */
    public String getRequestHeaders() {
        return request.getHeader(RequestHeader.HEADERS.toString());
    }

    /**
     * Returns the {@link RequestHeader#METHOD} request header.
     * 
     * @return {@code true} if the current request methods has a RequestHeader.HEADERS header.
     */
    public String getRequestMethod() {
        return request.getHeader(RequestHeader.METHOD.toString());
    }

    /**
     * Returns the allowed set of Request Methods.
     * 
     * @return {@code Set} of allowed Request Methods.
     */
    public Set<String> getAllowedRequestMethods() {
        return corsConfig.getValidRequestMethods();
    }

    /**
     * Returns the allowed set of Request Headers.
     * 
     * @return {@code Set} of allowed Request Headers.
     */
    public List<String> getAllowedRequestHeaders() {
        return corsConfig.getValidRequestHeaders();
    }

    /**
     * Set the {@link ResponseHeader#ALLOW_ORIGIN} to be the same value that was passed in the 'Origin' request header.
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader.ALLOW_ORIGIN should be set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setEchoOrigin(final HttpServletResponse response) {
        return setOrigin(response, request.getHeader(RequestHeader.ORIGIN.headerName));
    }

    /**
     * Set the {@link ResponseHeader#ALLOW_ORIGIN} to either echo the 'Origin' or to support '*' depending on the underlying
     * {@link CorsConfiguration} setting.
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader.ALLOW_ORIGIN should be set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setOrigin(final HttpServletResponse response) {
        if (corsConfig.anyOrigin()) {
            setAnyOrigin(response);
        } else {
            setEchoOrigin(response);
        }
        return this;
    }

    /**
     * Set the {@link ResponseHeader#ALLOW_ORIGIN} to the passed in value.
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader.ALLOW_ORIGIN should be set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setOrigin(final HttpServletResponse response, final String origin) {
        if (hasOriginHeader()) {
            response.setHeader(ResponseHeader.ALLOW_ORIGIN.toString(), origin);
        }
        return this;
    }

    /**
     * Set the {@link ResponseHeader#ALLOW_ORIGIN} to '*'.
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader.ALLOW_ORIGIN should be set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setAnyOrigin(final HttpServletResponse response) {
        return setOrigin(response, "*");
    }

    /**
     * Set the {@link ResponseHeader#ALLOW_CREDENTIALS} to 'true' if allowCookies was set set to true in the underlying
     * {@link CorsConfiguration}. </p> By default cookies are not included in CORS requests but by setting this header cookies
     * will be added to CORS request.
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader.ALLOW_CREDENTIALS should be
     *        set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setAllowCredentials(final HttpServletResponse response) {
        if (corsConfig.allowCookies()) {
            response.setHeader(ResponseHeader.ALLOW_CREDENTIALS.toString(), Boolean.TRUE.toString());
        }
        return this;
    }

    /**
     * Set the {@link ResponseHeader#EXPOSE_HEADERS} to the the configured comma separated list of headers. 
     * </p> 
     * During a simple CORS request only certain response headers are made available to a calling client:
     * <ul>
     * <li>Cache-Control</li>
     * <li>Content-Language</li>
     * <li>Content-Type</li>
     * <li>Expires</li>
     * <li>Last-Modified</li>
     * <li>Pragma</li>
     * </ul>
     * To expose other headers they need to be specified which what this method enables. 
     * </p>
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader#EXPOSE_HEADERS should be
     *        set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setExposeHeaders(final HttpServletResponse response) {
        if (corsConfig.exposeHeaders()) {
            final List<String> headers = corsConfig.getExposeHeaders();
            if (headers != null) {
                response.setHeader(ResponseHeader.EXPOSE_HEADERS.toString(), asString(headers));
            }
        }
        return this;
    }

    /**
     * Set the {@link ResponseHeader#ALLOW_METHODS} to the the configured comma separated list of http methods.
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader.ALLOW_METHODS should be set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setAllowMethods(final HttpServletResponse response) {
        final Set<String> httpMethods = corsConfig.getValidRequestMethods();
        if (httpMethods != null) {
            response.setHeader(ResponseHeader.ALLOW_METHODS.toString(), asString(httpMethods));
        }
        return this;
    }

    /**
     * Set the {@link ResponseHeader#MAX_AGE} to the configured max age value. 
     * </p> 
     * When making a preflight request the client has to perform two request with can be inefficient. This setting enables 
     * the caching of the preflight response for the specified time. During this time no preflight request will be made. 
     * </p>
     * 
     * @param response the {@link HttpServletResponse} for which the response header ResponseHeader.MAX_AGE should be set.
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setMaxAge(final HttpServletResponse response) {
        if (corsConfig.hasMaxAge()) {
            response.setHeader(ResponseHeader.MAX_AGE.toString(), Long.toString(corsConfig.getMaxAge()));
        }
        return this;
    }

    /**
     * Checks that the preflight request headers ({@link RequestHeader#HEADERS}) are supported.
     * 
     * @param validHeaders a set of headers that are allowed.
     * @return {@code true} if the current request headers are supported.
     */
    public boolean areRequestHeadersValid(final List<String> validHeaders) {
        final String requestHeaders = request.getHeader(RequestHeader.HEADERS.headerName);
        if (requestHeaders == null) {
            return true;
        }

        final Collection<String> lowerCaseValidHeaders = StringUtils.toLowerCase(validHeaders);
        final Iterable<String> headers = Splitter.on(',').trimResults().split(requestHeaders);
        boolean valid = Iterables.all(headers, new Predicate<String>() {
            @Override
            public boolean apply(final String header) {
                return lowerCaseValidHeaders.contains(header.toLowerCase());
            }
        });
        return valid;
    }

    /**
     * Checks that the configured preflight request headers ({@link RequestHeader#HEADERS}) are supported according to the
     * underlying {@link CorsConfiguration} settings.
     * 
     * @return {@code true} if the current request headers are not supported.
     */
    public boolean areRequestHeadersValid() {
        return areRequestHeadersValid(corsConfig.getValidRequestHeaders());
    }

    /**
     * Set the {@link ResponseHeader#ALLOW_HEADERS} to configured values.
     * 
     * @return {@code Cors} to support methods chaining.
     */
    public Cors setAllowHeaders(final HttpServletResponse response) {
        response.setHeader(ResponseHeader.ALLOW_HEADERS.toString(), asString(corsConfig.getValidRequestHeaders()));
        return this;
    }

    private String asString(final Collection<String> strings) {
        return Joiner.on(',').join(strings);
    }

    private boolean hasHeader(final String name) {
        return request.getHeader(name) != null;
    }

}
