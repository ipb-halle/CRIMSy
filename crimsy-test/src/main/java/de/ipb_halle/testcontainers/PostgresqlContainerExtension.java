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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * JUnit5 extension that starts a PostgreSQL docker container <em>once</em>
 * before all tests are invoked. Testcontainer's <a href=
 * "https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers">singleton
 * container pattern</a> is employed, thus the container is teared down when the
 * JVM shuts down. The docker container exposed its port 5432 to the host on
 * port 65432 to avoid conflicts with existing PostgreSQL installations.
 * <p>
 * Usage: Annotate the test class with
 * {@code @ExtendWith(PostgresqlContainerExtension.class)}
 * 
 * @author flange
 */
public class PostgresqlContainerExtension implements BeforeAllCallback {
    private static final String SCHEMA_FILE_RESOURCE = "/PostgresqlContainerSchemaFiles";
    private static final String IMAGE_NAME = "ipbhalle/crimsydb:bingo_pg12";

    private static final AtomicBoolean FIRST_RUN = new AtomicBoolean(true);
    private PostgreSQLContainer<?> container;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (FIRST_RUN.getAndSet(false)) {
            startContainer();
        }
    }

    @SuppressWarnings("resource")
    private void startContainer() throws Exception {
        DockerImageName customPostgresImage = DockerImageName.parse(IMAGE_NAME).asCompatibleSubstituteFor("postgres");
        container = new PostgreSQLContainer<>(customPostgresImage).withUsername("postgres");
        container.setPortBindings(Arrays.asList("65432:5432"));

        logger.info("Starting Postgresql container with image " + customPostgresImage.toString());
        container.start();

        for (String schemaFile : getSchemaFiles()) {
            copySchema("schema/" + schemaFile);
            applySchema(schemaFile);
        }
    }

    private List<String> getSchemaFiles() {
        List<String> schemaFileNames = new ArrayList<> ();
        try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                        this.getClass().getResourceAsStream(SCHEMA_FILE_RESOURCE)))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (! line.startsWith("#")) {
                    schemaFileNames.add(line /* .strip() */ );
                }
            }
        } catch (Exception e) {
            logger.error("Error configuring schema files for PostgresqlContainerExtension", (Throwable) e);
        } 

        return schemaFileNames;
    }

    private void copySchema(String filename) {
        logger.info("Copy schema file " + filename + " into container path /");
        container.copyFileToContainer(MountableFile.forClasspathResource(filename), "/");
    }

    private void applySchema(String filename) throws Exception {
        logger.info("Load schema file /" + filename);
        ExecResult result = container.execInContainer("su", "postgres", "-c psql < /" + filename);
        logger.info("Stdout: " + result.getStdout());
        logger.error("Stderr: " + result.getStderr());
    }
}
