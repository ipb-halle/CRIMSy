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
package de.ipb_halle.lbac.material.sequence.search;

import de.ipb_halle.lbac.material.sequence.SequenceType;

/**
 * Data object that holds information about a sequence search request.
 *
 * @author fmauz
 */
public class SequenceSearchInformation {
    public SequenceType sequenceQueryType;
    public SequenceType sequenceLibraryType;
    public String sequenceQuery;
    public int translationTable;

    public SequenceSearchInformation(SequenceType sequenceQueryType, SequenceType sequenceLibraryType,
            String sequenceQuery, int translationTable) {
        this.sequenceQueryType = sequenceQueryType;
        this.sequenceLibraryType = sequenceLibraryType;
        this.sequenceQuery = sequenceQuery;
        this.translationTable = translationTable;
    }
}
