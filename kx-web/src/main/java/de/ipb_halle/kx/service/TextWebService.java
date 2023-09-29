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

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.TermVectorService;
import java.io.IOException;
import java.io.PrintWriter;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.annotation.Resource;
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

    @Resource(name = "kxExecutorService")
    private ManagedExecutorService executorService;

    @Inject
    private JobTracker jobTracker;

    @Inject
    private FileObjectService fileObjectService;

    @Inject
    private TermVectorService termVectorService;


    static class FileAnalyserFactory implements IFileAnalyserFactory {
        public IFileAnalyser buildFileAnalyser() {
            return new FileAnalyser();
        }
    }

    private static IFileAnalyserFactory fileAnalyserFactory = new FileAnalyserFactory();
    private final Logger logger = LogManager.getLogger(TextWebService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("doGet(): request received.");
        try {
            final PrintWriter out = resp.getWriter();
            out.write(
                processRequest(req)
                .toString());
        } catch (IOException e) {
            logger.error((Throwable) e);
        }
    }

    private TextWebStatus processRequest(HttpServletRequest req) {
        try {
            final TextWebRequestType requestType = TextWebRequestType.valueOf(req.getParameter(TextWebRequestType.PARAMETER));
            final Integer fileId = Integer.parseInt(req.getParameter("fileId"));
            if (requestType == TextWebRequestType.SUBMIT) {
                return processSubmitRequest(fileId);
            } else {
                return processQueryRequest(fileId);
            }
        } catch (Exception e) {
            logger.warn((Throwable) e);
        }
        return TextWebStatus.PARAMETER_ERROR;
    }

    private TextWebStatus processSubmitRequest(Integer fileId) {
        FileObject fileObj = fileObjectService.loadFileObjectById(fileId);
        if (fileObj != null) {
            IFileAnalyser analyser = fileAnalyserFactory.buildFileAnalyser()
                .setFileObject(fileObj);
            jobTracker.putJob(fileId, analyser);
            executorService.submit(analyser);
            return analyser.getStatus();
        } else {
            logger.info("Could not obtain FileObject for fileId=" + fileId.toString());
        }
        return TextWebStatus.NO_INPUT_ERROR;
    }

    private TextWebStatus processQueryRequest(Integer fileId) {
        IFileAnalyser analyser = jobTracker.getJob(fileId);
        if (analyser == null) {
            return TextWebStatus.NO_SUCH_JOB_ERROR;
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

    private void saveResults(IFileAnalyser analyser) {
        saveLanguage(analyser);
        termVectorService.saveUnstemmedWordsOfDocument(
            analyser.getWordOrigins(), 
            analyser.getFileObject().getId());
        termVectorService.saveTermVectors(analyser.getTermVector());
    }

    private void saveLanguage(IFileAnalyser analyser) {
        FileObject fileObj = fileObjectService.loadFileObjectById(
            analyser.getFileObject().getId());
        fileObj.setDocumentLanguage(
            analyser.getLanguage());
        fileObjectService.save(fileObj);
    }

    // for testing
    protected void setExecutorService(ManagedExecutorService es) {
        executorService = es;
    }

    protected void setFileAnalyserFactory(IFileAnalyserFactory factory) {
        fileAnalyserFactory = factory;
    }
}
