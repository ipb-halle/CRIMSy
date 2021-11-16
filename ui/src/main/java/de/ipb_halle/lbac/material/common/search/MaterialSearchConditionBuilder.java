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
package de.ipb_halle.lbac.material.common.search;

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.items.service.ItemEntityGraphBuilder;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.entity.index.IndexTypeEntity;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialSearchConditionBuilder extends SearchConditionBuilder {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialSearchConditionBuilder(EntityGraph entityGraph, String rootName) {
        super(entityGraph, rootName);
    }

    protected void addDeactivatedCondition(String pathPrefix, List<Condition> conditionList, Set<String> values) {
        Boolean deactivated = Boolean.FALSE;
        for (String val : values) {
            deactivated |= (val.compareToIgnoreCase("deactivated") == 0);
        }
        Condition con = getBinaryLeafCondition(
                Operator.EQUAL,
                deactivated,
                rootGraphName + pathPrefix,
                AttributeType.DEACTIVATED);
        conditionList.add(con);
    }

    /**
     * Create an ORed condition for index values
     *
     * @param conditionList
     * @param values
     * @param isName limit search to names (true; SearchCategory=NAME), indices
     * (false; SearchCategory=INDEX) or not at all (SearchCategory=TEXT) (null)
     */
    protected Condition createIndexCondition(String pathPrefix, List<Condition> conditionList, Set<String> values, Boolean isName) {
        if (isName != null) {
            Condition typeCondition = getBinaryLeafCondition(
                    isName ? Operator.EQUAL : Operator.NOT_EQUAL,
                    IndexTypeEntity.INDEX_TYPE_NAME,
                    rootGraphName + pathPrefix + "/material_indices",
                    AttributeType.INDEX_TYPE);
            Condition con = new Condition(
                    Operator.AND,
                    getIndexCondition(pathPrefix, values, false),
                    typeCondition);

            return con;
        }
        Condition con = getIndexCondition(pathPrefix, values, true);
        return con;
    }

    /**
     * ToDo: distinguish between LABEL and ID AttributeType annotation
     *
     * @param conditionList
     * @param values
     */
    protected void addLabelCondition(List<Condition> conditionList, Set<String> values) {
        Set<Integer> idSet = new HashSet<>();
        for (String value : values) {
            try {
                int id = Integer.parseInt(value);
                if (id > 0) {
                    idSet.add(id);
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (idSet.size() > 0) {
            conditionList.add(getBinaryLeafCondition(Operator.IN,
                    idSet,
                    rootGraphName,
                    AttributeType.BARCODE));
        }
    }

    private void addMaterialTypeCondition(List<Condition> conditionList, Set<String> values) {
        Set<MaterialType> types = new HashSet<>();
        for (String val : values) {
            MaterialType t = MaterialType.fromString(val);
            if (t != null) {
                types.add(t);
            }
        }
        if (types.size() > 0) {
            conditionList.add(getBinaryLeafCondition(
                    Operator.IN,
                    getIdsFromMaterialTypes(types.toArray(new MaterialType[0])),
                    rootGraphName,
                    AttributeType.MATERIAL_TYPE
            ));
        }
    }

    protected void addOwnerCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple owners currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                rootGraphName + "/material_compositions/componentMaterials/USERSGROUPS",
                AttributeType.MEMBER_NAME);
        conditionList.add(con);
    }

    protected void addProjectCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple projects currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                rootGraphName + "/material_compositions/componentMaterials/projects",
                AttributeType.PROJECT_NAME);
        conditionList.add(con);
    }

    private void addStructureCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple structures currently not supported");
        }
        conditionList.add(getBinaryLeafCondition(
                Operator.SUBSTRUCTURE,
                values.iterator().next(),
                rootGraphName + "/material_compositions/componentMaterials/structures/molecules",
                AttributeType.MOLECULE));
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... perm) {
        List<Condition> conditionList = getMaterialCondition(request, true);

        return addACL(conditionList, rootGraphName, request.getUser(), perm);
    }

    protected AttributeType[] getIndexAttributes(boolean requireTopLevel) {
        if (requireTopLevel) {
            return new AttributeType[]{
                AttributeType.TOPLEVEL,
                AttributeType.TEXT
            };
        }
        return new AttributeType[]{
            AttributeType.TEXT};
    }

    protected Condition getIndexCondition(String pathPrefix, Set<String> values, boolean requireTopLevel) {
        ArrayList<Condition> subConditionList = new ArrayList<>();
        for (String value : values) {
            subConditionList.add(getBinaryLeafCondition(
                    Operator.ILIKE,
                    "%" + value + "%",
                    rootGraphName + pathPrefix + "/material_indices",
                    getIndexAttributes(requireTopLevel)));
        }
        return getDisjunction(subConditionList);
    }

    public List<Condition> getMaterialCondition(SearchRequest request, boolean toplevel) {
        List<Condition> conditionList = new ArrayList<>();
        for (SearchCategory key : request.getSearchValues().keySet()) {
            switch (key) {
                case DEACTIVATED:
                    addDeactivatedCondition("/material_compositions/componentMaterials", conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case INDEX:
                    conditionList.add(createIndexCondition("/material_compositions/componentMaterials", conditionList, request.getSearchValues().get(key).getValues(), Boolean.FALSE));
                    break;
                case LABEL:
                    if (toplevel) {
                        addLabelCondition(conditionList, request.getSearchValues().get(key).getValues());
                        // What is it good for? Need to add material_compositions?
                    }
                    break;
                case NAME:
                    Condition c2 = createIndexCondition("/material_compositions/componentMaterials", conditionList, request.getSearchValues().get(key).getValues(), Boolean.TRUE);
                    conditionList.add(c2);
                    break;
                case PROJECT:
                    if (toplevel) {
                        addProjectCondition(conditionList, request.getSearchValues().get(key).getValues());
                    }
                    break;
                case STRUCTURE:
                    addStructureCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case TEXT:
                    if (toplevel) {
                        conditionList.add(createIndexCondition("", conditionList, request.getSearchValues().get(key).getValues(), null));
                    }
                    break;
                case TYPE:
                    addMaterialTypeCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case USER:
                    if (toplevel) {
                        addOwnerCondition(conditionList, request.getSearchValues().get(key).getValues());
                    }
                    break;
            }
        }
        if (toplevel) {
            addComponentACL(request);
        }
        return conditionList;
    }

    protected void addComponentACL(SearchRequest request) {
        String graphPathOfComponentSubGraph = String.join(
                "/",
                rootGraphName,
                "material_compositions",
                MaterialEntityGraphBuilder.COMPONENT_MATERIAL_SUBGRAPHNAME);

        EntityGraph materialSubGraph = entityGraph.selectSubGraph(graphPathOfComponentSubGraph);

        addSubGraphACL(materialSubGraph,
                MaterialEntityGraphBuilder.COMPONENT_MATERIAL_SUBGRAPHNAME,
                request.getUser());

    }

    private Set<Integer> getIdsFromMaterialTypes(MaterialType... types) {
        Set<Integer> ids = new HashSet<>();
        for (MaterialType t : types) {
            ids.add(t.getId());
        }
        return ids;
    }
}
