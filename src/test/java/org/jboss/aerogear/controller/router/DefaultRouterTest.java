package org.jboss.aerogear.controller.router;

import org.jboss.aerogear.controller.RequestMethod;
import org.jboss.aerogear.controller.SampleController;
import org.jboss.aerogear.controller.spi.SecurityProvider;
import org.jboss.aerogear.controller.view.ViewResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultRouterTest {

    @Mock
    private SecurityProvider securityProvider;

    @Mock
    private Route route;
    @Mock
    private BeanManager beanManager;
    @Mock
    private ViewResolver viewResolver;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private ControllerFactory controllerFactory;
    @Mock
    private ServletContext servletContext;
    @Mock
    private RequestDispatcher requestDispatcher;

    private DefaultRouter router;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final RoutingModule routingModule = new AbstractRoutingModule() {

            @Override
            public void configuration() {
                route()
                        .from("/car/{id}").roles("admin")
                        .on(RequestMethod.GET)
                        .to(SampleController.class).find(pathParam("id"));
            }
        };
        router = new DefaultRouter(routingModule, beanManager, viewResolver, controllerFactory, securityProvider);
    }

    @Test
    public void testIt() throws ServletException {
        final SampleController controller = spy(new SampleController());
        when(controllerFactory.createController(eq(SampleController.class), eq(beanManager))).thenReturn(controller);
        when(request.getMethod()).thenReturn(RequestMethod.GET.toString());
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getContextPath()).thenReturn("/abc");
        when(request.getRequestURI()).thenReturn("/abc/car/3");
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        router.dispatch(request, response, chain);
        verify(controller).find(eq("3"));
    }

    @Test
    public void testRouteAllowed() throws Exception {
        final SampleController controller = spy(new SampleController());
        when(route.isSecured()).thenReturn(true);
        when(securityProvider.isRouteAllowed(route)).thenReturn(true);

        when(controllerFactory.createController(eq(SampleController.class), eq(beanManager))).thenReturn(controller);
        when(request.getMethod()).thenReturn(RequestMethod.GET.toString());
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getContextPath()).thenReturn("/abc");
        when(request.getRequestURI()).thenReturn("/abc/car/3");
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        router.dispatch(request, response, chain);
        verify(controller).find(eq("3"));
    }

    @Test(expected = ServletException.class)
    public void testRouteForbbiden() throws Exception {
        final SampleController controller = spy(new SampleController());
        doThrow(new ServletException()).when(securityProvider).isRouteAllowed(route);

        when(route.isSecured()).thenReturn(true);
        verify(securityProvider.isRouteAllowed(route));

        when(controllerFactory.createController(eq(SampleController.class), eq(beanManager))).thenReturn(controller);
        when(request.getMethod()).thenReturn(RequestMethod.GET.toString());
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getContextPath()).thenReturn("/abc");
        when(request.getRequestURI()).thenReturn("/abc/car/3");
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        router.dispatch(request, response, chain);
        verify(controller).find(eq("3"));
    }
}
