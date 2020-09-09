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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.save.FileAnalyser;
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
import de.ipb_halle.lbac.file.save.AttachmentHolder;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.File;

import javax.json.Json;
import javax.json.JsonObject;
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
    protected FileAnalyser fileAnalyser;
    protected TermVectorEntityService termVectorService;
    protected Integer fileId;
    protected FileEntityService fileEntityService;
    private final Logger logger;

    public UploadToCol(
            String filterDefinition,
            FileEntityService fileEntityService,
            User user,
            AsyncContext asyncContext,
            CollectionService collectionService,
            TermVectorEntityService termVectorService) {
        fileAnalyser = new FileAnalyser(new File(filterDefinition));
        fileSaver = new FileSaver(fileEntityService, user);
        this.asyncContext = asyncContext;
        this.collectionService = collectionService;
        this.request = (HttpServletRequest) asyncContext.getRequest();
        this.response = (HttpServletResponse) asyncContext.getResponse();
        this.termVectorService = termVectorService;
        this.fileEntityService = fileEntityService;
        this.logger = LogManager.getLogger(FileUploadOld.class);
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
            try {
                fileId = fileSaver.saveFile(
                        getAttachmentTarget(),
                        getFileNameFromRequest(),
                        request.getPart(HTTP_PART_FILENAME).getInputStream());

                fileAnalyser.analyseFile(fileSaver.getFileLocation().toString(), fileId);
                saveTermVector(fileAnalyser.getTermVector());
                saveOriginalWords(fileAnalyser.getWordOrigins());
                fileSaver.updateLanguageOfFile(fileAnalyser.getLanguage());
                response.getWriter().write(createJsonSuccessResponse(fileId, getFileNameFromRequest()));
            } catch (Exception e) {
                response.getWriter().write(createJsonErrorResponse(e.getMessage()));
            } finally {
                asyncContext.complete();
            }
        } catch (IOException e2) {
            logger.error("Could not write response message", e2);
        }
    }

    protected void saveOriginalWords(List<StemmedWordOrigin> originals) {
        termVectorService.saveUnstemmedWordsOfDocument(originals, fileId);
    }

    protected void saveTermVector(List<TermVector> termVector) {
        fileEntityService.saveTermVectors(termVector);
    }

}
