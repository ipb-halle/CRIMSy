/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.collections.CollectionService;
import java.io.IOException;
import java.io.PrintWriter;
import javax.inject.Inject;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@WebServlet(name = "FileUploadWebService", urlPatterns = {"/uploaddocs/*"}, asyncSupported = true)
@MultipartConfig(maxFileSize = 1024 * 1024 * 500, maxRequestSize = 1024 * 1024 * 1000)
public class FileUploadWebService extends HttpServlet {

    private final static long serialVersionUID = 1L;
    private final static long UPLOAD_TIMEOUT = 30L * 60L * 1000L;

    private final Logger logger = LogManager.getLogger(FileUploadWebService.class);

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileObjectService fileObjectService;

    @Inject
    private UserBean userBean;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        AsyncContext asyncContext = null;
        try {
            asyncContext = req.startAsync();

            //*** set timeout to 30 minutes
            asyncContext.setTimeout(UPLOAD_TIMEOUT);

            asyncContext.start(new UploadToCol(
                    fileObjectService,
                    userBean.getCurrentAccount(),
                    asyncContext,
                    collectionService));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            if (asyncContext != null) {
                asyncContext.complete();
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final PrintWriter out = resp.getWriter();
            JsonObject json = Json.createObjectBuilder()
                    .add("error", "get request not implemented.")
                    .build();
            out.write(json.toString());
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
