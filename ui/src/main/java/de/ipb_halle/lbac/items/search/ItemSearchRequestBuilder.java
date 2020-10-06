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
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchRequestImpl;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.ConditionBuilder;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class ItemSearchRequestBuilder {

    private List<Condition> leafConditions = new ArrayList<>();
    private int firstResultIndex;
    private int maxResults;
    private User user;
    private ItemEntityGraphBuilder entityGraphBuilder;
    private ConditionBuilder conditionBuilder;

    public ItemSearchRequestBuilder(User u, int firstResultIndex, int maxResults) {
        this.user = u;
        this.firstResultIndex = firstResultIndex;
        this.maxResults = maxResults;
        entityGraphBuilder = new ItemEntityGraphBuilder();
        conditionBuilder = new ConditionBuilder(leafConditions);
    }

    public SearchRequest buildSearchRequest() {
        SearchRequestImpl searchRequest = new SearchRequestImpl(
                entityGraphBuilder.buildEntityGraph(),
                conditionBuilder.buildConditionTree()
        );

        return searchRequest;
    }

    public ItemSearchRequestBuilder addID(Integer id) {
        addCondition(Operator.EQUAL,
                id,
                AttributeType.ITEM,
                AttributeType.LABEL);
        return this;
    }

    public ItemSearchRequestBuilder addIndexName(String name) {
        entityGraphBuilder.addMaterialName();
        addCondition(Operator.ILIKE,
                name,
                AttributeType.MATERIAL,
                AttributeType.TEXT);
        return this;
    }

    public ItemSearchRequestBuilder addLocation(String location) {
        entityGraphBuilder.addContainer();
        addCondition(Operator.ILIKE,
                location,
                AttributeType.CONTAINER,
                AttributeType.LABEL);
        return this;
    }

    public ItemSearchRequestBuilder addProject(String projectName) {
        entityGraphBuilder.addProject();
        addCondition(Operator.ILIKE,
                projectName,
                AttributeType.PROJECT_NAME);
        return this;
    }

    public ItemSearchRequestBuilder addUserName(String userName) {
        entityGraphBuilder.addUser();
        return this;
    }

    private void addCondition(Operator op, Object value, AttributeType... types) {
        leafConditions.add(new Condition(
                new Attribute(types),
                op,
                new de.ipb_halle.lbac.search.lang.Value(value)));
    }
}
