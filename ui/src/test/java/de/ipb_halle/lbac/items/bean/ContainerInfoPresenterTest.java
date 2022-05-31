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

import static de.ipb_halle.lbac.project.ProjectType.DUMMY_PROJECT;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.Project;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author fmauz
 */
public class ContainerInfoPresenterTest {
    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    @Test
    public void test_getContainerName() {
        ContainerInfoPresenter presenter = new ContainerInfoPresenter(null, messagePresenter);
        assertEquals("", presenter.getContainerName());

        Container container = new Container();
        container.setLabel(null);
        presenter = new ContainerInfoPresenter(container, messagePresenter);
        assertEquals("", presenter.getContainerName());

        container.setLabel("abc");
        assertEquals("abc", presenter.getContainerName());
    }

    @Test
    public void test_getContainerType() {
        ContainerInfoPresenter presenter = new ContainerInfoPresenter(null, messagePresenter);
        assertEquals("", presenter.getContainerType());

        Container container = new Container();
        container.setType(null);
        presenter = new ContainerInfoPresenter(container, messagePresenter);
        assertEquals("", presenter.getContainerType());

        container.setType(new ContainerType("TYPENAME", 0, false, false));
        assertEquals("container_type_TYPENAME", presenter.getContainerType());
    }

    @Test
    public void test_getContainerProject() {
        ContainerInfoPresenter presenter = new ContainerInfoPresenter(null, messagePresenter);
        assertEquals("", presenter.getContainerProject());

        Container container = new Container();
        container.setProject(null);
        presenter = new ContainerInfoPresenter(container, messagePresenter);
        assertEquals("", presenter.getContainerProject());

        container.setProject(new Project(DUMMY_PROJECT, "Rocket Science Project"));
        assertEquals("Rocket Science Project", presenter.getContainerProject());
    }

    @Test
    public void test_getContainerLocation() {
        ContainerInfoPresenter presenter = new ContainerInfoPresenter(null, messagePresenter);
        assertEquals("", presenter.getContainerLocation());

        Container container = new Container();
        presenter = new ContainerInfoPresenter(container, messagePresenter);
        assertEquals("", presenter.getContainerLocation());

        // build a container hierarchy
        Container parent = new Container();
        parent.setLabel("parent");
        Container parentsParent = new Container();
        parentsParent.setLabel("parent's parent");
        // not the right way
        // parent.setParentContainer(parentsParent);
        // container.setParentContainer(parent);

        container.getContainerHierarchy().add(parent);
        container.getContainerHierarchy().add(parentsParent);
        assertEquals("parent's parent-><br>parent", presenter.getContainerLocation());
    }
}
