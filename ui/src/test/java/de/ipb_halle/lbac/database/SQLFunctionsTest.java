/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.database;

import static org.junit.Assert.assertEquals;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * Tests for custom SQL functions created via CREATE FUNCTION in the schema
 * files.
 * 
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class SQLFunctionsTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManagerService ems;

    @Inject
    private ContainerService containerService;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SQLFunctionsTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

    @Test
    public void test_getContainerLabel() {
        Container c1 = new Container();
        Container c2 = new Container();
        Container c3 = new Container();
        c1.setLabel("Container1");
        c2.setLabel("Container2");
        c3.setLabel("Container3");
        c1.setType(new ContainerType("ROOM", 100, false, false));
        c2.setType(new ContainerType("FREEZER", 90, false, false));
        c3.setType(new ContainerType("WELLPLATE", 80, false, false));
        c2.setParentContainer(c1);
        c3.setParentContainer(c2);
        c1 = containerService.saveContainer(c1);
        c2 = containerService.saveContainer(c2);
        c3 = containerService.saveContainer(c3);

        assertEquals("Container1", getContainerLabel(c1.getId()));
        assertEquals("Container1 > Container2", getContainerLabel(c2.getId()));
        assertEquals("Container1 > Container2 > Container3", getContainerLabel(c3.getId()));
    }

    private static final String GET_CONTAINER_LABEL_FORMAT = "SELECT getContainerLabel(%s)";

    private String getContainerLabel(Integer cid) {
        String query = String.format(GET_CONTAINER_LABEL_FORMAT, cid);
        return (String) ems.doSqlQuery(query).get(0);
    }

    @Test
    public void test_getDimensionLabel() {
        assertEquals("", getDimensionLabel(null, true, 2, 2));
        assertEquals("", getDimensionLabel(true, null, 2, 2));
        assertEquals("", getDimensionLabel(true, true, null, 2));

        /*
         * These tests are similar to
         * Container2dControllerTest.test_getDimensionLabel().
         */
        assertEquals("A1", getDimensionLabel(false, false, 0, 0));
        assertEquals("A6", getDimensionLabel(false, false, 5, 0));
        assertEquals("A11", getDimensionLabel(false, false, 10, 0));
        assertEquals("F1", getDimensionLabel(false, false, 0, 5));
        assertEquals("K1", getDimensionLabel(false, false, 0, 10));
        assertEquals("AA1", getDimensionLabel(false, false, 0, 26));
        assertEquals("AF1", getDimensionLabel(false, false, 0, 31));

        assertEquals("A1", getDimensionLabel(false, true, 0, 0));
        assertEquals("A6", getDimensionLabel(false, true, 0, 5));
        assertEquals("A11", getDimensionLabel(false, true, 0, 10));
        assertEquals("F1", getDimensionLabel(false, true, 5, 0));
        assertEquals("K1", getDimensionLabel(false, true, 10, 0));
        assertEquals("AA1", getDimensionLabel(false, true, 26, 0));
        assertEquals("AF1", getDimensionLabel(false, true, 31, 0));

        assertEquals("A0", getDimensionLabel(true, false, 0, 0));
        assertEquals("A5", getDimensionLabel(true, false, 5, 0));
        assertEquals("A10", getDimensionLabel(true, false, 10, 0));
        assertEquals("F0", getDimensionLabel(true, false, 0, 5));
        assertEquals("K0", getDimensionLabel(true, false, 0, 10));
        assertEquals("AA0", getDimensionLabel(true, false, 0, 26));
        assertEquals("AF0", getDimensionLabel(true, false, 0, 31));
    }

    private static final String GET_DIMENSION_LABEL_FORMAT = "SELECT getDimensionLabel(%s, %s, %s, %s)";

    private String getDimensionLabel(Boolean zerobased, Boolean swapdimension, Integer itemrow, Integer itemcol) {
        String query = String.format(GET_DIMENSION_LABEL_FORMAT, zerobased, swapdimension, itemrow, itemcol);
        return (String) ems.doSqlQuery(query).get(0);
    }
}