/*
 * Text eXtractor
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
package de.ipb_halle.tx.text.properties;

public class Word extends TextPropertyBase {

    public final static String TYPE = "WORD";

    /**
     * true if this word is a stopword; default false
     */
    private boolean stopWord;

    private String stem;
    private Set<String> stemSet;

    /*
     * default constructor
     */
    public Word() {
        this(0, 0);
    }

    /**
     * constructor
     */
    public Word(int start, int end) {
        super(start, end);
        this.stopWord = false;
    }

    /**
     * @param stem the stem of this word as determined by a stemming algorithm
     */
    public void addStem(String stem) {
        if ((this.stem == null) && (this.stemSet == null)) {
            this.stem = stem;
        } else {
            if (this.stemSet == null) {
                this.stemSet = new HashSet<> ();
                this.stemSet.add(this.stem);
                this.stem = null;
            }
            this.stemSet.add(stem);
        }
    }

    /**
     * @return returns the stem of this Word, if one has been set 
     * by a stemming algorithm
     */
    public String getStem() {
        if (this.stemSet != null) {
            throw new IllegalStateException("call to getStem() when multiple stems are present");
        }
        return this.stem;
    }

    /**
     * @return a set of stems if at least on stem has been set
     */
    public Set<String> getStemSet() {
        if (this.stemSet == null) {
            if (this.stem != null) {
                Set<String> s = new HashSet<> ();
                s.add(this.stem);
                return s;
            } 
            return null;
        }
        return this.stemSet;
    }

    public boolean getStopWord() {
        return this.stopWord;
    }

    public String getType() {
        return TYPE;
    }

    /**
     * @param sw true if the Word is a stopword, false otherwise
     */
    public void setStopWord(boolean sw) {
        this.stopWord = sw;
    }
}
