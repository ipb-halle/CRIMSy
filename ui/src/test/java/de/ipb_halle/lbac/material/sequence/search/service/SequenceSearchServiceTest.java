/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.service;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceType;

import static de.ipb_halle.lbac.material.sequence.SequenceType.PROTEIN;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class SequenceSearchServiceTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private MaterialService materialService;

    @Inject
    SequenceSearchService sequenceSearchService;

    @Inject
    private FastaRESTSearchServiceMock mock;

    private Project project;
    private int publicAclId;

    private Sequence sequence;

    private final String SQL_LOAD_PARAMETER = "SELECT id from temp_search_parameter";

    @BeforeEach
    public void init() {
        mock.setBehaviour(null);

        materialService.setStructureInformationSaver(
                new StructureInformationSaverMock());
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        project.setOwner(publicUser);
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveProjectToDb(project);
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();
    }

    @AfterEach
    public void cleanUp() {
        entityManagerService.doSqlUpdate("DELETE FROM temp_search_parameter");
    }

    @Test
    public void test001_saveAndLoadSequence() {
        createAndSaveSequence();
        mock.setBehaviour((e) -> {
            FastaResult fastaResult = new FastaResult();
            fastaResult.setQueryAlignmentLine("abc");
            fastaResult.setSubjectSequenceName(String.valueOf(sequence.getId()));
            FastaSearchResult result = new FastaSearchResult();
            result.setProgramOutput("def");
            result.setResults(Arrays.asList(fastaResult));

            return Response.ok(result).build();
        });
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        builder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA, 1);
        builder.addMaterialType(MaterialType.SEQUENCE);

        SearchResult result = sequenceSearchService.searchSequences(builder.build());

        Assert.assertEquals(0, result.getErrorMessages().size());
        Assert.assertEquals(1, result.getAllFoundObjects().size());
        Assert.assertEquals(0, entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER).size());
    }

    @Test
    public void test002_process500Error() {
        mock.setBehaviour((e) -> {
            return Response.serverError().build();
        });
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        builder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA, 1);
        builder.addMaterialType(MaterialType.SEQUENCE);

        SearchResult result = sequenceSearchService.searchSequences(builder.build());

        Assert.assertEquals(1, result.getErrorMessages().size());
        Assert.assertEquals(0, result.getAllFoundObjects().size());
        Assert.assertEquals(0, entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER).size());
    }

    @Test
    public void test003_processRuntimeError() {
        mock.setBehaviour((e) -> {
            throw new RuntimeException();
        });
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        builder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA, 1);
        builder.addMaterialType(MaterialType.SEQUENCE);

        SearchResult result = sequenceSearchService.searchSequences(builder.build());

        Assert.assertEquals(1, result.getErrorMessages().size());
        Assert.assertEquals(0, result.getAllFoundObjects().size());
        Assert.assertEquals(0, entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER).size());
    }

    @Test
    public void test004_errorAtSqlBuild() {
        SearchResult result = sequenceSearchService.searchSequences(null);

        Assert.assertEquals(1, result.getErrorMessages().size());
        Assert.assertEquals(0, result.getAllFoundObjects().size());
        Assert.assertEquals(0, entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER).size());
    }

    @Test
    public void test005_error404withMessage() {
        mock.setBehaviour((e) -> {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
        });
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        builder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA, 1);
        builder.addMaterialType(MaterialType.SEQUENCE);

        SearchResult result = sequenceSearchService.searchSequences(builder.build());

        Assert.assertEquals(1, result.getErrorMessages().size());
        Assert.assertEquals(0, result.getAllFoundObjects().size());
        Assert.assertEquals(0, entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER).size());
    }

    @Test
    public void test006_fastaResultsIsNull() {
        mock.setBehaviour((e) -> {
            FastaSearchResult result = new FastaSearchResult();
            result.setProgramOutput("def");
            result.setResults(null);

            return Response.ok(result).build();
        });
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        builder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA, 1);
        builder.addMaterialType(MaterialType.SEQUENCE);

        SearchResult result = sequenceSearchService.searchSequences(builder.build());

        Assert.assertEquals(0, result.getErrorMessages().size());
        Assert.assertEquals(0, result.getAllFoundObjects().size());
        Assert.assertEquals(0, entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER).size());
    }

    private void createAndSaveSequence() {
        SequenceData data = SequenceData.builder()
                .sequenceString("abc")
                .sequenceType(PROTEIN)
                .circular(true)
                .annotations("def")
                .build();
        sequence = new Sequence(null, new ArrayList<>(), publicAclId, new HazardInformation(), new StorageInformation(), data);
        sequence.setProjectId(project.getId());
        materialService.saveMaterialToDB(sequence, publicAclId, new HashMap<>(), publicUser);
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("SequenceSearchServiceTest.war");
        deployment = UserBeanDeployment.add(deployment);
        deployment = MaterialDeployment.add(PrintBeanDeployment.add(deployment));
        deployment.addClass(SequenceSearchService.class);
        deployment.deleteClass(FastaRESTSearchService.class);
        deployment.addClass(FastaRESTSearchServiceMock.class);

        return deployment;
    }
}
