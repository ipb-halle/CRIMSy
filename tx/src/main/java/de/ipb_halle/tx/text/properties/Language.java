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

public class Language extends TextPropertyBase {

    public final static String TYPE = "LANGUAGE";

    /**
     * iso language code, may also read "default"
     */
    private String language;

    /**
     * default constructor
     */
    public Language() {
        this(0,0);
    }

    /**
     * default language constructor
     */
    public Language(int start, int end) {
        this(start, end, "default");
    }

    /**
     * constructor
     */
    public Language(int start, int end, String lang) {
        super(start, end);
        this.language = lang;
    }

    @Override
    public String dump(String text) {
        if ((getEnd() - getStart()) < 200) {
            return String.format("%s %s %6d %6d %s", TYPE, this.language, getStart(), getEnd(), text);
        }
        return String.format("%s %s %6d %6d", TYPE, this.language, getStart(), getEnd());
    }

    public String getLanguage() { 
        return this.language;
    }

    public String getType() {
        return TYPE;
    }
}
