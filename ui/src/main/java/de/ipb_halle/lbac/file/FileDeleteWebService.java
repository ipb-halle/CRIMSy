/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.file;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.CollectionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@WebServlet(name = "FileDeleteWebService", urlPatterns = {"/deletedocs/*"}, asyncSupported = true)
public class FileDeleteWebService extends HttpServlet {

    private final static long serialVersionUID = 1L;
    private final Logger logger = LogManager.getLogger(FileDeleteWebService.class);

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileEntityService fileEntityService;

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final HttpSession session = req.getSession();
            final AsyncContext asyncContext = req.startAsync();
            resp.setContentType("text/html");

            //*** check session object for security ***
            User user = (User) session.getAttribute("currentUser");
            if (user == null) {
                logger.warn("get user session object failed.");
                resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            /**
             * TODO: check DELETE access permission
             */
            try {
                Map<String, Object> cmap = new HashMap<>();

                //*** getting fileEntity ***
                logger.info("file uuid: " + req.getPathInfo().substring(1));
                cmap.put("id", req.getPathInfo().substring(1));
                List<FileObject> fe = this.fileEntityService.load(cmap);

                if ((fe != null) && (fe.size() > 0)) {
                    asyncContext.start(new FileDeleteExec(fe.get(0), fileEntityService, asyncContext));

                } else {
                    this.logger.warn("doDelete(): could not obtain fileEntity");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                this.logger.warn("doDelete() caught an exception: ", e);
            }
        } catch (IllegalStateException e) {
            logger.error("doDelete() caught an exception:", (Throwable) e);
        }
    }
}
