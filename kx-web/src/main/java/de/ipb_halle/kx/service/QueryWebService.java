/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@WebServlet(name = "QueryWebService", urlPatterns = {"/query/*"}, asyncSupported = true)
public class QueryWebService extends HttpServlet {

    private final static long serialVersionUID = 1L;

    private final Logger logger = LogManager.getLogger(QueryWebService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("doPost(): request received.");
        try {
            final PrintWriter out = resp.getWriter();
            out.write(processRequest(req));
        } catch (IOException e) {
            logger.error((Throwable) e);
        }
    }

    private String processRequest(HttpServletRequest req) {
        try {

        } catch (Exception e) {
            logger.warn((Throwable) e);
        }
        return "Error";
    }
}
