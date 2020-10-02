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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class ProjectService implements Serializable {

    private final String SQL_PROJECT_TEMPLATES = "SELECT id,materialdetailtypeid,aclistid,projectid FROM projecttemplates WHERE projectid=:pid";
    private final String SQL_GET_SIMILAR_NAMES
            = "SELECT DISTINCT (p.name) "
            + "FROM projects p "
            + SqlStringWrapper.JOIN_KEYWORD + " "
            + "WHERE p.name ILIKE :name "
            + "AND " + SqlStringWrapper.WHERE_KEYWORD + " "
            + "ORDER BY p.name";

    private final String SQL_LOAD_PROJECT_BY_NAME
            = "SELECT p.id "
            + "FROM projects p "
            + SqlStringWrapper.JOIN_KEYWORD + " "
            + "WHERE p.name=:projectname "
            + "AND " + SqlStringWrapper.WHERE_KEYWORD + " ";

    private final String SQL_DELETE_PROJECT_TEMPLATES
            = "DELETE FROM projecttemplates "
            + "WHERE projectid=:projectid";

    @Inject
    private MemberService memberService;

    @Inject
    private ACListService aclistService;

    @Inject
    private ACListService acListService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * Gets all project names which matches the pattern %name%
     *
     * @param name name for searching
     * @param user
     * @return List of matching materialnames
     */
    @SuppressWarnings("unchecked")
    public List<String> getSimilarProjectNames(String name, User user) {

        String sql = SqlStringWrapper.aclWrapper(SQL_GET_SIMILAR_NAMES, "p.aclist_id", "p.ownerid", ACPermission.permREAD);
        return this.em.createNativeQuery(sql)
                .setParameter("name", "%" + name + "%")
                .setParameter("userid", user.getId())
                .getResultList();
    }

    /**
     *
     * @param pE
     * @return
     */
    @SuppressWarnings("unchecked")
    private Project loadDetailInfosOfProject(ProjectEntity pE) {
        ACList projectACL = aclistService.loadById(pE.getAclist_id());
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
        User projectOwner = memberService.loadUserById(pE.getOwnerId());
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

    /**
     * TO DO: only projects which are accessable by user should be loaded
     *
     * @param u
     * @param projectName
     * @return
     */
    @SuppressWarnings("unchecked")
    public Project loadProjectByName(User u, String projectName) {
        if (projectName.trim().isEmpty()) {
            return null;
        }

        String sql = SqlStringWrapper.aclWrapper(SQL_LOAD_PROJECT_BY_NAME, "p.aclist_id", "p.ownerid", ACPermission.permREAD);
        List<Object> ids = this.em
                .createNativeQuery(sql)
                .setParameter("projectname", projectName)
                .setParameter("userid", u.getId())
                .getResultList();

        if (!ids.isEmpty()) {
            return loadProjectById((Integer) ids.get(0));
        } else {
            return null;
        }
    }

    /**
     *
     * @param u
     * @return
     */
    public List<Project> loadReadableProjectsOfUser(User u) {
        List<Project> projects = new ArrayList<>();
        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> criteriaQuery = builder.createQuery(ProjectEntity.class);
        Root<ProjectEntity> collectionRoot = criteriaQuery.from(ProjectEntity.class);
        criteriaQuery.select(collectionRoot);

        List<ProjectEntity> results = this.em.createQuery(criteriaQuery.distinct(true)).getResultList();
        for (ProjectEntity pE : results) {
            ACList projectACL = aclistService.loadById(pE.getAclist_id());
            boolean isOwner = pE.getOwnerId().equals(u.getId());
            boolean hasReadRight = false;
            if (!isOwner) {
                hasReadRight = acListService.isPermitted(ACPermission.permREAD, projectACL, u);
            }
            if (isOwner || hasReadRight) {
                projects.add(loadDetailInfosOfProject(pE));
            }
        }
        return projects;
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
                ProjectTemplateEntity ptE = new ProjectTemplateEntity(md.getId(), p.getDetailTemplates().get(md).getId(), pE.getId());
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

}
