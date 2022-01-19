/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.display;

/**
 * Configuration for the {@link FastaResultDisplayWrapper}.
 * 
 * @author flange
 */
public class ResultDisplayConfig {
    /*
     * maximum length of the nucleotide or amino acid residue alignment lines
     */
    private int lineLength = 50;

    /*
     * minimum number of space characters between the sequence start position number
     * and the start of the alignment line
     */
    private int prefixSpaces = 2;

    /*
     * minimum number of space characters between the end of the alignment line and
     * the sequence end position number
     */
    private int suffixSpaces = 2;

    /*
     * index count of a character in the query alignment
     */
    private int queryLineIndexMultiplier = 1;

    /*
     * whether the query alignment should reverse its index when the frame is
     * reversed
     */
    private boolean queryAlignmentCanReverse = false;

    /*
     * index count of a character in the subject alignment
     */
    private int subjectLineIndexMultiplier = 1;

    /*
     * whether the subject alignment should reverse its index when the frame is
     * reversed
     */
    private boolean subjectAlignmentCanReverse = false;

    public int getLineLength() {
        return lineLength;
    }

    public void setLineLength(int lineLength) {
        if (lineLength <= 0) {
            throw new IllegalArgumentException("Line length must be greater than 0.");
        }
        this.lineLength = lineLength;
    }

    public int getPrefixSpaces() {
        return prefixSpaces;
    }

    public void setPrefixSpaces(int prefixSpaces) {
        if (prefixSpaces <= 0) {
            throw new IllegalArgumentException("Number of prefix spaces must be greater than 0.");
        }
        this.prefixSpaces = prefixSpaces;
    }

    public int getSuffixSpaces() {
        return suffixSpaces;
    }

    public void setSuffixSpaces(int suffixSpaces) {
        if (suffixSpaces <= 0) {
            throw new IllegalArgumentException("Number of suffix spaces must be greater than 0.");
        }
        this.suffixSpaces = suffixSpaces;
    }

    public int getQueryLineIndexMultiplier() {
        return queryLineIndexMultiplier;
    }

    protected void setQueryLineIndexMultiplier(int queryLineIndexMultiplier) {
        if (queryLineIndexMultiplier <= 0) {
            throw new IllegalArgumentException("Query line index multiplier must be greater than 0.");
        }
        this.queryLineIndexMultiplier = queryLineIndexMultiplier;
    }

    public boolean isQueryAlignmentCanReverse() {
        return queryAlignmentCanReverse;
    }

    protected void setQueryAlignmentCanReverse(boolean queryAlignmentCanReverse) {
        this.queryAlignmentCanReverse = queryAlignmentCanReverse;
    }

    public int getSubjectLineIndexMultiplier() {
        return subjectLineIndexMultiplier;
    }

    protected void setSubjectLineIndexMultiplier(int subjectLineIndexMultiplier) {
        if (subjectLineIndexMultiplier <= 0) {
            throw new IllegalArgumentException("Subject line index multiplier must be greater than 0.");
        }
        this.subjectLineIndexMultiplier = subjectLineIndexMultiplier;
    }

    public boolean isSubjectAlignmentCanReverse() {
        return subjectAlignmentCanReverse;
    }

    protected void setSubjectAlignmentCanReverse(boolean subjectAlignmentCanReverse) {
        this.subjectAlignmentCanReverse = subjectAlignmentCanReverse;
    }
}