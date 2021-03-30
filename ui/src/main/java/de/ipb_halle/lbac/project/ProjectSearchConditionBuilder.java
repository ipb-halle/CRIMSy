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
package de.ipb_halle.lbac.project;

import de.ipb_halle.lbac.webclient.XmlSetWrapper;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class ProjectSearchConditionBuilder extends SearchConditionBuilder {

    public ProjectSearchConditionBuilder(EntityGraph graph, String wordRoot) {
        super(graph, wordRoot);
    }

    public List<Condition> getProjectCondition(SearchRequest request, boolean toplevel) {
        List<Condition> conditionList = new ArrayList<>();

        for (SearchCategory key : request.getSearchValues().keySet()) {
            switch (key) {
                case DEACTIVATED:
                    addDeactivatedCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case PROJECT:
                    addProjectNameCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case USER:
                    addOwnerCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
            }
        }
        return conditionList;
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... acPermission) {
        List<Condition> conditionList = getProjectCondition(request, true);
        return addACL(conditionList, rootGraphName, request.getUser(), acPermission);
    }

    private void addProjectNameCondition(List<Condition> conditionList, Set<String> values) {
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                rootGraphName, AttributeType.PROJECT_NAME);
        conditionList.add(con);
    }

    private void addOwnerCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple owners currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                rootGraphName + "/USERSGROUPS",
                AttributeType.MEMBER_NAME);
        conditionList.add(con);
    }

    private void addDeactivatedCondition(List<Condition> conditionList, Set<String> values) {
        boolean deactivated = Boolean.valueOf(values.iterator().next());
        Condition con = getBinaryLeafCondition(
                Operator.EQUAL,
                deactivated,
                rootGraphName,
                AttributeType.DEACTIVATED);
        conditionList.add(con);
    }

}
