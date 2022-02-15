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
package de.ipb_halle.lbac.container.service;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ContainerNestingServiceTest extends TestBase {

    @Inject
    private ContainerNestingService containerNestingService;

    @Inject
    private ContainerService containerService;

    @AfterEach
    public void cleanUp() {

    }

    @Test
    public void test001_loadNestedInContainers() {
        int[] ids = initializeContainer();
        Set<Integer> nestedIds = containerNestingService.loadNestedInObjects(ids[0]);
        Assert.assertEquals(4, nestedIds.size());
        nestedIds.contains(ids[1]);
        nestedIds.contains(ids[2]);
        nestedIds.contains(ids[4]);
        nestedIds.contains(ids[5]);
    }

    @Test
    public void test002_loadAllSubContainer() {
        int[] ids = initializeContainer();
        Set<Integer> nestedIds = containerNestingService.getNestingService().loadAllSubObjects(ids[5]);
        Assert.assertEquals(4, nestedIds.size());
        nestedIds.contains(ids[1]);
        nestedIds.contains(ids[2]);
        nestedIds.contains(ids[4]);
        nestedIds.contains(ids[0]);
    }

    @Test
    public void test003_loadSubpath() {
        int[] ids = initializeContainer();
        Set<Integer> nestedIds = containerNestingService.getNestingService().loadSubpath(ids[1], ids[4]);
        Assert.assertEquals(2, nestedIds.size());
        nestedIds.contains(ids[2]);
        nestedIds.contains(ids[4]);
    }

    @Test
    public void test004_checkContainerNesting() {
        int[] ids = initializeContainer();
        Container c0 = containerService.loadContainerById(ids[5]);
        Container c1 = containerService.loadContainerById(ids[4]);
        Container c2 = containerService.loadContainerById(ids[3]);
        Container c3 = containerService.loadContainerById(ids[2]);
        Container c4 = containerService.loadContainerById(ids[1]);
        Container c5 = containerService.loadContainerById(ids[0]);
        // Initial Nesting state
        // C0 -> C1 -> C3 -> C4 -> C5
        // C2

        Assert.assertEquals(10, getNestedEntries());
        //Check nesting of C5
        Assert.assertFalse(getNested(c5.getId(), c4.getId(), "C5 -> C4"));  // C5 -> C4 (direct)
        Assert.assertTrue(getNested(c5.getId(), c3.getId(), "C5 -> C3"));  // C5 -> C3 (indirect via C4)
        Assert.assertTrue(getNested(c5.getId(), c1.getId(), "C5 -> C1"));  // C5 -> C1 (indirect via C4->C3)
        Assert.assertTrue(getNested(c5.getId(), c0.getId(), "C5 -> C0"));  // C5 -> C0 (indirect via C4->C3->C1)
        //Check nesting of C4
        Assert.assertFalse(getNested(c4.getId(), c3.getId(), "C4 -> C3"));  // C4 -> C3 (direct)
        Assert.assertTrue(getNested(c4.getId(), c1.getId(), "C4 -> C1"));  // C4 -> C1 (indirect via C3)
        Assert.assertTrue(getNested(c4.getId(), c0.getId(), "C4 -> C0"));  // C4 -> C0 (indirect via C3->C1)
        //Check nesting of C3
        Assert.assertFalse(getNested(c3.getId(), c1.getId(), "C3 -> C1"));  // C3 -> C1 (direct)
        Assert.assertTrue(getNested(c3.getId(), c0.getId(), "C3 -> C0"));  // C3 -> C0 (indirect via C3)
        //Check nesting of C1
        Assert.assertFalse(getNested(c1.getId(), c0.getId(), "C1 -> C0"));  // C1 -> 03 (direct)

        //Move Container C3 into C2
        // State after moving
        // C0 -> C1 
        // C2 -> C3 -> C4 -> C5
        c3.setParentContainer(c2);
        containerService.saveEditedContainer(c3);
        Assert.assertEquals(7, getNestedEntries());

        c0 = containerService.loadContainerById(ids[5]);
        c1 = containerService.loadContainerById(ids[4]);
        c2 = containerService.loadContainerById(ids[3]);
        c3 = containerService.loadContainerById(ids[2]);
        c4 = containerService.loadContainerById(ids[1]);
        c5 = containerService.loadContainerById(ids[0]);
        //Check nesting of C5
        Assert.assertFalse(getNested(c5.getId(), c4.getId(), "C5 -> C4"));  // C5 -> C4 (direct)
        Assert.assertTrue(getNested(c5.getId(), c3.getId(), "C5 -> C3"));  // C5 -> C3 (indirect via C4)
        Assert.assertTrue(getNested(c5.getId(), c2.getId(), "C5 -> C2"));  // C5 -> C2 (indirect via C4->C3)
        //Check nesting of C4
        Assert.assertFalse(getNested(c4.getId(), c3.getId(), "C4 -> C3"));  // C4 -> C3 (direct)
        Assert.assertTrue(getNested(c4.getId(), c2.getId(), "C4 -> C2"));  // C4 -> C2 (indirect via C3)
        //Check nesting of C3
        Assert.assertFalse(getNested(c3.getId(), c2.getId(), "C3 -> C2"));  // C3 -> C2 (direct)
        //Check nesting of C1
        Assert.assertFalse(getNested(c1.getId(), c0.getId(), "C1 -> C0"));  // C1 -> 03 (direct)

    }
    @SuppressWarnings("unchecked")
    private int getNestedEntries() {
        List<BigInteger> amountList = (List) entityManagerService.doSqlQuery("SELECT COUNT(*) FROM nested_containers");
        return amountList.get(0).intValue();
    }
    @SuppressWarnings("unchecked")
    private boolean getNested(int source, int target, String label) {
        List<Boolean> nestedList = (List) entityManagerService.doSqlQuery("SELECT nested FROM nested_containers WHERE sourceid=" + source + " AND targetid=" + target);
        if (nestedList.size() != 1) {
            throw new RuntimeException("test004_checkContainerNesting():" + label + " no or to many nestedEntries found!");
        } else {
            return nestedList.get(0);
        }
    }

    @Deployment
    public static WebArchive createDeployment() {

        WebArchive deployment = prepareDeployment("ContainerNestingServiceTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

    private int[] initializeContainer() {
        Container c0 = new Container();
        c0.setType(new ContainerType("ROOM", 100, false, true));
        c0.setLabel("C0");
        containerService.saveContainer(c0);

        Container c1 = new Container();
        c1.setType(new ContainerType("ROOM", 99, false, true));
        c1.setLabel("C1");
        c1.setParentContainer(c0);
        containerService.saveContainer(c1);

        Container c2 = new Container();
        c2.setType(new ContainerType("ROOM", 99, false, true));
        c2.setLabel("C2");
        containerService.saveContainer(c2);

        Container c3 = new Container();
        c3.setType(new ContainerType("ROOM", 98, false, true));
        c3.setLabel("C3");
        c3.setParentContainer(c1);
        containerService.saveContainer(c3);

        Container c4 = new Container();
        c4.setType(new ContainerType("ROOM", 97, false, true));
        c4.setLabel("C4");
        c4.setParentContainer(c3);
        containerService.saveContainer(c4);

        Container c5 = new Container();
        c5.setType(new ContainerType("ROOM", 96, false, true));
        c5.setLabel("C5");
        c5.setParentContainer(c4);
        containerService.saveContainer(c5);

        return new int[]{c5.getId(), c4.getId(), c3.getId(), c2.getId(), c1.getId(), c0.getId()};
    }
}
