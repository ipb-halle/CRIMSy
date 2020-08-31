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

import java.io.Serializable;
import java.util.List;

/**
 * Collects File Entities into a list. Needed for transportation via the REST
 * API.
 *
 * @author fbroda
 */
public class FileEntityList implements Serializable {

    private final static long serialVersionUID = 1L;

    List<FileObject> fileEntities;

    public List<FileObject> getFileEntities() {
        return fileEntities;
    }

    public void setFileEntities(List<FileObject> fileEntities) {
        this.fileEntities = fileEntities;
    }
}
