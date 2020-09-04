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
     * @return returns the stem of this Word, if one has been set 
     * by a stemming algorithm
     */
    public String getStem() {
        return this.stem;
    }

    public boolean getStopWord() {
        return this.stopWord;
    }

    public String getType() {
        return TYPE;
    }

    /**
     * @param stem the stem of this word as determined by a stemming algorithm
     */
    public void setStem(String stem) {
        this.stem = stem;
    }

    /**
     * @param sw true if the Word is a stopword, false otherwise
     */
    public void setStopWord(boolean sw) {
        this.stopWord = sw;
    }
}
