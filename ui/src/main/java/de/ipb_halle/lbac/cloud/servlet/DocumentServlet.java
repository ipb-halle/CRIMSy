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
package de.ipb_halle.lbac.cloud.servlet;

/**
 * DocumentServlet
 * <p>
 * todo: security checks This class currently performs no security checks. Any
 * user can retrieve any document from any collection, even system files!
 * <p>
 * <p>
 * This class delivers documents.
 */
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet to deliver Documents to local and remote destinations.
 * DocumentServlet may be called by GET and POST methods.
 */
public class DocumentServlet extends HttpServlet {

    private final static long serialVersionUID = 1L;
    private final static String HTTP_PREFIX = "http://";
    private final static String HTTPS_PREFIX = "https://";

    @Inject
    private CollectionService collectionService;

    @Inject
    private NodeService nodeService;

    @Inject
    private CloudNodeService cloudNodeService;

    private Logger logger;

    /**
     * default constructor
     */
    public DocumentServlet() {
        super();
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Implements the processing of GET requests and delegates to the
     * processRequest(request, response) method.
     *
     * @param request the HttpServletRequest for to process
     * @param response the HttpServletResponse to fill with content
     * @throws ServletException @see processRequest
     * @throws IOException @see processRequest
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.logger.info("Enter Servlet for downloading file");
        processRequest(request, response);
    }

    /**
     * Implements the processing of POST requests and delegates to the
     * processRequest(request, response) method.
     *
     * @param request the HttpServletRequest for to process
     * @param response the HttpServletResponse to fill with content
     * @throws ServletException @see processRequest
     * @throws IOException @see processRequest
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * create a error message from a resource file and deliver it to the client
     *
     * @param response the HttpServletResponse object
     * @throws IOException
     */
    private void displayError(HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html");
        writeResponse(
                this.getClass().getResourceAsStream("error.html"),
                response,
                "");
    }

    /**
     * provide an InputStream for a document from a local collection
     *
     * @param doc the local document
     * @return the InputStream
     * @throws IOException
     */
    private InputStream getDocumentStream(Document doc)
            throws IOException {

        Node node = doc.getNode();
        if (this.nodeService.isRemoteNode(node)) {
            // any Cloud object from the list should work
            Cloud cloud = this.cloudNodeService.load(null, node).get(0).getCloud();
            return getSecureRemoteDocumentStream(cloud, doc.getLink(node.getBaseUrl()));
        } else {

            /*
		 * todo: authorization of request
                 * CAVEAT: we must make sure that:
		 * - request is authorized
		 * - path does not point to some restricted location
		 *   (e.g. system files via 'foo/../../../etc/passwd')
             */
            Path documentPath = Paths.get(doc.getPath());
            this.logger.info(String.format("getDocumentStream() local file: %s", documentPath.toString()));
            return new FileInputStream(documentPath.toFile());

        }
    }

    /**
     * provide an InputStream for a HTTP-URL (especially not requiring mutual
     * certificate based authentication).
     *
     * @param urlString the url
     * @return InputStream
     * @throws IOException
     */
    private InputStream getRemoteDocumentStream(String urlString)
            throws IOException {
        /*
		 * todo: HttpURLConnection needs to be disconnected or cloesed after use?
         */
        this.logger.info("getRemoteDocumentStream(): " + urlString);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        // conn.setRequestProperty("Authorization", "Token " + this.token);
        // conn.setRequestProperty("Accept", "application/xml");
        conn.connect();
        return conn.getInputStream();
    }

    /**
     * provide an InputStream for a HTTPS-URL which may require mutual
     * certificate based authentication.
     *
     * @param urlString the URL
     * @return input stream of remote document, fetched from other node.
     * Connection uses special SSLSocketFactory for mutual certificate based
     * authentication
     * @throws IOException
     */
    private InputStream getSecureRemoteDocumentStream(Cloud cloud, String urlString)
            throws IOException {
        this.logger.info("getSecureRemoteDocumentStream(): " + urlString);
        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setSSLSocketFactory(
                SecureWebClientBuilder
                        .getSSLSocketFactory(cloud));
        conn.connect();
        this.logger.info("connect to: " + conn.getPeerPrincipal().getName());

        try {
            InputStream inputStream = conn.getInputStream();
            return inputStream;

        } catch (Exception e) {
            logger.info("Response Code:" + conn.getResponseCode());
            logger.error(e);
        }
        return null;
    }

    /**
     * write the content of an InputStream as a response
     *
     * @param in the input stream to return as a response
     * @param response the HttpServletResponse to write to
     * @throws IOException
     */
    private void writeResponse(
            InputStream in,
            HttpServletResponse response,
            String fileName)
            throws IOException {
        response.addHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
        ByteBuffer buf = ByteBuffer.allocateDirect(65536);
        buf.clear();
        try (
                ReadableByteChannel chin = Channels.newChannel(in);
                WritableByteChannel chos = Channels.newChannel(
                        response.getOutputStream())) {
            while (chin.read(buf) > 0) {
                while (buf.flip().hasRemaining()) {
                    chos.write(buf);
                    buf.compact();
                }
                buf.clear();
            }
        }
    }

    /**
     * Processes a HttpServletRequest, checks for request parameters (nodeId,
     * collectionId, ...) and on success returns the requested document.
     *
     * @param request the request
     * @param response the response
     * @throws ServletException
     * @throws IOException
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UUID nodeId = UUID.fromString(request.getParameterMap().get("nodeId")[0]);

        UUID collectionId = UUID.fromString(request.getParameterMap().get("collectionId")[0]);

        String path = request.getParameterMap().get("path")[0];
        if (path == null) {
            this.logger.info("DocumentServlet: missing document path");
            displayError(response);
            return;
        }
        //*** check optional paramter orignalName ***
        String originalName = "document.pdf";
        if (request.getParameterMap().containsKey("originalName")) {
            originalName = request.getParameterMap().get("originalName")[0];
        }

        try {

            Document doc = new Document();

            doc.setPath(path);

            doc.setCollectionId(collectionId);

            doc.setNodeId(nodeId);
            if (nodeService.getLocalNode().getId().equals(nodeId)) {
                doc.setCollection(this.collectionService.loadById(collectionId));
            }

            doc.setNode(nodeService.loadById(nodeId));

            doc.setContentType(request.getParameterMap().get("contentType")[0]);

            doc.setOriginalName(originalName);

            InputStream in = getDocumentStream(doc);
            if (in != null) {
                response.setContentType(doc.getContentType());
                writeResponse(in, response, doc.getOriginalName());
            } else {
                this.logger.info("DocumentServlet: FileInputStream is null");
                displayError(response);
                return;
            }
        } catch (Exception e) {
            logger.error(e);
            throw new ServletException(e);
        }

    }

}
