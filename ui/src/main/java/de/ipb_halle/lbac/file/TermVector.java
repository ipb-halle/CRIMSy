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

import de.ipb_halle.lbac.entity.DTO;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author fmauz
 */
public class TermVector implements Serializable, DTO {

    private String wordRoot;
    private Integer fileId;

    private int termFrequency;

    public TermVector() {
    }

    /**
     * Constructor
     *
     * @param entity
     */
    public TermVector(TermVectorEntity entity) {
        this.wordRoot = entity.getId().getWordroot();
        this.fileId = entity.getId().getFile_id();
        this.termFrequency = entity.getTermFrequency();
    }

    public TermVector(String wordRoot, Integer fileId, int termFrequency) {
        this.wordRoot = wordRoot;
        this.fileId = fileId;
        this.termFrequency = termFrequency;
    }

    @Override
    public TermVectorEntity createEntity() {
        return new TermVectorEntity()
                .setId(new TermVectorId(wordRoot, fileId))
                .setTermFrequency(termFrequency);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TermVector other = (TermVector) obj;
        if (!Objects.equals(this.wordRoot, other.wordRoot)) {
            return false;
        }
        if (!Objects.equals(this.fileId, other.fileId)) {
            return false;
        }
        return true;
    }

    public Integer getFileId() {
        return fileId;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public String getWordRoot() {
        return wordRoot;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.wordRoot);
        hash = 29 * hash + Objects.hashCode(this.fileId);
        return hash;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }

    public void setWordRoot(String wordRoot) {
        this.wordRoot = wordRoot;
    }

}
