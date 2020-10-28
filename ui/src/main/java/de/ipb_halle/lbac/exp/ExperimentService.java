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
package de.ipb_halle.lbac.exp;

/**
 * ExperimentService provides service to load, save, update experiment entities.
 *
 * The current implementation is rather a mock implementation as many important
 * aspects (permissions, history, filtering, ...) are missing.
 */
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.exp.search.ExperimentEntityGraphBuilder;
import de.ipb_halle.lbac.search.PermissionConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// import javax.persistence.Persistence;
@Stateless
public class ExperimentService implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String TEMPLATE_FLAG = "TEMPLATE_FLAG";

    @Inject
    private ACListService aclistService;

    @Inject
    private NodeService nodeService;

    @Inject
    private MemberService memberService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;
    private ExperimentEntityGraphBuilder graphBuilder;
    private EntityGraph graph;
    private PermissionConditionBuilder permissionConditionBuilder;

    public ExperimentService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void ExperimentServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
        graphBuilder = new ExperimentEntityGraphBuilder(aclistService);
    }

    /**
     * build
     */
    public Query createExperimentQuery(String rawSql, Map<String, Object> cmap, Class targetClass) {
        Query q;
        if (targetClass == null) {
            q = this.em.createNativeQuery(rawSql);
        } else {
            q = this.em.createNativeQuery(rawSql, targetClass);
        }

        return q.setParameter(TEMPLATE_FLAG, cmap.getOrDefault(TEMPLATE_FLAG, null));
    }

    public SearchResult load(SearchRequest request) {
        SearchResult back = new SearchResultImpl();
        graph = createEntityGraph(request);
        SqlBuilder sqlBuilder = new SqlBuilder(graph);

        permissionConditionBuilder = new PermissionConditionBuilder(
                aclistService,
                request.getUser(),
                ACPermission.permREAD)
                .addFields(AttributeType.EXPERIMENT, AttributeType.MEMBER);

        String sql = sqlBuilder.query(permissionConditionBuilder.addPermissionCondition(request.getCondition()));
        Query q = em.createNativeQuery(sql, ExperimentEntity.class);
        for (Value param : sqlBuilder.getValueList()) {
            q.setParameter(param.getArgumentKey(), param.getValue());
        }
        q.setFirstResult(request.getFirstResult());
        q.setMaxResults(request.getMaxResults());
        List<ExperimentEntity> entities = q.getResultList();
        for (ExperimentEntity e : entities) {
            Experiment exp = new Experiment(
                    e,
                    aclistService.loadById(e.getACList()),
                    memberService.loadUserById(e.getOwner()));
            back.addResults(nodeService.getLocalNode(), Arrays.asList(exp));
        }
        return back;
    }

    /**
     * load an experiment by id
     *
     * NOTE: the Experiment DTO does NOT include its experiment records. Records
     * MUST be handled separately.
     *
     * @param id experiment Id
     * @return the Experiment object
     */
    public Experiment loadById(Integer id) {
        ExperimentEntity entity = this.em.find(ExperimentEntity.class, id);
        return new Experiment(
                entity, aclistService.loadById(entity.getACList()),
                memberService.loadUserById(entity.getOwner()));
    }

    public void updateExperimentAcl(int experimentid, ACList newAcList) {
        // TO DO: save History 
        ACList acl = aclistService.save(newAcList);
        Experiment exp = loadById(experimentid);
        exp.setACList(acl);
        em.merge(exp.createEntity());
    }

    /**
     * save a single experiment object
     *
     * @param e the experiment to save
     * @return the persisted Experiment DTO
     */
    public Experiment save(Experiment e) {
        return new Experiment(
                this.em.merge(e.createEntity()),
                e.getACList(),
                e.getOwner());
    }

    private EntityGraph createEntityGraph(SearchRequest request) {
        graphBuilder = new ExperimentEntityGraphBuilder(aclistService);
        
        return graphBuilder.buildEntityGraph(request.getCondition());
    }
}
