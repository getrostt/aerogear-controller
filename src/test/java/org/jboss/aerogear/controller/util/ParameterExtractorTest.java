package org.jboss.aerogear.controller.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.router.Consumer;
import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.router.RequestMethod;
import org.jboss.aerogear.controller.router.Route;
import org.jboss.aerogear.controller.router.RouteBuilder;
import org.jboss.aerogear.controller.router.RouteContext;
import org.jboss.aerogear.controller.router.RouteDescriptorAccessor;
import org.jboss.aerogear.controller.router.Routes;
import org.jboss.aerogear.controller.router.parameter.Parameter;
import org.junit.Test;

public class ParameterExtractorTest {

	public static class ModelObject {
	}

	public static class Controller {
		public void doSomething(ModelObject model) {
		}
	}

	private RouteBuilder routeBuilder;

	/**
	 * tests the method
	 * {@link ParameterExtractor#extractArguments(RouteContext, Map)} with a
	 * request content-type that defines a charset.
	 * 
	 * @throws Exception
	 */
	@Test
	public void extractArgumentsWithCharsetDefined() throws Exception {
		// build parameters
		routeBuilder = Routes.route();
		//@formatter:off
		routeBuilder
				.from("/cars")
				.on(RequestMethod.PUT)
				.consumes(MediaType.JSON)
				.to(Controller.class)
				.doSomething(param(ModelObject.class));
		//@formatter:on
		Route route = routeBuilder.build();
		HttpServletRequest request = mock(HttpServletRequest.class);
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getContextPath()).thenReturn("/myapp");
		when(request.getServletContext()).thenReturn(servletContext);
		when(request.getRequestURI()).thenReturn("/myapp/cars");
		when(request.getContentType()).thenReturn(
				"application/json; charset=utf-8");
		HttpServletResponse response = null;
		Routes routes = Routes.from(asList(routeBuilder));
		RouteContext routeContext = new RouteContext(route, request, response,
				routes);
		Map<String, Consumer> consumers = new HashMap<String, Consumer>();

		// using a custom implementation to skip consumer logic
		consumers.put(MediaType.JSON.getType(), new Consumer() {

			@Override
			@SuppressWarnings("unchecked")
			public <T> T unmarshall(HttpServletRequest request, Class<T> type) {
				return (T) new ModelObject();
			}

			@Override
			public String mediaType() {
				return MediaType.JSON.getType();
			}
		});

		// call SUT
		Map<String, Object> arguments = ParameterExtractor.extractArguments(
				routeContext, consumers);

		// verify result
		assertNotNull("parameter entityParam missing",
				arguments.get("entityParam"));
	}

	public <T> T param(Class<T> type) {
		addParameter(Parameter.param(type));
		return null;
	}

	private void addParameter(final Parameter<?> parameter) {
		((RouteDescriptorAccessor) routeBuilder).getRouteDescriptor()
				.addParameter(parameter);
	}
}
