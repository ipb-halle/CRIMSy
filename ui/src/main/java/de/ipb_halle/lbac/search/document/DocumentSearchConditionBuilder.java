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
package de.ipb_halle.lbac.search.document;

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class DocumentSearchConditionBuilder extends SearchConditionBuilder {

    public DocumentSearchConditionBuilder(User u, int firstResultIndex, int maxResults) {
        super(u, firstResultIndex, maxResults);
        this.target = SearchTarget.DOCUMENT;
    }

    public DocumentSearchConditionBuilder addCollectionID(Integer id) {
        addCondition(Operator.EQUAL,
                id,
                AttributeType.COLLECTION);
        return this;
    }

    public DocumentSearchConditionBuilder addWordRoots(Set<String> wordRoots) {
        addCondition(Operator.IN,
                wordRoots,
                AttributeType.WORDROOT);
        return this;
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission ...acPermission) {
        return null;
    }

}
