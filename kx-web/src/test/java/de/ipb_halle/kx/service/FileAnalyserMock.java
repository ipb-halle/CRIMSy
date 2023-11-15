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
import de.ipb_halle.kx.service.TextWebStatus;
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import java.io.InputStream;
import java.util.List;

/**
 * Interface of FileAnalyser to enabel Mocking for test cases.
 *
 * @author fbroda
 */
public class FileAnalyserMock implements IFileAnalyser {

    public FileObject fileObject;
    public String language;
    public List<TermVector> termvectorList;
    public List<StemmedWordOrigin> originalWordList;
    public TextWebStatus status;

    public FileAnalyserMock() {
        status = TextWebStatus.BUSY;
    }

    public String getLanguage() {
        return language;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public TextWebStatus getStatus() {
        return status;
    }

    public List<TermVector> getTermVector() {
        return termvectorList;
    }

    public List<StemmedWordOrigin> getWordOrigins() {
        return originalWordList;
    }

    public void run() {
    }

    public IFileAnalyser setFileObject(FileObject f) {
        fileObject = f;
        return this;
    }

    public IFileAnalyser setFilterDefinition(InputStream def) {
        return this;
    }

    // set status for test purposes
    public void setLanguage(String l) {
        language = l;
    }

    // set status for test purposes
    public void setStatus(TextWebStatus s) {
        status = s;
    }

    // set status for test purposes
    public void setTermVectors(List<TermVector> tvl) {
        termvectorList = tvl;
    }

    // set status for test purposes
    public void setWordOrigins(List<StemmedWordOrigin> wol) {
        originalWordList = wol;
    }
}

