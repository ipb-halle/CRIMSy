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

public abstract class TextPropertyBase<T> implements TextProperty, Cloneable {

    private int start;
    private int end;

    public TextPropertyBase(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Adjust the start and end values of this object. This is 
     * necessary, if two <code>TextRecord</code>s are combined
     * and the offsets for one set of properties changes.
     * @param offset the delta to be applied to the <code>start</code> and <code>end</code> properties
     * @return this object
     */
    public TextProperty adjust(int offset) {
        this.start += offset;
        this.end += offset;
        return this;
    }

    /**
     * Adjust the start and end values of this object. This 
     * method can adjust start and end differently, i.e. when 
     * the length of the underlying text span changes due to 
     * pattern matching and replacement.
     * @param startOffset offset by which to alter the <code>start</code> property
     * @param endOffset offset by which to alter the <code>end</code> property
     * @return this object
     */
    public TextProperty adjust(int startOffset, int endOffset) {
        this.start += startOffset;
        this.end += endOffset;
        return this;
    }

    /**
     * clone this object
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * @return a string describing this property
     */
    public String dump(String text) {
        return String.format("%s %6d %6d %s", getType(), this.start, this.end, text);
    }

    /**
     * Compare two TextProperties. Comparison starts with the
     * property type, continues with the start property and 
     * finally compares the end property to obtain an order of 
     * TextProperty objects.
     * @param prop the other property
     * @return -1, 0, 1 as defined in <code>interface Comparable</code>
     */
    public int compareTo(TextProperty prop) {
        int outcome = getType().compareTo(prop.getType());
        if (outcome != 0) {
            return outcome;
        }
        outcome = Integer.signum(this.start - prop.getStart());
        if (outcome != 0) {
            return outcome;
        }
        return Integer.signum(this.end - prop.getEnd());
    }

    public int getEnd() {
        return this.end;
    }

    public int getStart() {
        return this.start;
    }
}
