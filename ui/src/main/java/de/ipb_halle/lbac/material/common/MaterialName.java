/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.common;

import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class MaterialName extends IndexEntry implements Comparable<MaterialName> {

    protected Integer rank;

    public MaterialName(String name, String language, int rank) {
        super(1, name, language);
        this.value = name;
        this.language = language;
        this.rank = rank;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.value);
        hash = 73 * hash + Objects.hashCode(this.language);
        hash = 73 * hash + Objects.hashCode(rank);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MaterialName)) {
            return false;
        }
        MaterialName mn = (MaterialName) obj;
        return mn.getValue().equals(value)
                && mn.getLanguage().equals(language)
                && mn.rank == rank;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public int compareTo(MaterialName o) {
        if (o.rank < rank) {
            return -1;
        } else if (o.rank == rank) {
            return 0;
        } else {
            return 1;
        }
    }

}
