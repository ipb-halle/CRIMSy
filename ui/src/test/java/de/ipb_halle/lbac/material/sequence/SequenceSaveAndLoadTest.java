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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
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
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;

/**
 * 
 * @author flange
 */
@RunWith(Arquillian.class)
public class SequenceSaveAndLoadTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private MaterialService materialService;

    Project project;
    int publicAclId;

    @Before
    public void init() {
        cleanup();

        materialService.setStructureInformationSaver(
                new StructureInformationSaverMock());
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        project.setOwner(publicUser);
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveProjectToDb(project);
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();
    }

    @After
    public void finish() {
        cleanup();
    }

    private void cleanup() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);
    }

    @Test
    public void test001_saveAndLoadSequence() {
        SequenceData data = new SequenceData();
        data.setSequenceString("AGTTAAGCGTGA");
        data.setSequenceType(SequenceType.PROTEIN);
        data.setCircular(true);
        data.setAnnotations("some features in JSON format");
        Sequence sequence = new Sequence(null, new ArrayList<>(), project.getId(),
                new HazardInformation(), new StorageInformation(), data);

        materialService.saveMaterialToDB(sequence,
                GlobalAdmissionContext.getPublicReadACL().getId(),
                new HashMap<>(), publicUser.getId());
        Sequence loadedSequence = (Sequence) materialService.loadMaterialById(sequence.getId());

        assertEquals(sequence.getId(), loadedSequence.getId());
        assertEquals("AGTTAAGCGTGA", loadedSequence.getData().getSequenceString());
        assertEquals(SequenceType.PROTEIN, loadedSequence.getData().getSequenceType());
        assertTrue(loadedSequence.getData().isCircular());
        assertEquals("some features in JSON format", loadedSequence.getData().getAnnotations());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("SequenceSaveAndLoadTest.war");
        deployment = UserBeanDeployment.add(deployment);

        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}