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
import javax.inject.Inject;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@WebServlet(name = "TextWebService", urlPatterns = {"/process/*"}, asyncSupported = true)
public class TextWebService extends HttpServlet {

    private final static long serialVersionUID = 1L;

    private final Logger logger = LogManager.getLogger(TextWebService.class);

    @Resource(name = "kxExecutorService")
    private ManagedExecutorService managedExecutorService;

    @Inject
    private JobTracker jobTracker;

    @Inject
    private FileObjectService fileObjectService;

    @Inject
    private TermVectorService termVectorService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("doGet(): request received.");
        try {
            final TextWebRequestType requestType = TextWebRequestType.valueOf(req.getParameter(TextWebRequestType.PARAMETER));
            final Integer fileId = Integer.parseInt(req.getParameter("fileId"));
            final PrintWriter out = resp.getWriter();
            out.write(processRequest.toString());
        } catch (IOException e) {
            logger.error((Throwable) e);
        }
    }

    private TextWebStatus processRequest(Integer fileId, TextWebRequestType requestType) {
        try {
            if (requestType == SUBMIT) {
                return processSubmitRequest(fileId);
            } else {
                return processQueryRequest(fileId);
            }
        } catch (Exception e) {
            logger.warn((Throwable) e);
        }
        return TextWebStatus.ERROR;
    }

    private TextWebStatus processSubmitRequest(Integer fileId) {
        FileObject fileObj = fileObjectService.loadFileObjectById(fileId);

        FileAnalyser analyser = new FileAnalyser()
            .setFileObject(fileObj);
        jobTracker.putJob(fileId, analyser);
        managedExecutorService.submit(analyser);
        return analyser.getStatus();
    }

    private TextWebStatus processQueryRequest(Integer fileId) {
        FileAnalyser analyser = jobTracker.getJob(fileId);
        if (analyser == null) {
            return TextWebStatus.ERROR;
        }
        TextWebStatus status = analyser.getStatus();

        if (status == TextWebStatus.BUSY) {
            return status;
        }

        if (status == TextWebStatus.DONE) {
            saveResults(analyser);
        }

        // ERROR or DONE
        jobTracker.remove(fileId);
        return status;
    }

    private void saveResults(FileAnalyser analyser) {
        saveLanguage(analyser);
        termVectorService.saveUnstemmedWordsOfDocument(
            analyser.getWordOrigins(), 
            analyser.getFileObject().getId());
        termVectorService.saveTermVectors(analyser.getTermVector());
    }

    private void saveLanguage(FileAnalyser analyser) {
        FileObject fileObj = fileObjectService.loadFileObjectById(
            analyser.getFileObject().getId());
        fileObj.setLanguage(
            analyser.getLanguage());
        fileObjectService.save(fileObj);
    }

    // for testing
    protected void setExecutorService(ManagedExecutorService mes) {
        managedExecutorService = mes;
    }
}
