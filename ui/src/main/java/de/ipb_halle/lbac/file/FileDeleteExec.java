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

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.service.FileService;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileDeleteExec implements Runnable {

    private AsyncContext asyncContext;
    private FileObject fileObject;
    private Logger logger;

    private FileObjectService fileObjectService;
    private FileService fs;

    public FileDeleteExec(FileObject fo, FileObjectService fos, AsyncContext asyncContext) {

        this.logger = LogManager.getLogger(FileDeleteExec.class);
        this.asyncContext = asyncContext;

        this.fileObject = fo;
        this.fileObjectService = fos;

        fs = new FileService();
    }

    @Override
    public void run() {

        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        PrintWriter out = null;

        try {

            out = response.getWriter();
            response.setContentType("text/html");

            try {
                fs.deleteFile(fileObject.getFileLocation());
                this.logger.info(String.format("File %s in repository deleted.", fileObject.getName()));
            } catch (Exception e) {
                this.logger.warn(String.format("Error deleting file %s in repository.", fileObject.getName()));
                this.logger.warn(e.getMessage());
            }

            try {
                fileObjectService.delete(fileObject);
                this.logger.info(String.format("file %s in db deleted.", fileObject.getName()));
            } catch (Exception e) {
                this.logger.warn(String.format("Error deleting file entity  %s in db.", fileObject.getName()));
                this.logger.warn(e.getMessage());
            }
            this.logger.info(String.format("delete File %s complete.", fileObject.getName()));
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            this.logger.info(String.format("error deleting File."));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            asyncContext.complete();
            this.logger.info("run::async context complete.");
        }
    }
}
