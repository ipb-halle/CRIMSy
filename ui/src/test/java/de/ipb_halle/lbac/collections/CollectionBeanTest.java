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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.mock.CollectionBeanMock;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This class will provide some test cases for the CollectionService class.
 */
@RunWith(Arquillian.class)
public class CollectionBeanTest extends TestBase {

    @Inject
    private CollectionService collectionService;
    @Inject
    private FileService fileService;
    @Inject
    private GlobalAdmissionContext context;
    @Inject
    private ACListService acListService;
    @Inject
    private CollectionOrchestrator orchestrator;

    private DocumentSearchBean searchBean;

    private CollectionBean bean;

    private User publicUser;

    @Override
    public void setUp() {
        super.setUp();
        bean = new CollectionBeanMock()
                .setCollectionService(collectionService)
                .setDocumentSearchBean(searchBean)
                .setFileService(fileService)
                .setFileEntityService(fileEntityService)
                .setGlobalAdmissionContext(context)
                .setACListService(acListService)
                .setTermVectorEntityService(termVectorEntityService)
                .setMemberService(memberService);

        bean.setCollectionOrchestrator(orchestrator);
        bean.setNodeService(nodeService);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    @Ignore
    @Test
    public void test001_logIn() {
        LoginEvent logInEvent = new LoginEvent(publicUser);
        bean.setCurrentAccount(logInEvent);
        Assert.assertEquals("READ", bean.getEditMode());
        Assert.assertEquals(publicUser.getId(), bean.getCurrentAccount().getId());
        bean.setCurrentAccount(publicUser);
        Assert.assertEquals(publicUser.getId(), bean.getCurrentAccount().getId());
    }

    @Ignore
    @Test
    public void test002_actionCreate() {
        LoginEvent logInEvent = new LoginEvent(publicUser);
        bean.setCurrentAccount(logInEvent);
        bean.getActiveCollection().setDescription("Test");
        bean.getActiveCollection().setIndexPath("");
        bean.getActiveCollection().setName("Test-Collection");

        bean.actionCreate();

        Collection loadedCol = collectionService.loadById(bean.getActiveCollection().getId());
        Assert.assertNotNull(loadedCol);

    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CollectionBeanTest.war")
                .addClass(KeyManager.class)
                .addClass(CollectionService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(CollectionSearchState.class)
                .addClass(ACListService.class)
                .addClass(FileEntityService.class)
                .addClass(FileService.class)
                .addClass(CollectionBean.class)
                .addClass(DocumentSearchBean.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(DocumentSearchOrchestrator.class)
                .addClass(MembershipOrchestrator.class);
    }

}
