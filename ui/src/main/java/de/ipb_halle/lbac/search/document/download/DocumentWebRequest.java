/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.search.document.download;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import de.ipb_halle.lbac.webservice.WebRequest;

/**
 * POJO for communication between {@link DocumentWebService} and
 * {@link DocumentWebClient}.
 * 
 * @author flange
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentWebRequest extends WebRequest {
    private static final long serialVersionUID = 1L;

    private Integer fileObjectId;

    public Integer getFileObjectId() {
        return fileObjectId;
    }

    public void setFileObjectId(Integer fileObjectId) {
        this.fileObjectId = fileObjectId;
    }
}