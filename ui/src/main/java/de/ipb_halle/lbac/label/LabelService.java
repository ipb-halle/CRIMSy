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
package de.ipb_halle.lbac.label;

import de.ipb_halle.lbac.items.service.*;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDifference;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.bean.history.ItemComparator;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.ItemPositionHistoryList;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.items.entity.ItemHistoryEntity;
import de.ipb_halle.lbac.items.entity.ItemPositionsHistoryEntity;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.items.Code25LabelGenerator;
import de.ipb_halle.lbac.search.PermissionConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.DbField;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.OrderDirection;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.SqlCountBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class LabelService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;
    private String SQL_UPDATE_ITEM_LABEL
            = "UPDATE items SET label=:label WHERE id=:id";

    private Code25LabelGenerator labelGenerator;

    @PostConstruct
    public void init() {
        labelGenerator = new Code25LabelGenerator();
    }

    public String createLabel(int id, Class clazz) {
        return labelGenerator.generateLabel(id);
    }

    public void saveItemLabel(String label, int id) {
        Query q = em.createNativeQuery(SQL_UPDATE_ITEM_LABEL);
        q.setParameter("label", label);
        q.setParameter("id", id);
        q.executeUpdate();
    }

}
