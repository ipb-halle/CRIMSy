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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.SqlStringWrapper;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.DbField;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.OrderDirection;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class ProjectService implements Serializable {

    private final String SQL_PROJECT_TEMPLATES = "SELECT id,materialdetailtypeid,aclistid,projectid "
            + "FROM projecttemplates "
            + "WHERE projectid=:pid";

    private final String SQL_GET_NAME_AVAILABLE = "SELECT COUNT(*) "
            + "FROM projects WHERE LOWER(:name) = LOWER(name)";

    private final String SQL_DELETE_PROJECT_TEMPLATES
            = "DELETE FROM projecttemplates "
            + "WHERE projectid=:projectid";

    private final String SQL_DEACTIVATE_PROJECT
            = "UPDATE projects"
            + " SET deactivated=:deactivated"
            + " WHERE id=:id";

    @Inject
    private MemberService memberService;

    @Inject
    private ACListService aclistService;

    @Inject
    private ACListService acListService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private NodeService nodeService;

    @PostConstruct
    public void init() {

    }

    /**
     * Gets all project names which matches the pattern %name%
     *
     * @param name name for searching
     * @param user
     * @return List of matching materialnames
     */
    @SuppressWarnings("unchecked")
    public List<String> getSimilarProjectNames(String name, User user) {
        List<String> projectNames = new ArrayList<>();
        ProjectSearchRequestBuilder builder = new ProjectSearchRequestBuilder(user, 0, Integer.MAX_VALUE);
        builder.setDeactivated(false);
        builder.setProjectName(name);

        SearchResult result = loadProjects(builder.build());
        List<Project> projects = result.getAllFoundObjects(Project.class, result.getNode());
        for (Project p : projects) {
            projectNames.add(p.getName());
        }
        return projectNames;
    }

    /**
     *
     * @param pE
     * @return
     */
    @SuppressWarnings("unchecked")
    private Project loadDetailInfosOfProject(ProjectEntity pE) {
        ACList projectACL = aclistService.loadById(pE.getACList());
        Map<MaterialDetailType, ACList> detailTemplates = new HashMap<>();
        List<ProjectTemplateEntity> templates = em.createNativeQuery(
                SQL_PROJECT_TEMPLATES, ProjectTemplateEntity.class)
                .setParameter("pid", pE.getId()).getResultList();
        for (ProjectTemplateEntity ptE : templates) {
            detailTemplates.put(
                    MaterialDetailType.getTypeById(ptE.getMaterialDetailTypeId()),
                    acListService.loadById(ptE.getAcListId()));
        }
        List<BudgetReservation> budgetReservation = new ArrayList<>();
        User projectOwner = memberService.loadUserById(pE.getOwner());
        return new Project(
                pE,
                projectOwner,
                projectACL,
                detailTemplates,
                budgetReservation);
    }

    /**
     *
     * @param id
     * @return
     */
    public Project loadProjectById(int id) {
        ProjectEntity entity = em.find(ProjectEntity.class, id);
        return loadDetailInfosOfProject(entity);
    }

    public SearchResult loadProjects(SearchRequest request) {

        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
        ProjectEntityGraphBuilder graphBuilder = new ProjectEntityGraphBuilder();
        EntityGraph graph = graphBuilder.buildEntityGraph(true);
        ProjectSearchConditionBuilder conbuilder = new ProjectSearchConditionBuilder(graph, "projects");
        Condition con = conbuilder.convertRequestToCondition(
                request, ACPermission.permREAD);
        SqlBuilder builder = new SqlBuilder(graph);

        String sql = builder.query(con, createOrderList());
        Query query = this.em.createNativeQuery(sql, ProjectEntity.class);
        for (Value param : builder.getValueList()) {
            query.setParameter(param.getArgumentKey(), param.getValue());
        }
        @SuppressWarnings("unchecked")
        List<ProjectEntity> entities = query.getResultList();
        for (ProjectEntity entity : entities) {
            result.addResult(loadDetailInfosOfProject(entity));
        }
        return result;
    }

    public boolean isProjectNameAvailable(String name) {
        BigInteger i = (BigInteger) this.em.createNativeQuery(
                SQL_GET_NAME_AVAILABLE)
                .setParameter("name", name)
                .getResultList().get(0);
        return i.intValue() == 0;
    }

    public void changeDeactivationState(int projectId, boolean deactivated) {
        Query q = em.createNativeQuery(SQL_DEACTIVATE_PROJECT);
        q.setParameter("deactivated", deactivated);
        q.setParameter("id", projectId);
        q.executeUpdate();
    }

    /**
     *
     * @param p
     * @return
     */
    public Project saveProjectToDb(Project p) {

        ACList existingAcl = acListService.save(p.getUserGroups());
        p.setACList(existingAcl);
        for (MaterialDetailType md : p.getDetailTemplates().keySet()) {
            if (!p.getDetailTemplates().get(md).getACEntries().isEmpty()) {
                existingAcl = acListService.save(p.getDetailTemplates().get(md));
                p.getDetailTemplates().put(md, existingAcl);

            }
        }
        ProjectEntity pE = new ProjectEntity(p);
        this.em.persist(pE);
        p.setId(pE.getId());
        for (MaterialDetailType md : p.getDetailTemplates().keySet()) {
            if (!p.getDetailTemplates().get(md).getACEntries().isEmpty()) {
                ProjectTemplateEntity ptE = new ProjectTemplateEntity(
                        md.getId(),
                        p.getDetailTemplates().get(md).getId(),
                        pE.getId());
                this.em.persist(ptE);
            }
        }
        return p;
    }

    public void saveEditedProjectToDb(Project p) {
        ACList existingAcl = acListService.save(p.getUserGroups());
        p.setACList(existingAcl);
        for (MaterialDetailType md : p.getDetailTemplates().keySet()) {
            if (!p.getDetailTemplates().get(md).getACEntries().isEmpty()) {
                existingAcl = acListService.save(p.getDetailTemplates().get(md));
                p.getDetailTemplates().put(md, existingAcl);

            }
        }
        ProjectEntity pE = p.createEntity();
        pE.setMtime(new Date());
        this.em.merge(pE);
        this.em.createNativeQuery(SQL_DELETE_PROJECT_TEMPLATES)
                .setParameter("projectid", p.getId())
                .executeUpdate();

        for (MaterialDetailType md : p.getDetailTemplates().keySet()) {
            if (!p.getDetailTemplates().get(md).getACEntries().isEmpty()) {
                ProjectTemplateEntity ptE = new ProjectTemplateEntity(md.getId(), p.getDetailTemplates().get(md).getId(), pE.getId());
                this.em.persist(ptE);
            }
        }

    }

    private List<DbField> createOrderList() {
        DbField labelField = new DbField()
                .setColumnName("name")
                .setTableName("projects")
                .setOrderDirection(OrderDirection.ASC);

        List<DbField> orderList = new ArrayList<>();
        orderList.add(labelField);
        return orderList;
    }
}
