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
import java.util.HashMap;
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

    Project project;
    int publicAclId;

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
        SequenceData data = SequenceData.builder()
                .sequenceString("abc")
                .sequenceType(PROTEIN)
                .circular(true)
                .annotations("def")
                .build();
        Sequence seq = new Sequence(null, new ArrayList<>(), publicAclId, new HazardInformation(), new StorageInformation(), data);
        seq.setProjectId(project.getId());
        materialService.saveMaterialToDB(seq, publicAclId, new HashMap<>(), publicUser);
        FastaRESTSearchServiceMock.sequenceId = seq.getId();

        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        builder.setSequenceInformation("AAA", "DNA", SequenceType.DNA, 1);

        SearchResult result = sequenceSearchService.searchSequences(builder.build());

        Assert.assertEquals(1, result.getAllFoundObjects().size());

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("SequenceSearchServiceTest.war");
        deployment = UserBeanDeployment.add(deployment);
        deployment.addClass(SequenceSearchService.class);
        deployment.addClass(FastaRESTSearchServiceMock.class);

        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
