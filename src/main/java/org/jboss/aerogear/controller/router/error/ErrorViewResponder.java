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
package org.jboss.aerogear.controller.router.error;

import org.jboss.aerogear.controller.router.MediaType;
import org.jboss.aerogear.controller.view.AbstractViewResponder;
import org.jboss.aerogear.controller.view.HtmlViewResolver;
import org.jboss.aerogear.controller.view.ViewResolver;

public class ErrorViewResponder extends AbstractViewResponder {

    public static final MediaType MEDIA_TYPE = new MediaType("text/html", ErrorViewResponder.class);
    private final ErrorViewResolver errorViewResolver = new ErrorViewResolver(new HtmlViewResolver());

    @Override
    public MediaType getMediaType() {
        return MEDIA_TYPE;
    }

    @Override
    public ViewResolver getViewResolver()  {
        return errorViewResolver;
    }

}
