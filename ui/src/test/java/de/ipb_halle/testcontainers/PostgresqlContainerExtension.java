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
package de.ipb_halle.testcontainers;

import java.util.Arrays;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * JUnit5 extension that starts a PostgreSQL docker container once before all
 * tests are invoked and tears it down after all test invocations. The docker
 * container exposed its port 5432 to the host on port 65432 to avoid conflicts
 * with existing PostgreSQL installations.
 * <p>
 * Usage: Annotate the test class with
 * {@code @ExtendWith(PostgresqlContainerExtension.class)}
 * 
 * @author flange
 */
public class PostgresqlContainerExtension implements BeforeAllCallback, AfterAllCallback {
    private static final String IMAGE_NAME = "ipbhalle/crimsydb:bingo_pg12";
    private static final String DB_NAME = "lbac";
    private static final String DB_USER = "lbac";
    private static final String DB_PASSWORD = "lbac";

    private PostgreSQLContainer<?> container;

    @SuppressWarnings("resource")
    @Override
    public void beforeAll(ExtensionContext context) {
        DockerImageName customPostgresImage = DockerImageName.parse(IMAGE_NAME).asCompatibleSubstituteFor("postgres");
        container = new PostgreSQLContainer<>(customPostgresImage).withDatabaseName(DB_NAME).withUsername(DB_USER)
                .withPassword(DB_PASSWORD);
        container.setPortBindings(Arrays.asList("65432:5432"));
        container.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        container.stop();
    }
}