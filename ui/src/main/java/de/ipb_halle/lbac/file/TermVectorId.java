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

import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;
import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class TermVectorId implements Serializable {

    private Integer file_id;
    @AttributeTag(type = AttributeType.WORDROOT)
    private String wordroot;

    public TermVectorId() {
    }

    public TermVectorId(String wordRoot, Integer fileId) {
        this.wordroot = wordRoot;
        this.file_id = fileId;
    }

    public Integer getFile_id() {
        return file_id;
    }

    public String getWordroot() {
        return wordroot;
    }

    public void setFile_id(Integer file_id) {
        this.file_id = file_id;
    }

    public void setWordroot(String wordroot) {
        this.wordroot = wordroot;
    }

}
