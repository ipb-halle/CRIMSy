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
package de.ipb_halle.lbac.projects;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.admission.ACEntry;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectBean;
import javax.inject.Inject;
import de.ipb_halle.lbac.project.ProjectEditBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.SearchWebService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class ProjectEditBeanTest extends TestBase {

    @Inject
    protected ProjectEditBean instance;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ProjectEditBeanTest.war")
                .addClass(ProjectBean.class)
                .addClass(Navigator.class)
                .addClass(ProjectService.class)
                .addClass(WordCloudBean.class)
                .addClass(WordCloudWebClient.class)
                .addClass(CollectionBean.class)
                .addClass(DocumentSearchOrchestrator.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(SearchWebService.class)
                .addClass(DocumentSearchService.class)
                .addClass(DocumentSearchService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(SearchService.class)
                .addClass(SearchWebService.class)
                .addClass(Updater.class)
                .addClass(ProjectEditBean.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));
    }

    @Test
    public void test01_addGroupToProjectACL() {

        ProjectEditBean projectEditBean = new ProjectEditBean();
        projectEditBean.setMemberService(memberService);
        projectEditBean.init();
        projectEditBean.addGroupToProjectACL(
                createGroup("test01_addGroupToProjectACL",
                        nodeService.getLocalNode(),
                        memberService,
                        membershipService)
        );

        int sizeBeforeAdding = projectEditBean.getAddableGroupsForProject().size();
        Group groupToAdd = projectEditBean.getAddableGroupsForProject().get(0);
        projectEditBean.addGroupToProjectACL(groupToAdd);
        Assert.assertEquals(
                sizeBeforeAdding,
                projectEditBean.getAddableGroupsForProject().size() + 1);
    }

    @Test
    public void test02_removeAceFromProjectACL() {

        ProjectEditBean projectEditBean = new ProjectEditBean();
        projectEditBean.setMemberService(memberService);
        projectEditBean.init();
        projectEditBean.addGroupToProjectACL(
                createGroup("test02_removeAceFromProjectACL",
                        nodeService.getLocalNode(),
                        memberService,
                        membershipService)
        );

        int sizeBeforeAction = projectEditBean.getACEntriesOfProject().size();
        Group groupToAdd = projectEditBean.getAddableGroupsForProject().get(0);
        projectEditBean.addGroupToProjectACL(groupToAdd);

        projectEditBean.removeAceFromProjectACL(projectEditBean.getACEntriesOfProject().get(0));
        Assert.assertEquals(
                sizeBeforeAction,
                projectEditBean.getACEntriesOfProject().size());
    }

    @Test
    public void test03_RoleTemplate() {
        String materialDetail = MaterialDetailType.COMMON_INFORMATION.toString();
        ProjectEditBean projectEditBean = new ProjectEditBean();
        projectEditBean.setMemberService(memberService);
        projectEditBean.init();

        Group g
                = projectEditBean
                        .getAddableGroupsForRoleTemplates(materialDetail).get(0);
        int sizeBeforeAction = projectEditBean
                .getAddableGroupsForRoleTemplates(materialDetail).size();
        projectEditBean.addAceToRoleTemplate(g, materialDetail);

        int sizeAfterAction = projectEditBean
                .getAddableGroupsForRoleTemplates(materialDetail).size();

        Assert.assertEquals(
                sizeBeforeAction,
                sizeAfterAction + 1);

        ACEntry ace = projectEditBean.getACEntriesForDetailRole(materialDetail).get(0);

        projectEditBean.removeAceFromRoleTemplateACL(ace, materialDetail);

        sizeAfterAction = projectEditBean
                .getAddableGroupsForRoleTemplates(materialDetail).size();

        Assert.assertEquals(
                sizeBeforeAction,
                sizeAfterAction);

    }

}
