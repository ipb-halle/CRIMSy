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

import de.ipb_halle.lbac.entity.ACObject;
import de.ipb_halle.lbac.entity.User;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

@WebFilter(urlPatterns = {"/uploaddocs/*"},
        filterName = "FileUploadSecurityFilter",
        asyncSupported = true
)
public class FileUploadSecurityFilter implements Filter {

    private Logger logger;

    @Override
    public void init(FilterConfig filterConfig) {
        logger = LogManager.getLogger(FileUploadSecurityFilter.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        chain.doFilter(request, response);

//        final String remoteAddr = request.getRemoteAddr();
//        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
//        final String remoteURI = httpServletRequest.getRequestURI();
//        final HttpSession session = httpServletRequest.getSession();
//
//        User user = (User) session.getAttribute("currentUser");
//        ACObject aco = (ACObject) session.getAttribute("uploadDestination");
//
//        if (httpServletRequest.getMethod().equalsIgnoreCase("POST")) {
//
//            final Part part = httpServletRequest.getPart("qqfile");
//
//            /*
//             * TODO: lookup of ACListService and checking of CREATE-access for destination
//             */
//            logger.warn("USER " + user);
//            logger.warn("PART " + part);
//
//            if (user != null && part != null) {
//                logger.info("file " + part.getSubmittedFileName() + " (size: " + part.getSize() + "): upload request by " + user.getName() + " (" + remoteAddr + ") on " + remoteURI);
//                chain.doFilter(request, response);
//            } else {
//                logger.warn("improper file upload.");
//                assert part != null;
//                logger.warn("file " + part.getSubmittedFileName() + " (size: " + part.getSize() + "): upload request by " + remoteAddr + " on " + remoteURI + "was rejected!");
//                final RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.xhtml");
//                requestDispatcher.forward(request, response);
//            }
    }

    @Override
    public void destroy() {

    }
}
