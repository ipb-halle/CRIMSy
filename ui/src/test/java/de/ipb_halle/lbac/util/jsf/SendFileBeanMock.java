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
import java.util.Arrays;

import javax.enterprise.context.RequestScoped;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Injectable mock of {@link SendFileBean}.
 * 
 * @author flange
 */
@RequestScoped
public class SendFileBeanMock extends SendFileBean {
    private byte[] content;
    private String filename;

    public void reset() {
        content = null;
        filename = null;
    }

    @Override
    public void sendFile(byte[] content, String filename) throws IOException {
        this.content = Arrays.copyOf(content, content.length);
        this.filename = filename;
    }

    @Override
    public void sendFile(InputStream content, String filename) throws IOException {
        this.content = IOUtils.toByteArray(content);
        this.filename = filename;
    }

    @Override
    public void sendFile(File file) throws IOException {
        content = FileUtils.readFileToByteArray(file);
        filename = file.getName();
    }

    /**
     * @return content of the last call to the {@link #sendFile()} methods.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * @return filename of the last call to the {@link #sendFile()} methods.
     */
    public String getFilename() {
        return filename;
    }
}