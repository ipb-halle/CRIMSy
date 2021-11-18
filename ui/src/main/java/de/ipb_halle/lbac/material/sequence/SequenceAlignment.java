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
package de.ipb_halle.lbac.material.sequence;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class SequenceAlignment implements Searchable {

    private final Sequence foundSequence;
    private final FastaResult alignmentInformation;

    public SequenceAlignment(Sequence foundSequence, FastaResult alignmentInformation) {
        this.foundSequence = foundSequence;
        this.alignmentInformation = alignmentInformation;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof SequenceAlignment)) {
            return false;
        }
        SequenceAlignment otherAlignment = (SequenceAlignment) other;
        return Objects.equals(otherAlignment.getAlignmentInformation(), getAlignmentInformation())
                && foundSequence.getId() == otherAlignment.getFoundSequence().getId();
    }

    @Override
    public String getNameToDisplay() {
        return foundSequence.getFirstName();
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.SEQUENCE);
    }

    public Sequence getFoundSequence() {
        return foundSequence;
    }

    public FastaResult getAlignmentInformation() {
        return alignmentInformation;
    }

}
