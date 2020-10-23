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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.search.bean.Type;

/**
 *
 * @author fmauz
 */
public class NetObjectImpl implements NetObject {

    private Searchable searchable;
    private Node node;

    public NetObjectImpl(Searchable searchable, Node node) {
        this.searchable = searchable;
        this.node = node;
    }

    @Override
    public Searchable getSearchable() {
        return searchable;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public String getNameToDisplay() {
        return searchable.getNameToDisplay();
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof NetObject)) {
            return false;
        }
        NetObject otherNo = (NetObject) other;
        return getNode().getId().equals(otherNo.getNode().getId())
                && searchable.isEqualTo(otherNo.getSearchable());
    }

    @Override
    public Type getTypeToDisplay() {
        return searchable.getTypeToDisplay();
    }

}
