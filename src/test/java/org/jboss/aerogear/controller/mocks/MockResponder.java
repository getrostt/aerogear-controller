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
package org.jboss.aerogear.controller.mocks;

import static org.jboss.aerogear.controller.router.MediaType.HTML;
import static org.jboss.aerogear.controller.router.MediaType.JSP;
import static org.jboss.aerogear.controller.router.MediaType.JSON;
import static org.jboss.aerogear.controller.router.MediaType.ANY;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.jboss.aerogear.controller.router.Consumer;
import org.jboss.aerogear.controller.router.Responder;
import org.jboss.aerogear.controller.router.Responders;
import org.jboss.aerogear.controller.router.error.ErrorViewResponder;
import org.jboss.aerogear.controller.router.rest.JsonResponder;
import org.jboss.aerogear.controller.view.HtmlViewResponder;
import org.jboss.aerogear.controller.view.JspViewResponder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class MockResponder {

    @Mock
    private Instance<Responder> responderInstance;
    @Spy
    private JsonResponder jsonResponder;
    @Mock
    private Instance<Consumer> consumers;
    @Mock
    private JspViewResponder jspResponder;
    @Mock
    private HtmlViewResponder htmlResponder;
    @Spy
    private ErrorViewResponder errorViewResponder;
    private final List<Responder> responderList = new LinkedList<Responder>();

    public MockResponder() {
        MockitoAnnotations.initMocks(this);
    }

    public MockResponder addResponder(final Responder responder) {
        responderList.add(responder);
        return this;
    }

    public Responders getResponders() {
        if (responderList.isEmpty()) {
            mockResponders();
        }
        when(this.responderInstance.iterator()).thenReturn(responderList.iterator());
        return spy(new Responders(responderInstance));
    }

    public MockResponder addJspResponder() {
        when(jspResponder.accepts(HTML.getType())).thenReturn(true);
        when(jspResponder.getMediaType()).thenReturn(JSP);
        when(jspResponder.accepts(ANY)).thenReturn(true);
        addResponder(jspResponder);
        return this;
    }

    public MockResponder addHtmlResponder() {
        when(htmlResponder.accepts(HTML.getType())).thenReturn(true);
        when(htmlResponder.getMediaType()).thenReturn(HTML);
        addResponder(htmlResponder);
        return this;
    }

    public MockResponder addErrorResponder() {
        when(errorViewResponder.accepts(HTML.getType())).thenReturn(true);
        when(errorViewResponder.getMediaType()).thenReturn(ErrorViewResponder.MEDIA_TYPE);
        addResponder(errorViewResponder);
        return this;
    }

    public MockResponder addJsonResponder() {
        when(jsonResponder.accepts(JSON.getType())).thenReturn(true);
        when(jsonResponder.getMediaType()).thenReturn(JSON);
        addResponder(jsonResponder);
        return this;
    }

    public void mockResponders() {
        addJspResponder();
        addHtmlResponder();
        addJsonResponder();
        addErrorResponder();
    }

    public JsonResponder getJsonResponder() {
        return jsonResponder;
    }

    public JspViewResponder getJspResponder() {
        return jspResponder;
    }

    public HtmlViewResponder getHtmlResponder() {
        return htmlResponder;
    }

    public ErrorViewResponder getErrorViewResponder() {
        return errorViewResponder;
    }

}
