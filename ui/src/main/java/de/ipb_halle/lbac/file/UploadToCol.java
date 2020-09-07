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

import de.ipb_halle.lbac.file.save.FileAnalyser;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.save.FileSaver;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import de.ipb_halle.lbac.file.save.AttachmentHolder;

/**
 *
 * @author fmauz
 */
public class UploadToCol implements Runnable {

    protected CollectionService collectionService;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected AsyncContext asyncContext;
    private final String HTTP_PART_FILENAME = "qqfile";
    private static final String HTTP_PARAMETER_COLLECTION = "collection";
    protected String fileName;
    protected FileSaver fileSaver;
    protected FileAnalyser fileAnalyser;

    @Override
    public void run() {
        try {
            fileSaver.saveFile(
                    getAttachmentTarget(),
                    getFileNameFromRequest(),
                    request.getPart(HTTP_PART_FILENAME).getInputStream());
            
            fileAnalyser.analyseFile(fileSaver.getFileLocation().toString());
            
            saveTermVector(fileAnalyser.getTermVector());
            saveOriginalWords(fileAnalyser.getWordOrigins());
            fileSaver.updateLanguageOfFile(fileAnalyser.getLanguage());

        } catch (Exception e) {
            handleError(e);
        } finally {
            asyncContext.complete();
        }

    }

    protected void saveTermVector(List<TermVector> termVector) {

    }

    protected void saveOriginalWords(List<StemmedWordOrigin> originals) {

    }

    protected String getFileNameFromRequest() throws IOException, ServletException {
        request = (HttpServletRequest) asyncContext.getRequest();
        return request.getPart(HTTP_PART_FILENAME).getSubmittedFileName();
    }

    protected void handleError(Exception e) {

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

}
