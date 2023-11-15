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
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import java.io.InputStream;
import java.util.List;


/**
 * Interface of FileAnalyser to enabel Mocking for test cases.
 *
 * @author fbroda
 */
public interface IFileAnalyser extends Runnable {

    public String getLanguage();
    public FileObject getFileObject();
    public TextWebStatus getStatus();
    public List<TermVector> getTermVector();
    public List<StemmedWordOrigin> getWordOrigins();
    public void run();
    public IFileAnalyser setFileObject(FileObject f);
    public IFileAnalyser setFilterDefinition(InputStream def);
}
