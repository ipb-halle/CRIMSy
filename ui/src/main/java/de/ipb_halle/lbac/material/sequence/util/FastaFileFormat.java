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
package de.ipb_halle.lbac.material.sequence.util;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;

/**
 * Utility class to generate fasta files.
 * 
 * @author flange
 */
public class FastaFileFormat {
    private FastaFileFormat() {
    }

    /**
     * Generate a fasta file.
     * 
     * @param description
     * @param sequence
     * @return string in fasta file format
     * @throws IllegalArgumentException in case {@code description} or
     *                                  {@code sequence} are {@code null} or empty
     */
    public static String generateFastaString(String description, String sequence) {
        if ((description == null) || description.isEmpty() || (sequence == null) || sequence.isEmpty()) {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder(description.length() + 3 + sequence.length());
        sb.append(">").append(description).append("\n").append(sequence);
        return sb.toString();
    }

    /**
     * Generate a fasta file from the given {@link Sequence} instance.
     * 
     * @param sequence
     * @return string in fasta file format or and empty string if there is no sequence
     *         string available
     */
    public static String generateFastaString(Sequence sequence) {
        if (sequence == null) {
            return "";
        }

        StringBuilder description = new StringBuilder();
        description.append(sequence.getId());

        List<MaterialName> names = sequence.getNames();
        if ((names != null) && !names.isEmpty()) {
            description.append(" ").append(sequence.getNames().get(0).getValue());
        }

        SequenceData data = sequence.getSequenceData();
        if ((data == null) || (data.getSequenceLength() == null) || (data.getSequenceLength() == 0)) {
            return "";
        }

        return generateFastaString(description.toString(), data.getSequenceString());
    }

    /**
     * Generate a fasta file from the given {@link Sequence} instances.
     * 
     * @param sequences
     * @return string in fasta file format
     */
    public static String generateFastaString(Collection<Sequence> sequences) {
        if (sequences == null) {
            return "";
        }

        StringJoiner result = new StringJoiner("\n");
        for (Sequence sequence : sequences) {
            if (sequence != null) {
                result.add(generateFastaString(sequence));
            }
        }

        return result.toString();
    }
}
