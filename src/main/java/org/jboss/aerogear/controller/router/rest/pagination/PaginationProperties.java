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

import org.jboss.aerogear.controller.log.ExceptionBundle;

import com.google.common.base.Optional;

/**
 * Properties related to the offset/limit pagination strategy.
 * <p/>
 */
public class PaginationProperties {

    private final int offset;
    private final int limit;
    private final Optional<Integer> total;

    public PaginationProperties(final int offset, final int limit) {
        this(offset, limit, -1);
    }

    public PaginationProperties(final int offset, final int limit, final int total) {
        checkValues(offset, limit);
        this.offset = offset;
        this.limit = limit;
        this.total = total == -1 ? Optional.<Integer> absent() : Optional.of(Integer.valueOf(total));
    }

    private void checkValues(final int offset, final int limit) {
        if (offset < 0 || limit <= 0) {
            throw ExceptionBundle.MESSAGES.invalidPagingRequest(offset, limit);
        }
    }

    public int offset() {
        return offset;
    }

    public int limit() {
        return limit;
    }

    public Optional<Integer> total() {
        return total;
    }

    public int nextOffset() {
        if (offset == 0) {
            return limit;
        }
        if (total.isPresent()) {
            final int total = this.total.get();
            if (offset + limit >= total) {
                return total;
            }
        }
        return offset + limit;
    }

    public int previousOffset() {
        if (isOffsetGreaterThanTotal()) {
            final int total = this.total.get();
            return total - limit;
        }
        final int difference = offset - limit;
        if (difference < 0) {
            return 0;
        }
        return difference;
    }

    public boolean isFirstOffset() {
        return offset < limit;
    }

    public boolean isLastOffset() {
        if (total.isPresent()) {
            final int total = this.total.get();
            return offset + limit >= total;
        }
        return false;
    }

    public boolean isOffsetGreaterThanTotal() {
        if (total.isPresent()) {
            final int total = this.total.get();
            return offset >= total;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Params[offset=" + offset + ", limit=" + limit + ", total=" + total + ", nextOffset=" + nextOffset()
                + ", previousOffset=" + previousOffset() + "]";
    }

}
