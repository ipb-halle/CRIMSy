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
package de.ipb_halle.lbac.items.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Operator;

/**
 *
 * @author fmauz
 */
public class ItemSearchConditionBuilder extends SearchConditionBuilder {

    public ItemSearchConditionBuilder(User u, int firstResultIndex, int maxResults) {
        super(u, firstResultIndex, maxResults);
        target = SearchTarget.ITEM;

    }

    public ItemSearchConditionBuilder addLabel(String label) {
        addCondition(Operator.EQUAL,
                label,
                AttributeType.ITEM,
                AttributeType.LABEL);
        return this;
    }

    public ItemSearchConditionBuilder addIndexName(String name) {
        addCondition(Operator.ILIKE,
                "%" + name + "%",
                AttributeType.MATERIAL,
                AttributeType.TEXT);
        return this;
    }

    public ItemSearchConditionBuilder addLocation(String location) {
        addCondition(Operator.ILIKE,
                "%" + location + "%",
                AttributeType.CONTAINER,
                AttributeType.LABEL);
        return this;
    }

    public ItemSearchConditionBuilder addProject(String projectName) {
        addCondition(Operator.ILIKE,
                "%" + projectName + "%",
                AttributeType.PROJECT_NAME);
        return this;
    }

    public ItemSearchConditionBuilder addUserName(String userName) {
        addCondition(Operator.ILIKE,
                "%" + userName + "%",
                AttributeType.MEMBER_NAME);
        return this;
    }

    public ItemSearchConditionBuilder addDescription(String description) {
        addCondition(Operator.ILIKE,
                "%" + description + "%",
                AttributeType.TEXT);
        return this;
    }

    public ItemSearchConditionBuilder addSubMolecule(String molecule) {
        addConditionWithCast(Operator.SUBSTRUCTURE,
                molecule,
                " CAST(%s AS MOLECULE) ",
                AttributeType.MOLECULE);
        return this;
    }

}
