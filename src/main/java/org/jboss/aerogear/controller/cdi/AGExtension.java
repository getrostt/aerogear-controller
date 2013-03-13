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
package org.jboss.aerogear.controller.cdi;

import java.lang.reflect.Modifier;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.aerogear.controller.log.ExceptionBundle;
import org.jboss.aerogear.controller.router.Responder;

/**
 * AGExtension is a CDI extension that verifies that {@link Responder} implementations in AeroGear Controller
 * have a no-args constructor.
 * </p>
 * If a {@code Responder} implementation does not have a no-args constructor an error upon deployment will be raised
 * to fail early and make the user aware of this condition. If this check is not performed the {@code Responder} would simply 
 * not be picked up by CDI and trouble shooting the cause becomes difficult.  
 * </p>
 */
public class AGExtension implements Extension {

    <T> void processAnnotatedType(final @Observes ProcessAnnotatedType<T> pat) throws SecurityException {
        if (Responder.class.isAssignableFrom(pat.getAnnotatedType().getJavaClass())) {
            final Class<T> javaClass = pat.getAnnotatedType().getJavaClass();
            if (!Modifier.isAbstract(javaClass.getModifiers()) || !javaClass.isInterface()) {
                try {
                    javaClass.getConstructor(new Class[] {});
                } catch (final NoSuchMethodException e) {
                    throw ExceptionBundle.MESSAGES.responderDoesNotHaveNoArgsCtor(javaClass);
                }
            }
        }
    }

}
