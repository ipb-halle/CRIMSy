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

import java.util.Objects;

/**
 *
 * @see TermVectorEntity for SqlResultSetMapping
 * @author fmauz
 */
public class StemmedWordOrigin {

    private String stemmedWord;
    private String originalWord;

    public StemmedWordOrigin(String wordroot, String original) {
        this.stemmedWord = wordroot;
        this.originalWord = original;
    }

    public StemmedWordOrigin() {
    }

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
        final StemmedWordOrigin other = (StemmedWordOrigin) obj;
        if (!Objects.equals(this.originalWord, other.originalWord)) {
            return false;
        }
        if (!Objects.equals(this.stemmedWord, other.stemmedWord)) {
            return false;
        }
        return true;
    }

    public String getOriginalWord() {
        return originalWord;
    }


    public String getStemmedWord() {
        return stemmedWord;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(originalWord) + Objects.hashCode(stemmedWord);
    }

    public void setOriginalWord(String o) {
        this.originalWord = o;
    }
    public void setStemmedWord(String s) {
        this.stemmedWord = s;
    }
}
