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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.mock.ContainerLocalizerMock;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ContainerInfoPresenterTest extends TestBase {

    @Test
    public void test001_noContainerSet() {
        ContainerInfoPresenter presenter = new ContainerInfoPresenter(null, MessagePresenterMock.getInstance());
        Assert.assertEquals("", presenter.getContainerLocation());
        Assert.assertEquals("", presenter.getContainerName());
        Assert.assertEquals("", presenter.getContainerProject());
        Assert.assertEquals("", presenter.getContainerType());
    }

    @Test
    public void test002_noContainerWithoutProjectAndContainer() {
        Container container = createContainer("testContainer");
        ContainerInfoPresenter presenter = new ContainerInfoPresenter(container, MessagePresenterMock.getInstance());

        Assert.assertEquals("", presenter.getContainerLocation());
        Assert.assertEquals("testContainer", presenter.getContainerName());
        Assert.assertEquals("", presenter.getContainerProject());
        Assert.assertEquals("container_type_GLASS_FLASK", presenter.getContainerType());
    }

    @Test
    public void test003_noContainerWithProjectAndContainer() {
        Container container = createContainer(
                "testContainer",
                createProject("testProject"),
                createContainer("testContainer2"));

        ContainerInfoPresenter presenter = new ContainerInfoPresenter(container, MessagePresenterMock.getInstance());
        Assert.assertEquals("testContainer2", presenter.getContainerLocation());
        Assert.assertEquals("testContainer", presenter.getContainerName());
        Assert.assertEquals("testProject", presenter.getContainerProject());
        Assert.assertEquals("container_type_GLASS_FLASK", presenter.getContainerType());
    }

    private Container createContainer(String name, Project p, Container c) {
        Container container = new Container();
        container.setType(new ContainerType("GLASS_FLASK", 0, true, false));
        container.setId(1);
        container.setLabel(name);
        container.setProject(p);
        if (c != null) {
            container.getContainerHierarchy().add(c);
        }
        return container;
    }

    private Container createContainer(String name) {
        return createContainer(name, null, null);
    }

    private Project createProject(String name) {
        Project p = new Project();
        p.setId(1);
        p.setName(name);
        return p;

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerInfoPresenterTest.war")
                .addClass(Navigator.class)
                .addClass(ProjectService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
