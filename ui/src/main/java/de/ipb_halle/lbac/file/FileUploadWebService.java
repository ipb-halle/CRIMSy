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
package de.ipb_halle.lbac.file;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.cloud.solr.SolrUpdate;
import de.ipb_halle.lbac.file.FileUpload;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;

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

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

@WebServlet(name = "FileUploadWebService", urlPatterns = {"/uploaddocs/*"}, asyncSupported = true)
@MultipartConfig(maxFileSize = 1024 * 1024 * 500, maxRequestSize = 1024 * 1024 * 1000)
public class FileUploadWebService extends HttpServlet {

    private final static long serialVersionUID = 1L;
    private final static long UPLOAD_TIMEOUT = 30L * 60L * 1000L;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private final Logger logger = LogManager.getLogger(FileUploadWebService.class);

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileService fileService;

    @Inject
    private SolrTermVectorSearch solrTermVectorService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private SolrSearcher solrSearcher;

    @Inject
    private TermVectorEntityService termVectorEntityService;
    
    @Inject
    private UserBean userBean;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        final AsyncContext asyncContext = req.startAsync();
        try {

            //*** set timeout to 30 minutes
            asyncContext.setTimeout(UPLOAD_TIMEOUT);
            asyncContext.start(new FileUpload(
                    asyncContext,
                    collectionService,
                    fileService,
                    solrTermVectorService,
                    fileEntityService,
                    collectionBean,
                    solrSearcher,
                    new SolrUpdate(),
                    Messages.getBundle(MESSAGE_BUNDLE),
                    termVectorEntityService,
                    userBean.getCurrentAccount()
            ));
        } catch (Exception e) {
            logger.error(e);
            asyncContext.complete();
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final PrintWriter out = resp.getWriter();
        JsonObject json = Json.createObjectBuilder()
                .add("error", "get request not implemented.")
                .build();
        out.write(json.toString());
    }
}
