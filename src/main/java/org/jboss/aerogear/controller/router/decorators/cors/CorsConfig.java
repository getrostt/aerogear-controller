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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.aerogear.controller.router.RequestMethod;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * Concrete implementation of {@link CorsConfiguration} which provides configuration of CORS in AeroGear Controller.
 * 
 * @see CorsHandler
 */
public class CorsConfig implements CorsConfiguration {

    private final boolean corsSupportEnabled;
    private final boolean anyOrigin;
    private final boolean allowCookies;
    private final long maxAge;
    private final Set<String> validRequestMethods;
    private final List<String> validRequestHeaders;
    private final List<String> exposeHeaders;

    private CorsConfig(final Builder builder) {
        this.corsSupportEnabled = builder.corsSupportEnabled;
        this.anyOrigin = builder.anyOrigin;
        this.allowCookies = builder.allowCookies;
        this.maxAge = builder.maxAge;
        this.validRequestMethods = Collections.unmodifiableSet(builder.validRequestMethods);
        this.validRequestHeaders = Collections.unmodifiableList(builder.validRequestHeaders);
        this.exposeHeaders = Collections.unmodifiableList(builder.exposeHeaders);
    }

    /**
     * Returns a {@link Origin} instance which can be configured as needed
     * 
     * @return {@link Origin} to be used to configure and build a {@link CorsConfiguration} instance.
     */
    public static Origin enableCorsSupport() {
        return new Builder().enableCorsSupport();
    }

    public static CorsConfiguration disableCorsSupport() {
        return new Builder().disableCorsSupport();
    }

    /**
     * Returns a {@link CorsConfiguration} instance with the default values for all properties.
     * 
     * @return {@link CorsConfiguration} with default properties set.
     */
    public static CorsConfiguration defaultConfig() {
        return CorsConfig.enableCorsSupport().build();
    }

    @Override
    public boolean isCorsSupportEnabled() {
        return corsSupportEnabled;
    }

    @Override
    public boolean exposeHeaders() {
        return !exposeHeaders.isEmpty();
    }

    @Override
    public List<String> getExposeHeaders() {
        return Collections.unmodifiableList(exposeHeaders);
    }

    @Override
    public boolean anyOrigin() {
        return anyOrigin;
    }

    @Override
    public boolean allowCookies() {
        return allowCookies;
    }

    @Override
    public boolean hasMaxAge() {
        return maxAge > -1;
    }

    @Override
    public long getMaxAge() {
        return maxAge;
    }

    @Override
    public Set<String> getValidRequestMethods() {
        return validRequestMethods;
    }

    @Override
    public List<String> getValidRequestHeaders() {
        return Collections.unmodifiableList(validRequestHeaders);
    }

    @Override
    public String toString() {
        return new StringBuilder("CorsConfiguration[")
                .append("corsSupportEnabled=").append(corsSupportEnabled)
                .append(", exposeHeaders=").append(exposeHeaders)
                .append(", anyOrigin=").append(anyOrigin)
                .append(", allowCookies=").append(allowCookies)
                .append(", maxAge=").append(maxAge)
                .append(", validRequestHeaders=").append(validRequestHeaders)
                .append(", validRequestMethods=").append(validRequestMethods)
                .append("]").toString();
    }

    public interface SupportedOptions {
        Origin enableCorsSupport();

        CorsConfiguration disableCorsSupport();

        CorsConfiguration build();
    }

    public interface Origin {
        Cookies anyOrigin();

        Cookies echoOrigin();

        CorsConfiguration build();
    }

    public interface Cookies {
        ExposeHeaders enableCookies();

        ExposeHeaders disableCookies();

        CorsConfiguration build();
    }

    public interface ExposeHeaders extends MaxAge {
        MaxAge exposeHeaders(String... headers);

        CorsConfiguration build();
    }

    public interface MaxAge {
        ValidRequestMethods maxAge(long age);

        CorsConfiguration build();
    }

    public interface ValidRequestMethods {
        ValidRequestHeaders validRequestMethods(RequestMethod... requestMethods);

        ValidRequestHeaders enableAllRequestMethods();

        CorsConfiguration build();
    }

    public interface ValidRequestHeaders {
        CorsConfiguration validRequestHeaders(final String... validHeaders);

        CorsConfiguration build();
    }

    private static class Builder implements SupportedOptions, Origin, ExposeHeaders, Cookies, MaxAge, ValidRequestHeaders,
            ValidRequestMethods {
        private boolean corsSupportEnabled = true;
        private boolean anyOrigin;
        private boolean allowCookies;
        private long maxAge;
        private List<String> exposeHeaders = new ArrayList<String>();
        private Set<String> validRequestMethods = new HashSet<String>();
        private List<String> validRequestHeaders = new ArrayList<String>();

        public Builder() {
        }

        public Origin enableCorsSupport() {
            corsSupportEnabled = true;
            return this;
        }

        public CorsConfiguration disableCorsSupport() {
            corsSupportEnabled = false;
            return build();
        }

        public Cookies anyOrigin() {
            this.anyOrigin = true;
            return this;
        }

        public Cookies echoOrigin() {
            this.anyOrigin = false;
            return this;
        }

        public ExposeHeaders enableCookies() {
            allowCookies = true;
            return this;
        }

        public ExposeHeaders disableCookies() {
            allowCookies = false;
            return this;
        }

        public MaxAge exposeHeaders(final String... headers) {
            exposeHeaders.addAll(Arrays.asList(headers));
            return this;
        }

        public ValidRequestMethods maxAge(final long age) {
            maxAge = age;
            return this;
        }

        public ValidRequestHeaders validRequestMethods(final RequestMethod... requestMethods) {
            validRequestMethods.addAll(asSet(requestMethods));
            return this;
        }

        public ValidRequestHeaders enableAllRequestMethods() {
            validRequestMethods(RequestMethod.values());
            return this;
        }

        public CorsConfiguration validRequestHeaders(final String... validHeaders) {
            validRequestHeaders.addAll(Arrays.asList(validHeaders));
            return build();
        }

        private Set<String> asSet(final RequestMethod... requestMethods) {
            final String join = Joiner.on(",").join(requestMethods);
            return asSet(join, false);
        }

        private Set<String> asSet(final String csvString, final boolean toLowerCase) {
            if (csvString == null) {
                return Collections.emptySet();
            }

            final Set<String> strings = new LinkedHashSet<String>();
            for (String string : Splitter.on(',').trimResults().split(csvString)) {
                strings.add(toLowerCase ? string.toLowerCase() : string);
            }
            return strings;
        }

        public CorsConfiguration build() {
            validRequestHeaders.add("Origin");
            if (validRequestMethods.isEmpty()) {
                enableAllRequestMethods();
            }
            return new CorsConfig(this);
        }

    }

}
