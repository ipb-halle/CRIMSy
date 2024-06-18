/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.util.jsf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jakarta.enterprise.context.RequestScoped;

import org.omnifaces.util.Faces;

/**
 * Injectable JSF backing bean that acts as facade for OmniFaces'
 * {@link Faces#sendFile} methods.
 *
 * @author flange
 */
@RequestScoped
public class SendFileBean {
    /**
     * Send the given byte array as a file to the client.
     * 
     * @param content content of the file
     * @param filename name of the file
     * @throws IOException
     */
    public void sendFile(byte[] content, String filename) throws IOException {
        Faces.sendFile(content, filename, true);
    }
    /**
     * Send the given file to the client.
     * 
     * @param file the file to send
     * @throws IOException
     */
    public void sendFile(File file) throws IOException {
        Faces.sendFile(file, true);
    }

    /**
     * Send the given input stream as a file to the client.
     * 
     * @param content content of the file
     * @param filename name of the file
     * @throws IOException
     */
    public void sendFile(InputStream content, String filename) throws IOException {
        Faces.sendFile(content, filename, true);
    }
}
