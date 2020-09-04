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

public class Line extends TextPropertyBase {

    public final static String TYPE = "LINE";

    private int lineNumber;

    /**
     * default constructor
     */
    public Line() {
        this(0, 0, 0);
    }
        
    /**
     * constructor
     */
    public Line(int start, int end, int lineNumber) {
        super(start, end);
        this.lineNumber = lineNumber;
    }

    @Override
    public String dump(String text) {
        return String.format("%s %6d %s", TYPE, this.lineNumber, text);
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public String getType() {
        return TYPE;
    }
}
