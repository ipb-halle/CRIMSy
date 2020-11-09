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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author fmauz
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteMaterial implements Searchable {

    private int id;
    private List<String> names = new ArrayList<>();
    private Type type;
    private String moleculeString;
    private String sumFormula;
    private Map<Integer, String> indices = new HashMap<>();

    public void setId(int id) {
        this.id = id;
    }

    public void addName(String name) {
        names.add(name);
    }

    public void setMoleculeString(String moleculeString) {
        this.moleculeString = moleculeString;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Material)) {
            return false;
        } else {
            return ((Material) other).getId() == getId();
        }
    }

    @Override
    public String getNameToDisplay() {
        if (!names.isEmpty()) {
            return names.get(0);
        }
        return "";
    }

    @Override
    public Type getTypeToDisplay() {
        return type;
    }

    public Map<Integer, String> getIndices() {
        return indices;
    }

    public void setSumFormula(String sumFormula) {
        this.sumFormula = sumFormula;
    }

    public String getSumFormula() {
        return sumFormula;
    }

    public int getId() {
        return id;
    }

    public String getMoleculeString() {
        return moleculeString;
    }

    public List<String> getNames() {
        return names;
    }

    public Type getType() {
        return type;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public void setIndices(Map<Integer, String> indices) {
        this.indices = indices;
    }

}
