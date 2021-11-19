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

import static de.ipb_halle.lbac.material.sequence.SequenceType.DNA;
import static de.ipb_halle.lbac.material.sequence.SequenceType.PROTEIN;

import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayConfig;
import de.ipb_halle.lbac.material.sequence.search.display.ResultDisplayConfig;
import de.ipb_halle.lbac.material.sequence.search.display.FastxyResultDisplayConfig;
import de.ipb_halle.lbac.material.sequence.search.display.TfastxyResultDisplayConfig;

/**
 * Modes for sequence search.
 * 
 * @author flange
 */
public enum SearchMode {
    PROTEIN_PROTEIN(PROTEIN, PROTEIN, new FastaResultDisplayConfig()),
    DNA_DNA(DNA, DNA, new FastaResultDisplayConfig()),
    DNA_PROTEIN(DNA, PROTEIN, new FastxyResultDisplayConfig()),
    PROTEIN_DNA(PROTEIN, DNA, new TfastxyResultDisplayConfig());

    private final SequenceType querySequenceType;
    private final SequenceType librarySequenceType;
    private final ResultDisplayConfig displayConfig;

    SearchMode(SequenceType querySequenceType, SequenceType librarySequenceType, ResultDisplayConfig displayConfig) {
        this.querySequenceType = querySequenceType;
        this.librarySequenceType = librarySequenceType;
        this.displayConfig = displayConfig;
    }

    public SequenceType getQuerySequenceType() {
        return querySequenceType;
    }

    public SequenceType getLibrarySequenceType() {
        return librarySequenceType;
    }

    public ResultDisplayConfig getDisplayConfig() {
        return displayConfig;
    }
}