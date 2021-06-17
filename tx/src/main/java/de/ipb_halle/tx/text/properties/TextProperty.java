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

public interface TextProperty extends Comparable<TextProperty>, Cloneable {

    public TextProperty adjust(int offset);

    public TextProperty adjust(int startOffset, int endOffset);

    public Object clone() throws CloneNotSupportedException;

    public String dump(String text);

    public int getEnd();

    public int getStart();

    public String getType();

}
