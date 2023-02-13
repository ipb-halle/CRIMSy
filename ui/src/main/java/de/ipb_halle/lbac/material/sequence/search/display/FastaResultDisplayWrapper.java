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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.Frame;
import de.ipb_halle.lbac.material.sequence.Sequence;

/**
 * Wrapper for {@link Sequence} and {@link FastaResult} objects that adds support to display their
 * alignments to humans.
 * 
 * @author flange
 */
public class FastaResultDisplayWrapper {
    private final FastaResult result;
    private final Sequence sequence;
    private ResultDisplayConfig config = new ResultDisplayConfig();

    public FastaResultDisplayWrapper(Sequence sequence, FastaResult result) {
        this.sequence = sequence;
        this.result = result;
    }

    /**
     * @return the wrapped {@link Sequence} object
     */
    public Sequence getSequence() {
        return sequence;
    }

    /**
     * @return the wrapped {@link FastaResult} object
     */
    public FastaResult getFastaResult() {
        return result;
    }

    /**
     * @return the configuration for the alignment display
     */
    public ResultDisplayConfig getConfig() {
        return config;
    }

    /**
     * Sets the configuration for the alignment display.
     * 
     * @param config
     * @return this object
     */
    public FastaResultDisplayWrapper config(ResultDisplayConfig config) {
        this.config = config;
        return this;
    }

    static class AlignmentLine {
        private final String line;
        private final int startIndex;
        private final int stopIndex;

        public AlignmentLine(String line, int startIndex, int stopIndex) {
            this.line = line;
            this.startIndex = startIndex;
            this.stopIndex = stopIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getStopIndex() {
            return stopIndex;
        }

        public String getLine() {
            return line;
        }
    }

    /**
     * @return the alignment lines according to the configuration
     */
    public String getAlignments() {
        String queryAlignmentLine = replaceLeadingChars(result.getQueryAlignmentLine(), '-', ' ');
        String subjectAlignmentLine = replaceLeadingChars(result.getSubjectAlignmentLine(), '-', ' ');

        boolean isQueryLineReversed = isReversed(result.getFrame(), config.isQueryAlignmentCanReverse());
        boolean isSubjectLineReversed = isReversed(result.getFrame(), config.isSubjectAlignmentCanReverse());

        List<AlignmentLine> queryLines = createAlignmentLines(queryAlignmentLine,
                result.getQueryAlignmentDisplayStart(), config.getLineLength(), config.getQueryLineIndexMultiplier(),
                isQueryLineReversed);
        List<AlignmentLine> subjectLines = createAlignmentLines(subjectAlignmentLine,
                result.getSubjectAlignmentDisplayStart(), config.getLineLength(),
                config.getSubjectLineIndexMultiplier(), isSubjectLineReversed);
        List<String> consenusLines = segmentString(result.getConsensusLine(), config.getLineLength());

        int maxPrefixLength = Integer.toString(maxIndex(l -> l.getStartIndex(), queryLines, subjectLines)).length();
        int maxSuffixLength = Integer.toString(maxIndex(l -> l.getStopIndex(), queryLines, subjectLines)).length();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.max(queryLines.size(), subjectLines.size()); i++) {
            if (i != 0) {
                sb.append("\n\n");
            }

            // query alignment line
            sb.append("Query");
            if (i < queryLines.size()) {
                AlignmentLine line = queryLines.get(i);

                if (! line.getLine().isBlank()) {
                    // leading spaces
                    sb.append(StringUtils.leftPad("", "Subject".length() - "Query".length() + 1));

                    // start index of this line
                    sb.append(StringUtils.leftPad(Integer.toString(line.getStartIndex()), maxPrefixLength));

                    // fill up with spaces till sequence start
                    sb.append(StringUtils.leftPad("", config.getPrefixSpaces()));

                    // sequence filled up with spaces
                    int lengthOfSubjectLine = (i < subjectLines.size()) ? subjectLines.get(i).getLine().length() : 0;
                    sb.append(StringUtils.rightPad(line.getLine(),
                            Math.max(line.getLine().length(), lengthOfSubjectLine) + config.getSuffixSpaces()));

                    // stop index of this line
                    sb.append(StringUtils.leftPad(Integer.toString(line.getStopIndex()), maxSuffixLength));
                }
            }
            sb.append("\n");

            // consensus line
            if (i < consenusLines.size()) {
                String line = consenusLines.get(i);
                if (! line.isBlank()) {
                    // leading spaces
                    sb.append(StringUtils.leftPad("",
                            "Subject".length() + 1 + maxPrefixLength + config.getPrefixSpaces()));

                    // consensus
                    sb.append(line);
                }
            }
            sb.append("\n");

            // subject alignment line
            sb.append("Subject");
            if (i < subjectLines.size()) {
                AlignmentLine line = subjectLines.get(i);

                if (! line.getLine().isBlank()) {
                    // leading space
                    sb.append(" ");

                    // start index of this line
                    sb.append(StringUtils.leftPad(Integer.toString(line.getStartIndex()), maxPrefixLength));

                    // fill up with spaces till sequence start
                    sb.append(StringUtils.leftPad("", config.getPrefixSpaces()));

                    // sequence filled up with spaces
                    int lengthOfQueryLine = (i < queryLines.size()) ? queryLines.get(i).getLine().length() : 0;
                    sb.append(StringUtils.rightPad(line.getLine(),
                            Math.max(line.getLine().length(), lengthOfQueryLine) + config.getSuffixSpaces()));

                    // stop index of this line
                    sb.append(StringUtils.leftPad(Integer.toString(line.getStopIndex()), maxSuffixLength));
                }
            }
        }

