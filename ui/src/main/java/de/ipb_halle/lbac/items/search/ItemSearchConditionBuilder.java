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

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author fmauz
 */
public class ItemSearchConditionBuilder extends SearchConditionBuilder {

    private final static String itemLabelPattern = "[0-9]{8,12}";

    @Deprecated
    public ItemSearchConditionBuilder(User u, int firstResultIndex, int maxResults) {
        super(u, firstResultIndex, maxResults);
        target = SearchTarget.ITEM;

    }

    @Deprecated
    public ItemSearchConditionBuilder addLabel(String label) {
        return this;
    }

    @Deprecated
    public ItemSearchConditionBuilder addIndexName(String name) {
        return this;
    }

    @Deprecated
    public ItemSearchConditionBuilder addLocation(String location) {
        addCondition(Operator.ILIKE,
                "%" + location + "%",
                AttributeType.CONTAINER,
                AttributeType.LABEL);
        return this;
    }

    @Deprecated
    public ItemSearchConditionBuilder addProject(String projectName) {
        return this;
    }

    @Deprecated
    public ItemSearchConditionBuilder addUserName(String userName) {
        return this;
    }

    @Deprecated
    public ItemSearchConditionBuilder addDescription(String description) {
        return this;
    }

    @Deprecated
    public ItemSearchConditionBuilder addSubMolecule(String molecule) {
        return this;
    }

    private void addDeactivatedCondition(List<Condition> conditionList, Set<String> values) {
        Boolean deactivated = Boolean.FALSE;
        for (String val : values) {
            deactivated |= (val.compareToIgnoreCase("deactivated") == 0);
        }
        Condition con = getBinaryLeafCondition(
                Operator.EQUAL,
                deactivated,
                AttributeType.ITEM,
                AttributeType.DIRECT,
                AttributeType.DEACTIVATED);
        conditionList.add(con);
    }

    private void addLabelCondition(List<Condition> conditionList, Set<String> values) {
        Set<String> idSet = new HashSet<>();
        for (String value : values) {
            if (value.matches(itemLabelPattern)) {
                idSet.add(value);
            }
        }
        if (idSet.size() > 0) {
            conditionList.add(getBinaryLeafCondition(
                    Operator.IN,
                    idSet,
                    AttributeType.ITEM,
                    AttributeType.LABEL));
        }
    }

    private void addLocationCondition(List<Condition> conditionList, Set<String> values) {
        List<Condition> subList = new ArrayList<>();
        for (String value : values) {
            subList.add(getBinaryLeafCondition(
                    Operator.ILIKE,
                    "%" + value + "%",
                    AttributeType.CONTAINER,
                    AttributeType.LABEL));
        }
        conditionList.add(getDisjunction(subList));
    }

    private void addOwnerCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple owners currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                AttributeType.ITEM,
                AttributeType.DIRECT,
                AttributeType.OWNER,
                AttributeType.MEMBER_NAME);
        conditionList.add(con);
    }

    private void addProjectCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple projects currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                AttributeType.ITEM,
                AttributeType.PROJECT,
                AttributeType.PROJECT_NAME);
        conditionList.add(con);
    }

    /**
     * Create an ORed condition for index values
     *
     * @param conditionList
     * @param values
     * @param isName limit search to names (true), indices (false) or not at all
     * (null)
     */
    private void addTextCondition(List<Condition> conditionList, Set<String> values) {
        conditionList.add(getTextCondition(values));
    }

    public Condition convertRequestToCondition(SearchRequest request, ACPermission... acPermission) {

        List<Condition> itemConditionList = getItemCondition(request);

        MaterialSearchConditionBuilder matBuilder = new MaterialSearchConditionBuilder();
        List<Condition> subList = matBuilder.getMaterialCondition(request);

        if (!subList.isEmpty()) {
            Condition matCondition = addACL(
                    subList,
                    request,
                    AttributeType.MATERIAL,
                    ACPermission.permREAD);
            itemConditionList.add(matCondition);
        }

        return addACL(itemConditionList,
                request,
                AttributeType.ITEM,
                acPermission);
    }

    public List<Condition> getItemCondition(SearchRequest request) {
        List<Condition> conditionList = new ArrayList<>();
        for (Map.Entry<SearchCategory, Set<String>> entry : request.getSearchValues().entrySet()) {
            switch (entry.getKey()) {
                case DEACTIVATED:
                    addDeactivatedCondition(conditionList, entry.getValue());
                    break;
                case LABEL:
                    addLabelCondition(conditionList, entry.getValue());
                    break;
                case LOCATION:
                    addLocationCondition(conditionList, entry.getValue());
                    break;
                case PROJECT:
                    addProjectCondition(conditionList, entry.getValue());
                    break;
                case TEXT:
                    addTextCondition(conditionList, entry.getValue());
                    break;
                case USER:
                    addOwnerCondition(conditionList, entry.getValue());
                    break;
            }
        }
        return conditionList;
    }

    private Condition getTextCondition(Set<String> values) {
        ArrayList<Condition> subConditionList = new ArrayList<>();
        for (String value : values) {
            subConditionList.add(getBinaryLeafCondition(
                    Operator.ILIKE,
                    "%" + value + "%",
                    AttributeType.MATERIAL,
                    AttributeType.TEXT));
        }
        if (subConditionList.size() > 1) {
            return new Condition(
                    Operator.OR,
                    subConditionList.toArray(new Condition[0])
            );
        }
        if (subConditionList.size() > 0) {
            return subConditionList.get(0);
        }
        throw new IllegalArgumentException("Could not create Condition");
    }
}
