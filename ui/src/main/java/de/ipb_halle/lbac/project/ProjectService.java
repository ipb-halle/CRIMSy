/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.component.MaterialDetailType;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.MemberService;
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
public class ProjectService {

    private final String SQL_PROJECT_TEMPLATES = "SELECT id,materialdetailtypeid,aclistid,projectid FROM projecttemplates WHERE projectid=:pid";
    private final String SQL_GET_SIMILAR_NAMES = "SELECT name FROM projects WHERE LOWER(name) LIKE LOWER(:name)";

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
    public List<String> getSimilarProjectNames(String name, User user) {
        return this.em.createNativeQuery(SQL_GET_SIMILAR_NAMES)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    /**
     *
     * @param pE
     * @return
     */
    private Project loadDetailInfosOfProject(ProjectEntity pE) {
        ACList projectACL = aclistService.loadById(pE.getUserGroups());
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
            ACList projectACL = aclistService.loadById(pE.getUserGroups());
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
        p.setUserGroups(existingAcl);
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

}