        return sb.toString();
    }

    static String replaceLeadingChars(String input, char oldChar, char newChar) {
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == oldChar) {
                chars[i] = newChar;
            } else {
                break;
            }
        }

        return new String(chars);
    }

    private boolean isReversed(Frame frame, boolean canReverse) {
        return (Frame.REVERSE.equals(frame) && canReverse);
    }

    static List<String> segmentString(String input, int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length cannot be less than 1");
        }
        List<String> results = new ArrayList<>();

        for (int i = 0; i < input.length(); i += length) {
            // StringUtils.substring is a bit more greedy than String.subString
            String segment = StringUtils.substring(input, i, i + length);
            results.add(segment);
        }

        return results;
    }

    static List<AlignmentLine> createAlignmentLines(String sequence, int start, int lineLength, int indexMultiplier,
            boolean reverse) {
        List<AlignmentLine> results = new ArrayList<>();
        int startIndex = start;
        int stopIndex;
        int signMultiplier = (reverse ? -1 : 1);

        for (String segment : segmentString(sequence, lineLength)) {
            stopIndex = startIndex - signMultiplier
                    + (segment.length() - StringUtils.countMatches(segment, ' ')
                            - StringUtils.countMatches(segment, '-') - StringUtils.countMatches(segment, '/')
                            - StringUtils.countMatches(segment, '\\')) * indexMultiplier * signMultiplier;

            /*
             * An explanation for the following calculation is given in the test method
             * test_getAlignmentsDNAQueryProteinDatabaseWithFrameShifts() in
             * FastaResultDisplayWrapperTest.
             */
            int frameShifts = StringUtils.countMatches(segment, '/') - StringUtils.countMatches(segment, '\\');
            stopIndex = stopIndex - frameShifts;

            results.add(new AlignmentLine(segment, startIndex, stopIndex));
            startIndex = stopIndex + signMultiplier;
        }

        return results;
    }

    @SafeVarargs
    private static int maxIndex(Function<AlignmentLine, Integer> function, List<AlignmentLine>... lists) {
        int max = Integer.MIN_VALUE;

        for (List<AlignmentLine> list : lists) {
            for (AlignmentLine line : list) {
                int value = function.apply(line);
                if (value > max) {
                    max = value;
                }
            }
        }

        return max;
    }
}
