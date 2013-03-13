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

/**
 * RequestPathParser is able to replace, or add query parameters related to pagination.
 */
public class RequestPathParser {

    private final PaginationInfo pgInfo;
    private final String requestPath;

    public RequestPathParser(final PaginationInfo paginationInfo, final String requestPath) {
        pgInfo = paginationInfo;
        this.requestPath = requestPath;
    }

    /**
     * Replaces the offset and limit for the current request.
     * 
     * @param offset the new offset value.
     * @param limit the new limit value.
     * @return {@code String} the updated request url, containing the full url with query string.
     */
    public String replace(final int offset, final int limit) {
        final String path = replaceParam(pgInfo.getOffsetParamName(), String.valueOf(offset), requestPath);
        return replaceParam(pgInfo.getLimitParamName(), String.valueOf(limit), path);
    }

    private String replaceParam(final String paramName, final String paramValue, final String path) {
        final StringBuilder parsed = new StringBuilder();
        final int startIdx = path.indexOf(paramName);
        if (paramMissingFromQueryPath(startIdx)) {
            return path + "&" + paramName + "=" + paramValue;
        }
        parsed.append(path.substring(0, startIdx));
        parsed.append(paramName).append("=").append(paramValue);
        final String targetParam = path.substring(startIdx);
        final int tailIdx = targetParam.indexOf('&');
        if (tailIdx != -1) {
            parsed.append(targetParam.substring(tailIdx));
        }
        return parsed.toString();
    }

    private boolean paramMissingFromQueryPath(final int idx) {
        return idx == -1;
    }

}
