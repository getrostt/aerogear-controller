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

package org.jboss.aerogear.controller.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.controller.router.RequestMethod;

import com.google.common.base.Splitter;

/**
 * Utility methods for various {@link HttpServletRequest} operation.
 */
public class RequestUtils {
    
    private final static Pattern PATH_PATTERN = Pattern.compile("/([^/]+)");
    private final static Pattern PATH_PLACEHOLDER_PATTERN = Pattern.compile("/\\{?([^/}?]+)\\}?");
    private final static Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z]*)\\}");

    private RequestUtils() {
    }

    /**
     * Returns the path of the current request with out the context path. 
     * </p> 
     * For example, if the web application was deployed with a context path of '/myapp', and the request submitted was 
     * '/myapp/cars/12', this method would return '/cars/12'.
     * 
     * @param request the {@link HttpServletRequest}.
     * @return {@code String} the request path without the context path (suffix)
     */
    public static String extractPath(final HttpServletRequest request) {
        final String contextPath = request.getServletContext().getContextPath();
        return request.getRequestURI().substring(contextPath.length());
    }

    /**
     * Returns the {@link RequestMethod} for the passed-in {@link HttpServletRequest}.
     * 
     * @param httpServletRequest the {@link HttpServletRequest}
     * @return {@link RequestMethod} matching the Http Method of the request.
     */
    public static RequestMethod extractMethod(final HttpServletRequest httpServletRequest) {
        return RequestMethod.valueOf(httpServletRequest.getMethod());
    }

    /**
     * Returns the {@code Accept header} from the passed-in {@code HttpServletRequest}.
     * 
     * @param request the {@link HttpServletRequest}
     * @return {@code Set<String>} of the values of the Http Accept Header, or an empty list if there was not Accept header
     */
    public static Set<String> extractAcceptHeader(final HttpServletRequest request) {
        final String acceptHeader = request.getHeader("Accept");
        if (acceptHeader == null) {
            return Collections.emptySet();
        }

        final Set<String> acceptHeaders = new LinkedHashSet<String>();
        for (String header : Splitter.on(',').trimResults().split(acceptHeader)) {
            acceptHeaders.add(header);
        }
        return acceptHeaders;
    }
    
    /**
     * Extracts the path elements and returns a map indexed by the order in which
     * the path elements appear.
     * 
     * @param path the path from which the path elements will be extracted/collected.
     * @return {@code Map<Integer, String} indexed by the position of the path elements.
     */
    public static Map<Integer, String> extractPathSegments(final String path) {
        final Matcher requestMatcher = PATH_PLACEHOLDER_PATTERN.matcher(path);
        final Map<Integer, String> map = new HashMap<Integer, String>();
        for (int i = 0; requestMatcher.find(); i++) {
            map.put(i, requestMatcher.group(1));
        }
        return map;
    }
    
    /**
     * Extracts path parameter placeholders from the passed in path.
     * 
     * @param path the path from which the path elements will be extracted/collected.
     * @return {@code Map<String, Integer} indexed name of the placeholder/variable name, and the value is the position 
     *                    of this name in the path.
     */
    public static Map<String, Integer> extractPathVariableNames(final String path) {
        final Matcher requestMatcher = PATH_PATTERN.matcher(path);
        final Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; requestMatcher.find(); i++) {
            final String segment = requestMatcher.group(1);
            if (segment.startsWith("{")) {
                map.put(trimPlaceHolder(segment), i);
            }
        }
        return map;
    }
    
    private static String trimPlaceHolder(final String str) {
        return str.substring(1, str.length()-1);
    }
    
    /**
     * Extracts path parameters from the passed-in request path.
     * 
     * @param requestPath the actual request path.
     * @param configPath the configuration time path. This path can contain path variable placeholders which will
     *                   be mapped to the corresponding request path values.
     * @return {@code Map<String, String>} index by the name of the parameter, and the value is the value of the corresponding
     *                   request path parameter.
     */
    public static Map<String, String> mapPathParams(final String requestPath, final String configPath) {
        final Map<Integer, String> pathElementsMap = extractPathSegments(requestPath);
        final Map<String, Integer> pathVariableNameMap = extractPathVariableNames(configPath);
        
        final Map<String, String> params = new HashMap<String, String>();
        final Matcher configMatcher = PLACEHOLDER_PATTERN.matcher(configPath);
        while (configMatcher.find()) {
            final String paramName = configMatcher.group(1);
            final String value = pathElementsMap.get(pathVariableNameMap.get(paramName));
            params.put(paramName, value);
        }
        return params;
    }

    /**
     * Will extract any placeholders, {name}, from the passed-in string.
     * 
     * @param str the string from with placeholder names should be extracted.
     * @return {@code Set} containing the placeholder names.
     */
    public static Set<String> extractPlaceHolders(final String str) {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
        final Set<String> params = new HashSet<String>();
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params;
    }
    
    /**
     * Injects/replaces the placeholders in the passed-in string with the corresponding values from the passed-in map.
     * 
     * @param str the string containing placeholders that are to be replaced by values in the map
     * @param map the map containing the mapping of param name to param value.
     * @return {@code String} the string with the placesholders replaced by the values.
     */
    public static String injectParamValues(final String str, final Map<String, Object> map) {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, String.valueOf(map.get(matcher.group(1))));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
