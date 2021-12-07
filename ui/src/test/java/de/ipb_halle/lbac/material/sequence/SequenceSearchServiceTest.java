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
package de.ipb_halle.lbac.material.sequence;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import static de.ipb_halle.lbac.material.sequence.SequenceType.PROTEIN;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.search.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.ws.rs.core.Response;
import org.junit.Assert;

/**
 *
 * @author flange
 */
@RunWith(Arquillian.class)
public class SequenceSearchServiceTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private MaterialService materialService;

    @Inject
    SequenceSearchService sequenceSearchService;

    @Inject
    private FastaRESTSearchServiceMock mock;

    Project project;
    int publicAclId;

    private Sequence sequence;

    @Before
    public void init() {

        materialService.setStructureInformationSaver(
                new StructureInformationSaverMock());
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        project.setOwner(publicUser);
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveProjectToDb(project);
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();

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
    }

    @Test
    public void test003_errorAtSqlBuild() {
        mock.setBehaviour((e) -> {
            FastaResult fastaResult = new FastaResult();
            fastaResult.setQueryAlignmentLine("abc");
            fastaResult.setSubjectSequenceName(String.valueOf(sequence.getId()));
            FastaSearchResult result = new FastaSearchResult();
            result.setProgramOutput("def");
            result.setResults(Arrays.asList(fastaResult));

            return Response.ok(result).build();
        });

        SearchResult result = sequenceSearchService.searchSequences(null);
        Assert.assertEquals(1, result.getErrorMessages().size());
        Assert.assertEquals(0, result.getAllFoundObjects().size());
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
