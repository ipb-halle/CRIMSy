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

import de.ipb_halle.kx.file.AttachmentHolder;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.service.TextWebRequestType;
import de.ipb_halle.kx.service.TextWebStatus;
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import de.ipb_halle.kx.termvector.TermVectorService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.save.FileSaver;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.json.Json;
import javax.json.JsonObject;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class UploadToCol implements Runnable {

    protected CollectionService collectionService;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected AsyncContext asyncContext;
    protected final String HTTP_PART_FILENAME = "qqfile";
    protected static final String HTTP_PARAMETER_COLLECTION = "collection";
    protected String fileName;
    protected FileSaver fileSaver;
    protected Integer fileId;
    protected FileObjectService fileObjectService;
    protected AnalyseClient analyseClient = new AnalyseClient();
    private final Logger logger;

    public UploadToCol(
            FileObjectService fileObjectService,
            User user,
            AsyncContext asyncContext,
            CollectionService collectionService) {
        this.fileSaver = new FileSaver(fileObjectService, user);
        this.asyncContext = asyncContext;
        this.collectionService = collectionService;
        this.request = (HttpServletRequest) asyncContext.getRequest();
        this.response = (HttpServletResponse) asyncContext.getResponse();
        this.fileObjectService = fileObjectService;
        this.logger = LogManager.getLogger(UploadToCol.class);
    }

    private String createJsonErrorResponse(String errorMessage) {
        JsonObject json = Json.createObjectBuilder()
                .add("success", false)
                .add("error", errorMessage)
                .build();
        return json.toString();
    }

    private String createJsonSuccessResponse(Integer id, String fileName) {
        JsonObject json = Json.createObjectBuilder()
                .add("success", true)
                .add("newUuid", id.toString())
                .add("uploadName", fileName)
                .build();
        return json.toString();
    }

    protected AttachmentHolder getAttachmentTarget() throws Exception {
        request = (HttpServletRequest) asyncContext.getRequest();
        final String collectionName = asyncContext.getRequest().getParameter(HTTP_PARAMETER_COLLECTION);
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", collectionName);
        cmap.put("local", true);
        List<Collection> cl = collectionService.load(cmap);
        if (cl.isEmpty()) {
            throw new Exception("Could not find collection with name " + collectionName);
        }
        return cl.get(0);
    }

    protected String getFileNameFromRequest() throws IOException, ServletException {
        request = (HttpServletRequest) asyncContext.getRequest();
        return request.getPart(HTTP_PART_FILENAME).getSubmittedFileName();
    }

    @Override
    public void run() {
        try {
            fileId = fileSaver.saveFile(
                    getAttachmentTarget(),
                    getFileNameFromRequest(),
                    request.getPart(HTTP_PART_FILENAME).getInputStream());
            
            
            TextWebStatus status = analyseClient.analyseFile(fileId, TextWebRequestType.SUBMIT);
            while (status == TextWebStatus.BUSY) {
                Thread.sleep(400);
                status = analyseClient.analyseFile(fileId, TextWebRequestType.QUERY);
            } 
            if (status != TextWebStatus.DONE) {
                throw new Exception("Analysis returned an unexpected status code: " + status.toString());
            } 
            response.getWriter().write(createJsonSuccessResponse(fileId, getFileNameFromRequest()));

        } catch (Exception e) {
            writeErrorMessage(e);
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            asyncContext.complete();
        }

    }

    private void writeErrorMessage(Exception outerException) {
        try (PrintWriter writer = response.getWriter()) {
            writer.write(createJsonErrorResponse(outerException.getMessage()));
        } catch (IOException ex) {
            logger.error(ExceptionUtils.getStackTrace(ex));
        }
    }

}
