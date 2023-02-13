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
package de.ipb_halle.lbac.util.ssl;

import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.globals.KeyStoreFactory;

import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.KeyManagerFactory;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * SecureWebClient This class configures a JAX-RS WebClient to use our keystore
 * and truststore when making requests. The class implements the Singleton
 * pattern, but methods of interest are provided as static methods.
 */
public class SecureWebClientBuilder {
    
    private static final Integer CONNECTION_TIMEOUT_IN_MS = 10 * 1000;
    private static final Integer READ_TIMEOUT_IN_MS = 30 * 1000;
    
    private static SecureWebClientBuilder instance;
    private Map<String, SSLContext> sslContext;
    private Logger logger;

    /**
     * private constructor
     */
    private SecureWebClientBuilder() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.sslContext = new HashMap<>();
    }

    /**
     * createCollection a webclient using the SSLContext of this class.basePath
     * and localPath are just concatenated.
     *
     * @param cloudNode
     * @param localPath an arbitrary suffix which is appended to the base URL
     * (e.g /rest/nodes)
     * @return a WebClient with dedicated SSLSocketFactory which is configured
     * to mutual certificate based authentication
     */
    public static WebClient createWebClient(CloudNode cloudNode, String localPath) {
        
        WebClient wc = WebClient.create(cloudNode.getNode().getBaseUrl() + localPath);
        
        ClientConfiguration cc = WebClient.getConfig(wc);
        
        HTTPConduit hc = cc.getHttpConduit();
        TLSClientParameters tcp = new TLSClientParameters();
        tcp.setSSLSocketFactory(
                SecureWebClientBuilder.getSSLSocketFactory(cloudNode.getCloud()));
        hc.setTlsClientParameters(tcp);
        
        hc.getClient().setConnectionTimeout(CONNECTION_TIMEOUT_IN_MS);
        hc.getClient().setReceiveTimeout(READ_TIMEOUT_IN_MS);
        return wc;
    }

    /**
     * @return returns the Singleton instance of this class
     */
    private synchronized static SecureWebClientBuilder getInstance() {
        if (instance == null) {
            instance = new SecureWebClientBuilder();
        }
        return instance;
    }

    /**
     * Returns the SSLSocketFactory for this class SSLContext. This is needed by
     * DocumentServlet, which uses a Servlet API instead of a REST API.
     *
     * @return the SSLSocketFactory
     */
    public static SSLSocketFactory getSSLSocketFactory(Cloud cloud) {
        SecureWebClientBuilder instance = SecureWebClientBuilder.getInstance();
        String cloudName = cloud.getName();
        SSLContext ctx = instance.sslContext.get(cloudName);
        if (ctx == null) {
            synchronized (SecureWebClientBuilder.class) {
                ctx = instance.sslContext.get(cloudName);
                if (ctx == null) {
                    ctx = instance.init(cloudName);
                }
            }
            
        }
        return ctx.getSocketFactory();
    }

    /**
     * Initialize the SSLContext
     */
    private SSLContext init(String cloudName) {
        try {
            
            KeyStore ks = KeyStoreFactory.getInstance().getKeyStore(cloudName);
            KeyStore ts = KeyStoreFactory.getInstance().getTrustStore(cloudName);
            
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            kmf.init(ks, KeyStoreFactory.getInstance().getKeyPass());
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            tmf.init(ts);

            // helper(tmf);
            SSLContext ctx = SSLContext.getInstance(
                    KeyStoreFactory.getInstance().getSSL_PROTOCOL());
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
                    new java.security.SecureRandom());
            this.sslContext.put(cloudName, ctx);
            return ctx;
            
        } catch (Exception e) {
            this.logger.warn("init() caught an exception: ", e);
        }
        return null;
    }

    /**
     * log some information for debugging purposes
     */
    private void helper(TrustManagerFactory tmf) {
        for (TrustManager tm : tmf.getTrustManagers()) {
            try {
                for (X509Certificate c : ((X509TrustManager) tm).getAcceptedIssuers()) {
                    this.logger.info("Principal: " + c.getSubjectX500Principal().toString());
                }
            } catch (Exception e) {
                this.logger.info("helper() Caught an exception", (Throwable) e);
                this.logger.info(String.format("TrustManager dump: %s --> %s", tm.getClass().getName(), tm.toString()));
            }
        }
    }
    
}
