/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.reporting.report;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.test.EntityManagerService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportServiceTest {
    private static final long serialVersionUID = 1L;
    private static final String INSERT_REPORT_FORMAT = "INSERT INTO reports (context, name, source) VALUES ('%s','%s','%s')";

    @Inject
    private EntityManagerService ems;

    @Inject
    private ReportService reportService;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "ReportServiceTest.war")
                .addClass(ReportService.class)
                .addClass(EntityManagerService.class)
                .addAsResource("PostgresqlContainerSchemaFiles")
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Test
    public void test_loadByContext() {
        insertReport("context1", "report1", "source1");
        insertReport("context2", "report2", "source2");

        assertThat(reportService.loadByContext(null), is(empty()));
        assertThat(reportService.loadByContext("context"), is(empty()));

        List<Report> reports = reportService.loadByContext("context1");
        assertThat(reports, hasSize(1));
        assertEquals("report1", reports.get(0).getName());
    }

    private void insertReport(String context, String name, String source) {
        ems.doSqlUpdate(String.format(INSERT_REPORT_FORMAT, context, name, source));
    }

}
