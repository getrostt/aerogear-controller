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
import static org.jboss.aerogear.controller.router.MediaType.HTML;
import static org.jboss.aerogear.controller.router.MediaType.JSON;
import static org.jboss.aerogear.controller.router.MediaType.JSP;
import static org.jboss.aerogear.controller.router.MediaType.ANY;
import static org.jboss.aerogear.controller.router.RequestMethod.GET;
import static org.jboss.aerogear.controller.router.RequestMethod.POST;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.Car;
import org.jboss.aerogear.controller.SampleController;
import org.jboss.aerogear.controller.mocks.RouteTester;
import org.jboss.aerogear.controller.router.parameter.MissingRequestParameterException;
import org.jboss.aerogear.controller.router.rest.AbstractRestResponder;
import org.jboss.aerogear.controller.router.rest.JsonResponder;
import org.jboss.aerogear.controller.router.rest.pagination.PaginationInfo;
import org.junit.Test;

public class DefaultRouteProcessorTest {

    @Test 
    public void testRouteForbidden() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/car/{id}").roles("admin")
                        .on(GET)
                        .to(SampleController.class).find(param("id"));
            }
        }).requestMethod(GET).acceptHeader(HTML);
        final Route route = routeTester.routeFor("/car/3");
        doThrow(new ServletException("RouteForbiddenTest")).when(routeTester.getSecurityProvider()).isRouteAllowed(route);
        final InvocationResult processResult = routeTester.process(route);
        assertThat(processResult.getResult()).isInstanceOf(ServletException.class);
    }

    @Test
    public void testMvcRouteWithPathParam() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/car/{id}").roles("admin")
                        .on(GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("id"));
            }
        });
        routeTester.acceptHeader(HTML).processGetRequest("/car/3");
        verify(routeTester.<SampleController>getController()).find("3");
        verify(routeTester.jspResponder()).respond(any(), any(RouteContext.class));
    }

    @Test
    public void testRestRouteWithPathParam() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/car/{id}").roles("admin")
                        .on(GET)
                        .produces(JSP, JSON)
                        .to(SampleController.class).find(param("id"));
            }
        });
        routeTester.acceptHeader(JSON).processGetRequest("/car/3");
        verify(routeTester.<SampleController>getController()).find("3");
        verify(routeTester.jsonResponder()).respond(any(), any(RouteContext.class));
    }

    @Test
    public void testRestRouteWithTypedPathParam() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/car/{id}").roles("admin")
                        .on(GET)
                        .produces(JSP, JSON)
                        .to(SampleController.class).find(param("id",Long.class));
            }
        });
        routeTester.acceptHeader(JSON).processGetRequest("/car/3");
        verify(routeTester.<SampleController>getController()).find(new Long(3));
        verify(routeTester.jsonResponder()).respond(any(), any(RouteContext.class));
    }

    @Test
    public void testFormParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.POST)
                        .produces(JSP)
                        .to(SampleController.class).save(param("color"), param("brand"));
            }
        }).requestMethod(POST).acceptHeader(HTML).param("color", "red").param("brand", "Ferrari");
        routeTester.spyController(new SampleController());
        final InvocationResult r = routeTester.process("/cars");
        final Car car = (Car) r.getResult();
        assertThat(car.getColor()).isEqualTo("red");
        assertThat(car.getBrand()).isEqualTo("Ferrari");
        verify(routeTester.jspResponder()).respond(any(), any(RouteContext.class));
    }

    @Test
    public void testFormParametersWithOneDefaultValue() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.POST)
                        .produces(JSP)
                        .to(SampleController.class).save(param("color", "gray"), param("brand", "Lada"));
            }
        }).acceptHeader(HTML).param("color", "gray");
        routeTester.processPostRequest("/cars");
        verify(routeTester.<SampleController>getController()).save("gray", "Lada");
        verify(routeTester.jspResponder()).respond(any(), any(RouteContext.class));
    }

    @Test
    public void testEntityFormParameter() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(POST)
                        .produces(JSP)
                        .to(SampleController.class).save(param(Car.class));
            }
        }).acceptHeader(HTML).param("car.color", "Blue").param("car.brand", "BMW");
        routeTester.processPostRequest("/cars");
        verify(routeTester.<SampleController>getController()).save(any(Car.class));
    }

    @Test
    public void testQueryParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand", "Ferrari"));
            }
        }).acceptHeader(HTML);
        routeTester.processGetRequest("/cars?color=red");
        verify(routeTester.<SampleController>getController()).find("red", "Ferrari");
    }

    @Test
    public void testPathAndQueryParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars/{color}")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand"));
            }
        }).acceptHeader(HTML).param("brand", "BMW");
        routeTester.processGetRequest("/cars/blue?brand=BMW");
        verify(routeTester.<SampleController>getController()).find("blue", "BMW");
    }

    @Test
    public void testHeaderParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand", "Ferrari"));
            }
        }).acceptHeader(HTML).header("color", "red");
        routeTester.processGetRequest("/cars");
        verify(routeTester.<SampleController>getController()).find("red", "Ferrari");
    }

    @Test
    public void testHeaderAndPathParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars/{color}")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand"));
            }
        }).acceptHeader(HTML).header("brand", "Ferrari");
        routeTester.processGetRequest("/cars/red");
        verify(routeTester.<SampleController>getController()).find("red", "Ferrari");
    }

    @Test
    public void testCookieParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand", "Ferrari"));
            }
        }).acceptHeader(HTML).cookie("brand", "Ferrari").cookie("color", "red");
        routeTester.processGetRequest("/cars");
        verify(routeTester.<SampleController>getController()).find("red", "Ferrari");
    }

    @Test
    public void testCookieAndPathParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars/{color}")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand"));
            }
        }).acceptHeader(HTML).cookie("brand", "Ferrari");
        routeTester.processGetRequest("/cars/red");
        verify(routeTester.<SampleController>getController()).find("red", "Ferrari");
    }

    @Test
    public void testQueryAndCookieParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand"));
            }
        }).acceptHeader(HTML).cookie("brand", "Ferrari");
        routeTester.processGetRequest("/cars?color=red");
        verify(routeTester.<SampleController>getController()).find("red", "Ferrari");
    }

    @Test
    public void testQueryAndHeaderParameters() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand"));
            }
        }).acceptHeader(HTML).header("brand", "Ferrari");
        routeTester.processGetRequest("/cars?color=red");
        verify(routeTester.<SampleController>getController()).find("red", "Ferrari");
    }
    
    @Test (expected = RuntimeException.class) 
    public void testNoRespondersForMediaType() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/car/{id}")
                        .on(GET)
                        .produces(new MediaType("custom/type", CustomResponder.class))
                        .to(SampleController.class).find(param("id"));
            }
        }).acceptHeader("custom/type");
        routeTester.processGetRequest("/car/3");
    }

    @Test
    public void testDefaultResponder() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.POST)
                        .produces(JSP)
                        .to(SampleController.class).save(param("color"), param("brand"));
            }
        }).acceptHeader(JSP).header("brand", "Lada");
        routeTester.processPostRequest("/cars?color=gray");
        verify(routeTester.<SampleController>getController()).save("gray", "Lada");
        verify(routeTester.jspResponder()).respond(anyObject(), any(RouteContext.class));
    }

    @Test
    public void testAnyResponder() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars/{id}").roles("admin")
                        .on(RequestMethod.GET)
                        .produces(JSP, JSON)
                        .to(SampleController.class).find(param("id"));
            }
        }).requestMethod(GET).acceptHeader(ANY);
        routeTester.process("/cars/10");
        verify(routeTester.<SampleController>getController()).find("10");
        verify(routeTester.jspResponder()).respond(anyObject(), any(RouteContext.class));
    }

    @Test
    public void testAnyResponderEmptyAcceptHeader() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars/{id}")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("id"));
            }
        }).requestMethod(GET);
        routeTester.process("/cars/10");
        verify(routeTester.<SampleController>getController()).find("10");
        verify(routeTester.jspResponder()).respond(anyObject(), any(RouteContext.class));
    }

    @Test
    public void testConsumes() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars").roles("admin")
                        .on(POST)
                        .consumes(JSON)
                        .produces(MediaType.JSON)
                        .to(SampleController.class).save(param(Car.class));
            }
        }).requestMethod(POST).acceptHeader(JSON).body("{\"color\":\"red\", \"brand\":\"mini\"}");
        routeTester.process("/cars");
        verify(routeTester.<SampleController>getController()).save(any(Car.class));
    }

    @Test
    public void testOrderMultipleAcceptHeaders() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars/{id}").roles("admin")
                        .on(GET)
                        .produces(JSP, JSON)
                        .to(SampleController.class).find(param("id"));
            }
        }).requestMethod(GET).acceptHeader(JSON).acceptHeader(HTML);
        routeTester.process("/cars/10");
        verify(routeTester.<SampleController>getController()).find("10");
        verify(routeTester.jsonResponder()).respond(anyObject(), any(RouteContext.class));
    }

    @Test(expected = RuntimeException.class)
    public void testNoConsumers() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars").roles("admin")
                        .on(POST)
                        .consumes(JSON)
                        .produces(JSON)
                        .to(SampleController.class).save(param(Car.class));
            }
        }).requestMethod(POST).acceptHeader("application/bogus").body("{\"color\":\"red\", \"brand\":\"mini\"}");
        routeTester.process("/cars");
    }

    @Test
    public void testPagedEndpointWithWebLinking() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/ints")
                        .on(GET)
                        .produces(JSON)
                        .to(SampleController.class).findByWithDefaults(param(PaginationInfo.class), param("color"));
            }
        }).spyController(new SampleController()).addResponder(new JsonResponder());
        final InvocationResult result = routeTester.acceptHeader(JSON).processGetRequest("/ints?color=blue");
        verify(result.getRouteContext().getResponse()).setHeader(eq("Link"), anyString());
        verify(routeTester.<SampleController>getController()).findByWithDefaults(any(PaginationInfo.class), eq("blue"));
        assertThat(routeTester.getStringWriter().toString()).isEqualTo("[0,1,2,3,4,5,6,7,8,9]");
    }

    @Test
    public void testPagedEndpointWithCustomHeadersDefaultPrefix() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/ints")
                        .on(RequestMethod.GET)
                        .produces(JSON)
                        .to(SampleController.class).findByWithCustomHeadersDefaultPrefix(param(PaginationInfo.class), param("color"));
            }
        }).spyController(new SampleController()).addResponder(new JsonResponder());
        final InvocationResult result = routeTester.acceptHeader(JSON).processGetRequest("/ints?color=blue");
        final HttpServletResponse response = result.getRouteContext().getResponse();
        verify(routeTester.<SampleController>getController()).findByWithCustomHeadersDefaultPrefix(any(PaginationInfo.class), eq("blue"));
        verify(response).setHeader("AG-Links-Next", "http://localhost:8080/test/ints?color=blue&offset=10&limit=10");
        verify(response, never()).setHeader(eq("AG-Links-Previous"), anyString());
        assertThat(routeTester.getStringWriter().toString()).isEqualTo("[0,1,2,3,4,5,6,7,8,9]");
    }

    @Test
    public void testPagedEndpointCustomHeadersPrefix() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/ints")
                        .on(RequestMethod.GET)
                        .produces(JSON)
                        .to(SampleController.class).findByWithCustomHeadersPrefix(param(PaginationInfo.class), param("color"));
            }
        }).addResponder(new JsonResponder()).spyController(new SampleController())
        .param("color", "blue").param("offset", "0").param("limit", "5");
        final InvocationResult result = routeTester.acceptHeader(JSON).processGetRequest("/ints");
        final HttpServletResponse response = result.getRouteContext().getResponse();
        verify(routeTester.<SampleController>getController()).findByWithCustomHeadersPrefix(any(PaginationInfo.class), eq("blue"));
        verify(response).setHeader("Test-Links-Next", "http://localhost:8080/test/ints?color=blue&offset=5&limit=5");
        verify(response, never()).setHeader(eq("Test-Links-Previous"), anyString());
        assertThat(routeTester.getStringWriter().toString()).isEqualTo("[0,1,2,3,4]");
    }

    @Test
    public void testPagedEndpointMiddlePage() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/ints")
                        .on(RequestMethod.GET)
                        .produces(JSON)
                        .to(SampleController.class).findByWithCustomHeadersPrefix(param(PaginationInfo.class), param("color"));
            }
        }).addResponder(new JsonResponder()).spyController(new SampleController())
        .param("color", "blue").param("offset", "5").param("limit", "5");
        final InvocationResult result = routeTester.acceptHeader(JSON).processGetRequest("/ints");
        final HttpServletResponse response = result.getRouteContext().getResponse();
        verify(routeTester.<SampleController>getController()).findByWithCustomHeadersPrefix(any(PaginationInfo.class), eq("blue"));
        verify(response).setHeader("Test-Links-Previous", "http://localhost:8080/test/ints?color=blue&offset=0&limit=5");
        verify(response).setHeader("Test-Links-Next", "http://localhost:8080/test/ints?color=blue&offset=10&limit=5");
        assertThat(routeTester.getStringWriter().toString()).isEqualTo("[5,6,7,8,9]");
    }

    @Test
    public void testPagedEndpointBeyondLastPage() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/ints")
                        .on(RequestMethod.GET)
                        .produces(JSON)
                        .to(SampleController.class).findByWithCustomParamNames(param(PaginationInfo.class), param("brand"));
            }
        }).acceptHeader(JSON).addResponder(new JsonResponder()).spyController(new SampleController());
        final InvocationResult result = routeTester.processGetRequest("/ints?brand=BMW&myoffset=50&mylimit=5");
        final HttpServletResponse response = result.getRouteContext().getResponse();
        verify(routeTester.<SampleController>getController()).findByWithCustomParamNames(any(PaginationInfo.class), eq("BMW"));
        verify(response).setHeader("TS-Links-Previous", "http://localhost:8080/test/ints?brand=BMW&myoffset=45&mylimit=5");
        verify(response, never()).setHeader(eq("TS-Links-Next"), anyString());
        assertThat(routeTester.getStringWriter().toString()).isEqualTo("[]");
    }
    
    @Test 
    public void testMissingQueryParameter() throws Exception {
        final RouteTester routeTester = RouteTester.from(new AbstractRoutingModule() {
            @Override
            public void configuration() {
                route()
                        .from("/cars")
                        .on(RequestMethod.GET)
                        .produces(JSP)
                        .to(SampleController.class).find(param("color"), param("brand", "Ferrari"));
            }
        }).spyController(new SampleController());
        final InvocationResult result = routeTester.acceptHeader(JSP).processGetRequest("/cars");
        assertThat(result.getResult()).isInstanceOf(MissingRequestParameterException.class);
    }    
    
    private class CustomResponder extends AbstractRestResponder {
        
        private MediaType customMediaType = new MediaType("application/custom", CustomResponder.class); 
        
        @Override
        public void writeResponse(Object entity, RouteContext routeContext) throws Exception {
            // NoOp
        }

        public MediaType getMediaType() {
            return customMediaType;
        }

    }
    
}
