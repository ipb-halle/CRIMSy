/*
 * Cloud Resource & Information Management System (CRIMSy)
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
package de.ipb_halle.lbac.material.structure;

import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Table(name = "molecules")
public class MoleculeEntity {

    @Id
    private Integer id;

       @Column
    private String format;

    @Column
    @AttributeTag(type = AttributeType.MOLECULE)
    private String molecule;


    public String getFormat() {
        return format;
    }

    public Integer getId() {
        return id;
    }

    public String getMolecule() {
        return molecule;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }
}
