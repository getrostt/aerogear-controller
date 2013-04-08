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

import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.fest.assertions.Fail;
import org.jboss.aerogear.controller.SampleController;
import org.jboss.aerogear.controller.util.RequestUtils;
import org.junit.Test;

public class DefaultRouteTest {

    private static final RequestMethod[] GET = new RequestMethod[] { RequestMethod.GET };
    private static final Class<?> TARGET_CLASS = SampleController.class;
    private static final Method TARGET_METHOD = indexMethod(TARGET_CLASS, "index");

    @Test
    public void defaults() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.getPath()).isEqualTo("/car/{id}");
        assertThat(route.getMethods().size()).isEqualTo(1);
        assertThat(route.getMethods()).contains(RequestMethod.GET);
        assertThat(route.getTargetClass()).isEqualTo(SampleController.class);
        assertThat(route.getTargetMethod()).isEqualTo(TARGET_METHOD);
        assertThat(route.getRoles()).isEmpty();
        assertThat(route.isSecured()).isFalse();
        assertThat(route.hasExceptionsRoutes()).isFalse();
        assertThat(route.produces()).contains(MediaType.JSP);
    }

    @Test
    public void createRouteWithNullHttpMethods() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on((RequestMethod[]) null).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.getMethods()).isEmpty();
    }

    @Test
    public void constructWithNullRoles() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").roles((String[]) null).on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.getRoles()).isEmpty();
    }

    @Test
    public void constructWithNullExceptions() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").setThrowables((Set<Class<? extends Throwable>>) null).on(GET).to(SampleController.class)
                .index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.hasExceptionsRoutes()).isFalse();
    }

    @Test
    public void constructWithRoles() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").roles("admin").on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.getRoles().size()).isEqualTo(1);
        assertThat(route.getRoles()).contains("admin");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void constructWithExceptions() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").setThrowables(exceptions(IllegalArgumentException.class)).on(GET).to(SampleController.class)
                .index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.hasExceptionsRoutes()).isTrue();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void tryToModifyRoles() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").roles("admin").on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        route.getRoles().remove("admin");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void tryToModifyHttpMethods() throws Exception {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        route.getMethods().remove(RequestMethod.GET);
    }

    @Test
    public void matchesUnparameterizedRoutePaths() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/index").on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.matches(RequestMethod.GET, "/index", acceptHeaders(MediaType.HTML.getType()))).isTrue();
    }

    private Set<String> acceptHeaders(String... mediaTypes) {
        return new HashSet<String>(Arrays.asList(mediaTypes));
    }

    @Test
    public void matchParameterizedRoutePaths() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.matches(RequestMethod.GET, "/car/3", acceptHeaders(MediaType.HTML.getType()))).isTrue();
        assertThat(route.matches(RequestMethod.GET, "/car", acceptHeaders(MediaType.HTML.getType()))).isFalse();
        assertThat(route.matches(RequestMethod.GET, "/c", acceptHeaders(MediaType.HTML.getType()))).isFalse();
        assertThat(route.matches(RequestMethod.GET, "/carss", acceptHeaders(MediaType.HTML.getType()))).isFalse();
        assertThat(route.matches(RequestMethod.GET, "/somelongpath", acceptHeaders(MediaType.HTML.getType()))).isFalse();
    }

    @Test
    public void doesNotMatchesProduces() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).produces(MediaType.HTML).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        final Set<String> acceptHeaders = new HashSet<String>(Arrays.asList(MediaType.JSON.getType()));
        assertThat(route.matches(RequestMethod.GET, "/car/3", acceptHeaders)).isFalse();
    }

    @Test
    public void matchesProduces() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).produces(MediaType.HTML).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        final Set<String> acceptHeaders = new HashSet<String>(Arrays.asList(MediaType.HTML.getType()));
        assertThat(route.matches(RequestMethod.GET, "/car/3", acceptHeaders)).isTrue();
    }

    @Test
    public void matchesProducesAny() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).produces(MediaType.HTML).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        final Set<String> acceptHeaders = new HashSet<String>(Arrays.asList("*/*"));
        assertThat(route.matches(RequestMethod.GET, "/car/3", acceptHeaders)).isTrue();
    }
    
    @Test
    public void matchesProducesSubtype() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).produces(MediaType.HTML).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.matches(RequestMethod.GET, "/car/3", acceptHeaders("text/*"))).isTrue();
    }
    
    @Test
    public void toStringTest() {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{id}").on(GET).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        final String expected = "DefaultRoute[path=/car/{id}, "
                + "targetClass=class org.jboss.aerogear.controller.SampleController, "
                + "targetMethod=public void org.jboss.aerogear.controller.SampleController.index(), "
                + "produces=[MediaType[type=text/html, responderClass=class org.jboss.aerogear.controller.view.JspViewResponder]], "
                + "parameters=[], roles=[], throwables=[]]";
        assertThat(route.toString()).isEqualTo(expected);
    }

    @Test
    public void produces() {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/home").on(GET).produces(MediaType.JSON).to(SampleController.class).index();
        final Route route = new DefaultRoute(rd);
        assertThat(route.produces()).contains(MediaType.JSON);
    }
    
    @Test
    public void matchesProducesMultiplePathParams() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{color}/{brand}").on(GET).produces(MediaType.HTML).to(SampleController.class).find(null, null);
        final Route route = new DefaultRoute(rd);
        final Set<String> acceptHeaders = new HashSet<String>(Arrays.asList("*/*"));
        assertThat(route.matches(RequestMethod.GET, "/car/red/Ford", acceptHeaders)).isTrue();
    }
    
    @Test
    public void matchesProducesMultiplePathParamsNoMatch() throws NoSuchMethodException {
        final RouteDescriptor rd = new RouteDescriptor();
        rd.setPath("/car/{color}/{brand}/{id}").on(GET).produces(MediaType.HTML).to(SampleController.class).find(null, null);
        final Route route = new DefaultRoute(rd);
        final Set<String> acceptHeaders = new HashSet<String>(Arrays.asList("*/*"));
        assertThat(route.matches(RequestMethod.GET, "/car/red/Ford", acceptHeaders)).isFalse();
    }

    private static Method indexMethod(final Class<?> targetClass, final String methodName) {
        Method m = null;
        try {
            m = targetClass.getDeclaredMethod(methodName);
        } catch (final Exception e) {
            Fail.fail("Could not find a method named '" + methodName + "' on target class", e);
        }
        return m;
    }

    private Set<Class<? extends Throwable>> exceptions(final Class<? extends Throwable>... exs) {
        return new HashSet<Class<? extends Throwable>>(Arrays.asList(exs));
    }

}
