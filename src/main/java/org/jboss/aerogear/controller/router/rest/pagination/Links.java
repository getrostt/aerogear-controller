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
package org.jboss.aerogear.controller.router.rest.pagination;

import com.google.common.base.Optional;

/**
 * Links are a part of the metadata related to paging of fetched data. 
 * </p> 
 * Links are used to navigate the the first, previous, next, or last set of data.
 */
public class Links {

    private final String first;
    private final String previous;
    private final String next;
    private final Optional<String> last;

    public Links(final RequestPathParser requestPathParser, final PaginationProperties paging) {
        first = requestPathParser.replace(0, paging.limit());
        next = requestPathParser.replace(paging.nextOffset(), paging.limit());
        previous = requestPathParser.replace(paging.previousOffset(), paging.limit());
        last = paging.total().isPresent() ? Optional.of(requestPathParser.replace(paging.total().get() - paging.limit(),
                paging.limit())) : Optional.<String> absent();
    }

    public String getFirst() {
        return first;
    }

    public String getPrevious() {
        return previous;
    }

    public String getNext() {
        return next;
    }

    public Optional<String> getLast() {
        return last;
    }

    @Override
    public String toString() {
        return "Links[first=" + first + ", previous=" + previous + ",next=" + next + ", last=" + last + "]";
    }

}
