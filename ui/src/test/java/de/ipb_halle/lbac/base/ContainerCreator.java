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
package de.ipb_halle.lbac.base;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.test.EntityManagerService;

/**
 *
 * @author fmauz
 */
public class ContainerCreator {

    protected EntityManagerService entityManagerService;
    protected ContainerService containerService;

    public ContainerCreator(EntityManagerService entityManagerService, ContainerService containerService) {
        this.entityManagerService = entityManagerService;
        this.containerService = containerService;
    }

    public Container createAndSaveContainer(String label, Container parent) {
        Container c = new Container();
        c.setLabel(label);
        c.setType(new ContainerType("ROOM", 100, true, true));
        c.setParentContainer(parent);
        c = containerService.saveContainer(c);
        return c;

    }
}
