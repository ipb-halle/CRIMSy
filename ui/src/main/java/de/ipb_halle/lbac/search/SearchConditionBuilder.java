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

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import de.ipb_halle.lbac.search.lang.Value;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public abstract class SearchConditionBuilder {

    protected List<Condition> leafConditions = new ArrayList<>();
    protected int firstResultIndex;
    protected int maxResults;
    protected User user;
    protected SearchTarget target;
    protected EntityGraph entityGraph;
    protected String rootGraphName;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    protected SearchConditionBuilder(EntityGraph entityGraph, String rootGraphName) {
        this.entityGraph = entityGraph;
        this.rootGraphName = rootGraphName;
    }

    /**
     * Create an access control condition and combine it with other conditions.
     *
     * @param conditionList
     * @param request
     * @param acPermission
     * @return the combined condition
     */
    protected Condition addACL(List<Condition> conditionList,
            String graphPath,
            User user,
            ACPermission... acPermission) {
        List<Condition> subCondition = new ArrayList<>();
        for (ACPermission perm : acPermission) {
            subCondition.add(getACLCondition(
                    user,
                    perm,
                    graphPath));
        }

        Condition aclCondition = getDisjunction(subCondition);

        if ((conditionList == null) || (conditionList.isEmpty())) {
            return aclCondition;
        }
        conditionList.add(aclCondition);
        return new Condition(
                Operator.AND,
                conditionList.toArray(new Condition[0]));
    }

    protected void addSubGraphACL(EntityGraph subGraph, String graphPath, User user) {
        subGraph.setSubSelectCondition(
                getACLCondition(user,
                        ACPermission.permREAD,
                        graphPath));
        subGraph.setSubSelectAttribute(AttributeType.DIRECT);
    }

    public abstract Condition convertRequestToCondition(SearchRequest request, ACPermission... perm);

    /**
     * @param user the user for whom the access control condition is to be built
     * @param permission the permission to check
     * @param acObjAttrtype one or more values of <code>AttributeType</code> to
     * select the entity to which the generated condition should be applied
     * @return a <code>Condition</code> object to be applied to an EntityGraph
     * which contains an entity sub graph created by the getEntityGraph() method
     * of this class. The built condition honours the two possibilities of
     * obtaining access: either by group membership in an allowed group or by
     * object ownership and a specific owner ACE:      <code>
     *       ((acObjAttrType:MEMBER = user AND acObjAttrType:ACE:MEMBER = OWNER_ACCOUNT) OR
     *       acObjAttrType:MEMBERSHIP:MEMBER = user)
     *     AND
     *       acObjAttrType:PERM_XXX IS TRUE
     * </code>
     */
    protected Condition getACLCondition(
            User user,
            ACPermission permission,
            String graphPath,
            AttributeType... acObjAttrType) {

        Condition ownerCondition = new Condition(
                Operator.AND,
                new Condition(
                        new Attribute(graphPath, new AttributeType[]{
                    AttributeType.OWNER,
                    AttributeType.DIRECT}),
                        Operator.EQUAL,
                        new Value(user.getId())),
                new Condition(
                        new Attribute(graphPath + "/ACENTRIES",
                                new AttributeType[]{
                                    AttributeType.DIRECT,
                                    AttributeType.MEMBER}),
                        Operator.EQUAL,
                        new Value(GlobalAdmissionContext.OWNER_ACCOUNT_ID))
        );

        Condition memberCondition = new Condition(
                Operator.OR,
                ownerCondition,
                new Condition(
                        new Attribute(graphPath + "/ACENTRIES/MEMBERSHIPS",
                                new AttributeType[]{
                                    AttributeType.DIRECT,
                                    AttributeType.MEMBER}),
                        Operator.EQUAL,
                        new Value(user.getId()))
        );

        return new Condition(
                Operator.AND,
                memberCondition,
                new Condition(getPermissionAttribute(graphPath + "/ACENTRIES", permission)
                        .addType(AttributeType.DIRECT),
                        Operator.IS_TRUE));
    }

    protected Condition getBinaryLeafCondition(Operator op,
            Object value, String path, AttributeType... attrTypes) {
        return new Condition(
                new Attribute(path, attrTypes),
                op,
                new Value(value));
    }

    protected Condition getBinaryLeafConditionWithCast(Operator op,
            Object value, String castExpression, String path, AttributeType... attrTypes) {
        Value valueWithCast = new Value(value)
                .setCastExpression(castExpression);
        return new Condition(
                new Attribute(path, attrTypes),
                op,
                valueWithCast);
    }

    protected Condition getDisjunction(List<Condition> subConditionList) {
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

    private Attribute getPermissionAttribute(String graphPath, ACPermission perm) {
        switch (perm) {
            case permREAD:
                return new Attribute(graphPath, AttributeType.PERM_READ);
            case permEDIT:
                return new Attribute(graphPath, AttributeType.PERM_EDIT);
            case permCHOWN:
                return new Attribute(graphPath, AttributeType.PERM_CHOWN);
            case permGRANT:
                return new Attribute(graphPath, AttributeType.PERM_GRANT);
            case permSUPER:
                return new Attribute(graphPath, AttributeType.PERM_SUPER);
            case permCREATE:
                return new Attribute(graphPath, AttributeType.PERM_CREATE);
            case permDELETE:
                return new Attribute(graphPath, AttributeType.PERM_DELETE);
        }
        throw new IllegalArgumentException("illegal argument");
    }
}
