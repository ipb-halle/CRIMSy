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
package de.ipb_halle.lbac.search.wordcloud;

import java.io.Serializable;

/**
 *
 * @author fmauz
 */
public class WordTerm implements Comparable<WordTerm>,Serializable {

    private int absoluteFrequency;
    private float relativeFrequency;
    private String term;
    private FreqCategory category;
    private int docsWithTerm;
    private String wordRepresantation;

    public WordTerm(int aboluteFrequency, String term, String wordRepresantation) {
        this(aboluteFrequency, term, 0, wordRepresantation);
    }

    public WordTerm(int aboluteFrequency, String term, int docsWithTerm, String wordRepresantation) {
        this.absoluteFrequency = aboluteFrequency;
        this.term = term;
        this.category = FreqCategory.NONE;
        this.docsWithTerm = docsWithTerm;
        this.wordRepresantation = wordRepresantation;
    }

    @Override
    public int compareTo(WordTerm o) {
        if (o.getAboluteFrequency() < absoluteFrequency) {
            return 1;
        } else if (o.getAboluteFrequency() == getAboluteFrequency()) {
            return 0;
        } else {
            return -1;
        }
    }

    public int getAboluteFrequency() {
        return absoluteFrequency;
    }

    public float getRelativeFrequency() {
        return relativeFrequency;
    }

    public String getTerm() {
        return term;
    }

    public FreqCategory getCategory() {
        return category;
    }

    public void setAboluteFrequency(int aboluteFrequency) {
        this.absoluteFrequency = aboluteFrequency;
    }

    public void setRelativeFrequency(float relativeFrequency) {
        this.relativeFrequency = relativeFrequency;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setCategory(FreqCategory category) {
        this.category = category;
    }

    public int getDocsWithTerm() {
        return docsWithTerm;
    }

    public void setDocsWithTerm(int docsWithTerm) {
        this.docsWithTerm = docsWithTerm;
    }

    public String getWordRepresantation() {
        return wordRepresantation;
    }

}
