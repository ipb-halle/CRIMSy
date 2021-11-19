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
package de.ipb_halle.lbac.material.sequence.search.bean;

import java.util.Comparator;

import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;

/**
 * Options for sorting alignment results. 
 * 
 * @author flange
 */
public enum SortItem {
    SUBJECTNAME(Comparator.comparing(f -> f.getFastaResult().getSubjectSequenceName())),
    LENGTH(Comparator.comparing(f -> f.getFastaResult().getSubjectSequenceLength(), Comparator.reverseOrder())),
    BITSCORE(Comparator.comparing(f -> f.getFastaResult().getBitScore(), Comparator.reverseOrder())),
    EVALUE(Comparator.comparing(f -> f.getFastaResult().getExpectationValue())),
    SMITHWATERMANSCORE(Comparator.comparing(f -> f.getFastaResult().getSmithWatermanScore(), Comparator.reverseOrder())),
    IDENTITY(Comparator.comparing(f -> f.getFastaResult().getIdentity(), Comparator.reverseOrder())),
    SIMILARITY(Comparator.comparing(f -> f.getFastaResult().getSimilarity(), Comparator.reverseOrder()));

    private final Comparator<FastaResultDisplayWrapper> comparator;

    private SortItem(Comparator<FastaResultDisplayWrapper> comparator) {
        this.comparator = comparator;
    }

    public Comparator<FastaResultDisplayWrapper> getComparator() {
        return comparator;
    }
}