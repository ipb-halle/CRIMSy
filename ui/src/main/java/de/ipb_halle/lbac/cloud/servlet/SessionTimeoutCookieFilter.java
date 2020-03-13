/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.cloud.servlet;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(
        urlPatterns = {"/*"},
        filterName = "SessionTimeoutCookieFilter",
        asyncSupported = true)

/**
 * set cookies with session expire time and server time to trigger client javascript
 */

public class SessionTimeoutCookieFilter implements Filter {

    private Logger logger;

    @Override
    public void init(FilterConfig filterConfig) {
        logger = LogManager.getLogger(SessionTimeoutCookieFilter.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest  httpServletRequest  = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        final HttpSession         session             = httpServletRequest.getSession();

        long   currTime   = System.currentTimeMillis();
        long   expiryTime = currTime + session.getMaxInactiveInterval() * 1000;
        Cookie cookie     = new Cookie("serverTime", "" + currTime);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);

        cookie = new Cookie("sessionExpiry", "" + expiryTime);

        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
