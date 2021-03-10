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
package de.ipb_halle.lbac.search.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author fmauz
 */
@XmlRootElement
public class Attribute {

    private Set<AttributeType> types;
    private String graphPath;

    public Attribute(Collection<AttributeType> types) {
        this.types = new HashSet<>();
        this.types.addAll(types);
    }

    public Attribute(AttributeType[] types) {
        this();
        this.types.addAll(Arrays.asList(types));
    }

    public Attribute(String path, AttributeType[] types) {
        this();
        this.types.addAll(Arrays.asList(types));
        this.graphPath = path;
    }
    
    public Attribute(AttributeType type) {
        this();
        this.types.add(type);
    }

    public Attribute(String path, AttributeType type) {
        this();
        this.types.add(type);
        this.graphPath = path;
    }

    
    private Attribute() {
        this.types = new HashSet<>();
        this.graphPath = "";
    }

    public Attribute addParentPath(String path) {
        this.graphPath = String.join("/", path, this.graphPath);
        return this;
    }

    public Attribute addType(AttributeType type) {
        this.types.add(type);
        return this;
    }

    public Attribute addTypes(AttributeType[] types) {
        this.types.addAll(Arrays.asList(types));
        return this;
    }

    public Attribute addTypes(Collection<AttributeType> types) {
        this.types.addAll(types);
        return this;
    }

    public Set<AttributeType> getTypes() {
        return this.types;
    }

    public String getGraphPath() {
        return this.graphPath;
    }

    public Attribute setGraphPath(String path) {
        this.graphPath = path;
        return this;
    }

    public void setTypes(Set<AttributeType> types) {
        this.types = types;
    }
}
