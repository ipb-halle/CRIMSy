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

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.cloud.solr.SolrUpdate;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.util.HexUtil;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileUpload implements Runnable {

    private final String TMP = "tmp";
    private final String PARTNAME = "qqfile";
    private final String PREFIX = "upload";
    private final String HASH = "MD5";

    private final AsyncContext asyncContext;
    private String uploadPath;
    private Collection collection;
    private User user;
    private final Logger logger;

    private final FileEntityService fileEntityService;

    private final SolrTermVectorSearch solrTermVectorSearch;

    private final CollectionBean collectionBean;

    private final SolrSearcher solrSearcher;

    private final TermVectorEntityService termVectorEntityService;

    private final SolrUpdate solrUpdate;
    private ResourceBundle messages;

    private TermVectorParser termVectorParser = new TermVectorParser();

    public FileUpload(
            AsyncContext asyncContext,
            CollectionService collectionService,
            FileService fileService,
            SolrTermVectorSearch solrTermVectorSearch,
            FileEntityService fileEntityService,
            CollectionBean collectionBean,
            SolrSearcher solrSearcher,
            SolrUpdate solrUpdate,
            ResourceBundle messages,
            TermVectorEntityService termVectorEntityService,
            User user) throws Exception {

        this.logger = LogManager.getLogger(FileUpload.class);
        this.asyncContext = asyncContext;
        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();

        final String requestedCollection = asyncContext.getRequest().getParameter("collection");

        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", requestedCollection);
        cmap.put("local", true);
        this.user = user;
        List<Collection> cl = collectionService.load(cmap);

        if ((cl == null) || (cl.isEmpty())) {
            this.logger.error("FileUploadExec::could not obtain requested collection");

            asyncContext.complete();
            throw new Exception("No collection found with name " + requestedCollection);
        } else {
            this.collection = cl.get(0);
            this.uploadPath = fileService.getStoragePath(this.collection.getName());
        }

        this.solrTermVectorSearch = solrTermVectorSearch;
        this.fileEntityService = fileEntityService;
        this.collectionBean = collectionBean;
        this.solrSearcher = solrSearcher;
        this.solrUpdate = solrUpdate;
        this.messages = messages;
        this.termVectorEntityService = termVectorEntityService;

    }

    /**
     * Consumes a documentstream from a http request and makes it part of the
     * collection. The response will be JSON with a success tag [true|false] and
     * in error case with a tag error which has information about the reason.
     * <ol>
     * <li>fetch infos about the file from http request and session</li>
     * <li>saves it to a temp location </li>
     * <li>checkes if the file already exists</li>
     * <li>saves it to the final location</li>
     * <li>saves the document in solr</li>
     * <li>saves the language depent termvector in the db</li>
     * <li>updates the session information</li>
     * </ol>
     */
    @Override
    public void run() {
        logger.info("Start File Upload");
        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        PrintWriter out = null;
        Path tmpFile = null;
        Path destFile = null;

        try {
            String fileName = request.getPart(PARTNAME).getSubmittedFileName();

            out = response.getWriter();
            response.setContentType("application/json");

            checkDataType(fileName);

            tmpFile = createTempFolder();

            byte[] digest = saveFileInTempFolder(request, tmpFile);

            fileEntityService.checkIfFileAlreadyExists(
                    HexUtil.toHex(digest),
                    collection);

            destFile = saveFileInFinalFolder(digest, tmpFile);

            Integer id = saveDocumentInSolr(fileName, destFile.toString());

            //Loads the document from solr because there are now more
            //information e.g. the language of the document
            Document d = solrSearcher.getDocumentById(id, collection.getId());

            saveFileWithTermVectorInDB(id, fileName, digest, destFile, d);

            try {
                String x = solrSearcher.getTermPositions(d, collection.getIndexPath());
                List<StemmedWordOrigin> wordOrigins = termVectorParser.parseTermVectorXmlToWordOrigins(d, x);
                termVectorEntityService.saveUnstemmedWordsOfDocument(wordOrigins, id);
            } catch (Exception unstemmedWordException) {
                logger.error(
                        "Error of getting unstemmed words for " + d.getOriginalName(),
                        unstemmedWordException);
            }

            updateCollectionCountInfos();

            out.write(createJsonSuccessResponse(id, fileName));

        } catch (Exception ex) {
            this.logger.warn("xxxx caught an exception:", (Throwable) ex);
            if (destFile != null) {
                logger.error("Delete: " + destFile);
                try {
                    Files.delete(destFile);
                } catch (Exception e) {
                    logger.error("Error at deleting: " + destFile);
                }
            }
            this.logger.error(ex);
            String errorMessage = messages.getString(ex.getMessage());

            if (errorMessage == null) {
                errorMessage = messages.getString("fileupload_error_unknown_error");
            }
            if (ex instanceof UnsupportedLanguageException) {
                out.write(createJsonErrorResponse(errorMessage + ((UnsupportedLanguageException) ex).getLanguage()));
            } else {
                out.write(createJsonErrorResponse(errorMessage));
            }
        } finally {

            try {
                if (tmpFile != null) {
                    Files.delete(tmpFile);
                }
            } catch (Exception e) {
                logger.error("Error at deleting temp File: " + tmpFile);
            }
            asyncContext.complete();
        }
        logger.info("Finished File Upload");
    }

    private String createJsonSuccessResponse(Integer id, String fileName) {
        JsonObject json = Json.createObjectBuilder()
                .add("success", true)
                .add("newUuid", id.toString())
                .add("uploadName", fileName)
                .build();
        return json.toString();
    }

    private String createJsonErrorResponse(String errorMessage) {
        JsonObject json = Json.createObjectBuilder()
                .add("success", false)
                .add("error", errorMessage)
                .build();
        return json.toString();
    }

    private byte[] saveFileInTempFolder(HttpServletRequest request, Path tmpFile) throws Exception {
        try {
            OutputStream outputStream;
            InputStream inputStream = null;
            MessageDigest md = MessageDigest.getInstance(HASH);

            final Part part = request.getPart(PARTNAME);

            inputStream = part.getInputStream();

            //*** create input stream decorator md5 hash ***
            DigestInputStream dis = new DigestInputStream(inputStream, md);
            //*** temp file location ***
            outputStream = new FileOutputStream(tmpFile.toFile());

            byte[] b = new byte[1024];
            int i = 0;

            while ((i = dis.read(b)) != -1) {
                outputStream.write(b, 0, i);
            }
            outputStream.flush();
            outputStream.close();

            return md.digest();
        } catch (Exception e) {
            logger.error(e);
            throw new Exception("Could not save file on server", e);
        }
    }

    public Integer saveDocumentInSolr(String fileName, String dest) throws Exception {

        /**
         * xxxxxxx - Needs a massive refactoring: id must be set by database
         * instead of random
         */
        try {
            Integer id = -1000;

            Document doc = new Document();
            doc.setId(id);
            doc.setCollectionId(collection.getId());
            doc.setCollection(collection);
            doc.setNodeId(collection.getNode().getId());
            doc.setNode(collection.getNode());
            doc.setContentType("application/pdf");
            doc.setPath(dest);

            Map<String, String> params = new HashMap<>();
            params.put("literal.id", id.toString());
            params.put("literal.permission", "PERMISSION ALLES ERLAUBT");

            params.put("literal.original_name", fileName);
            params.put("literal.upload_date", new Date().toString());
            params.put("literal.storage_location", doc.getPath());

            solrUpdate.update(doc, params);

            return id;
        } catch (Exception e) {
            if (isLanguageUnsupported(e)) {
                String language = e.getMessage().split(":")[e.getMessage().split(":").length - 1].trim();

                throw new UnsupportedLanguageException("fileupload_error_unsupported_language", language, e);

            }
            throw new Exception("fileupload_error_unknown_error", e);
        }
    }

    private boolean isLanguageUnsupported(Exception e) {
        return e.getMessage().contains("Invalid output field mapping for text field and language:");
    }

    private Path createTempFolder() throws Exception {
        Path tmpFile = null;
        try {
            Files.createDirectories(Paths.get(this.uploadPath, TMP));
            tmpFile = Files.createTempFile(Paths.get(this.uploadPath, TMP), PREFIX, TMP);
        } catch (Exception e) {
            logger.error(e);
            throw new Exception("Could not create TEMP folder", e);

        }
        return tmpFile;
    }

    private Path saveFileInFinalFolder(byte[] digest, Path tmpFile) throws Exception {
        Path destDir = Paths.get(this.uploadPath, HexUtil.toHex(digest[0]), HexUtil.toHex(digest[1]));
        Path destFile = Paths.get(this.uploadPath, HexUtil.toHex(digest[0]),
                HexUtil.toHex(digest[1]), HexUtil.toHex(digest));
        Files.createDirectories(destDir);

        Files.copy(tmpFile, destFile);

        return destFile;
    }

    private void saveFileWithTermVectorInDB(Integer id, String fileName, byte[] digest, Path destFile, Document d) throws Exception {
        FileObject fileEntity = new FileObject();
        fileEntity.setId(id);
        fileEntity.setName(fileName);
        fileEntity.setHash(HexUtil.toHex(digest));
        fileEntity.setCreated(new Date());
        fileEntity.setFilename(destFile.toString());
        fileEntity.setCollection(collection);
        fileEntity.setUser(user);
        fileEntity.setDocument_language(d.getLanguage());
        String tvJsonString = solrTermVectorSearch.getTermVector(d);

        fileEntityService.save(fileEntity);

        TermVectorParser tvParser = new TermVectorParser();
        fileEntityService.saveTermVectors(tvParser.parseTermVectorJson(tvJsonString, id));

    }

    private void updateCollectionCountInfos() {
        for (Collection c : collectionBean.getCollectionSearchState().getCollections()) {
            if (c.getId().equals(collection.getId())) {
                c.setCountDocs(c.getCountDocs() + 1);
            }
        }
    }

    private void checkDataType(String fileName) throws Exception {
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new Exception("fileupload_error_unsupported_file_type");
        }
    }

    public void setTermVectorParser(TermVectorParser termVectorParser) {
        this.termVectorParser = termVectorParser;
    }

}
