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

import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.TermVectorService;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.mock.CollectionBeanMock;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This class will provide some test cases for the CollectionService class.
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
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

    private CollectionBean bean;

    @BeforeEach
    public void init() {
        bean = new CollectionBeanMock()
                .setCollectionService(collectionService)
                .setFileService(fileService)
                .setFileObjectService(fileObjectService)
                .setGlobalAdmissionContext(context)
                .setACListService(acListService)
                .setTermVectorService(termVectorService)
                .setMemberService(memberService);

        bean.setCollectionOrchestrator(orchestrator);
        bean.setNodeService(nodeService);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    @Test
    public void test001_logIn() {
        LoginEvent logInEvent = new LoginEvent(publicUser);
        bean.setCurrentAccount(logInEvent);
        Assert.assertEquals("READ", bean.getEditMode());
        Assert.assertEquals(publicUser.getId(), bean.getCurrentAccount().getId());
        bean.setCurrentAccount(publicUser);
        Assert.assertEquals(publicUser.getId(), bean.getCurrentAccount().getId());
    }

    @Disabled("Due to some problems with the test database")
    @Test
    public void test002_actionCreate() {
        LoginEvent logInEvent = new LoginEvent(publicUser);
        bean.setCurrentAccount(logInEvent);
        bean.getActiveCollection().setDescription("Test");
        bean.getActiveCollection().setName("Test-Collection");

        bean.actionCreate();

        Collection loadedCol = collectionService.loadById(bean.getActiveCollection().getId());
        Assert.assertNotNull(loadedCol);
    }

    @Test
    public void test003_getCreatableLocalCollections() {
        entityManagerService.doSqlUpdate("DELETE FROM collections;");
        Collection col = new Collection();
        col.setName("ReadOnly");
        col.setOwner(adminUser);
        col.setACList(acListReadable);

        collectionService.save(col);

        LoginEvent logInEvent = new LoginEvent(publicUser);
        bean.setCurrentAccount(logInEvent);
        Assert.assertEquals(0, bean.getCreatableLocalCollections().size());
        logInEvent = new LoginEvent(adminUser);
        bean.setCurrentAccount(logInEvent);
        Assert.assertEquals(1, bean.getCreatableLocalCollections().size());

        entityManagerService.doSqlUpdate("DELETE FROM collections WHERE id=" + col.getId());
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
                .addClass(FileObjectService.class)
                .addClass(FileService.class)
                .addClass(CollectionBean.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorService.class)
                .addClass(MembershipOrchestrator.class);
    }

}
