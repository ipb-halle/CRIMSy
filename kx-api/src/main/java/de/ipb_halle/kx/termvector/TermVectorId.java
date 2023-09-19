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
package de.ipb_halle.kx.termvector;

import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
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

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            assert(file_id != null);
            assert(wordroot != null);

            if (o instanceof TermVectorId) {
                TermVectorId otherId = (TermVectorId) o;
                return file_id.equals(otherId.file_id)
                    && wordroot.equals(otherId.wordroot);
            }
        }
        return false;
    }

    public Integer getFile_id() {
        return file_id;
    }

    public String getWordroot() {
        return wordroot;
    }

    @Override
    public int hashCode() {
        return this.wordroot.hashCode() + this.file_id.intValue();
    }

    public void setFile_id(Integer file_id) {
        this.file_id = file_id;
    }

    public void setWordroot(String wordroot) {
        this.wordroot = wordroot;
    }

}
