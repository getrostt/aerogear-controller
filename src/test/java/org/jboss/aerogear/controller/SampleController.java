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

package org.jboss.aerogear.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.jboss.aerogear.controller.router.error.ErrorResponse;
import org.jboss.aerogear.controller.router.error.JsonErrorResponse;
import org.jboss.aerogear.controller.router.rest.pagination.Paginated;
import org.jboss.aerogear.controller.router.rest.pagination.PaginationInfo;

public class SampleController {

    public void index() {
    }

    public void client(String name) {
    }

    public void client(Long name) {
    }

    public void lol() {
    }

    public void save(Car car) {
    }
    
    public Car save(String color, String brand) {
        return new Car(color, brand);
    }

    public void save(Car car, String metadata) {
    }

    public void find(String id) {
    }

    public void find(String color, String brand) {
    }

    public void find(Long id) {
    }

    @Paginated
    public List<Integer> findBy(PaginationInfo pinfo, String query) {
        return ints(pinfo.getOffset(), pinfo.getLimit(), 50);
    }

    @Paginated
    public List<Integer> findByWithDefaults(PaginationInfo pinfo, String query) {
        return ints(pinfo.getOffset(), pinfo.getLimit(), 50);
    }

    @Paginated(webLinking = false)
    public List<Integer> findByWithCustomHeadersDefaultPrefix(PaginationInfo pinfo, String query) {
        return ints(pinfo.getOffset(), pinfo.getLimit(), 50);
    }

    @Paginated(webLinking = false, customHeadersPrefix = "Test-")
    public List<Integer> findByWithCustomHeadersPrefix(PaginationInfo pinfo, String query) {
        return ints(pinfo.getOffset(), pinfo.getLimit(), 50);
    }

    @Paginated(offsetParamName = "myoffset", limitParamName = "mylimit", customHeadersPrefix = "TS-", webLinking = false)
    public List<Integer> findByWithCustomParamNames(PaginationInfo pinfo, String query) {
        return ints(pinfo.getOffset(), pinfo.getLimit(), 50);
    }

    private List<Integer> ints(final int offset, final int limit, final int total) {
        final ArrayList<Integer> ints = new ArrayList<Integer>();
        if (offset >= total) {
            return ints;
        }
        int maxResults = offset + limit;
        for (int i = offset; i < maxResults; i++) {
            ints.add(i);
        }
        return ints;
    }

    public void admin() {
    }

    public void error(final Exception e) {
    }

    public void throwSampleControllerException() throws SampleControllerException {
        throw new SampleControllerException("Bogus exception");
    }

    public void throwIllegalStateException() {
        throw new IllegalStateException("Bogus exception");
    }

    public void errorPage() {
    }

    public ErrorResponse errorResponse() {
        return new JsonErrorResponse(HttpServletResponse.SC_NOT_FOUND).json(Collections.emptyList());
    }

    public void superException() {
    }

    public void subException() {
    }
}
