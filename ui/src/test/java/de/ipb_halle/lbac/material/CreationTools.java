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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.admission.MemberService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author fmauz
 */
public class CreationTools {

    private MemberService memberService;

    private ProjectService projectService;

    private String hazardStatement;
    private String precautionaryStatement;
    private String storageClassRemark;

    public CreationTools(String hazardStatement,
            String precautionaryStatement,
            String storageClassRemark,
            MemberService memberService,
            ProjectService projectService) {
        this.hazardStatement = hazardStatement;
        this.precautionaryStatement = precautionaryStatement;
        this.storageClassRemark = storageClassRemark;
        this.projectService = projectService;
        this.memberService = memberService;

    }

    public Structure createEmptyStructure(int projectId) {
        List<MaterialName> names = new ArrayList<>();
        HazardInformation hazards = new HazardInformation();
        StorageClassInformation storage = new StorageClassInformation();
        storage.setStorageClass(new StorageClass(1, "xyz"));
        double molarMass = 10;
        double exactMolarMass = 20;
        int id = 0;
        Structure s = new Structure(
                "",
                molarMass,
                exactMolarMass,
                id,
                names,
                projectId,
                hazards,
                storage,
                new Molecule("xcy", 1));

        s.setCreationTime(new Date());
        return s;
    }

    public Project createProject() {
        Project p = new Project(ProjectType.BIOCHEMICAL_PROJECT, "biochemical-test-project");
        p.setBudget(1000d);
        p.setDescription("Description of biochemical test project");

        Group g = memberService.loadGroupById(GlobalAdmissionContext.PUBLIC_GROUP_ID);
        User u = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

        ACList projectAcList = new ACList();

        projectAcList.addACE(u, new ACPermission[]{
            ACPermission.permREAD,
            ACPermission.permCHOWN,
            ACPermission.permCREATE,
            ACPermission.permDELETE,
            ACPermission.permEDIT,
            ACPermission.permGRANT,
            ACPermission.permSUPER});

        projectAcList.addACE(g, new ACPermission[]{
            ACPermission.permREAD});

        p.setOwner(u);
        p.setUserGroups(projectAcList);

        p.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.STORAGE_CLASSES, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.STRUCTURE_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.HAZARD_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.INDEX, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.TAXONOMY, projectAcList);

        projectService.saveProjectToDb(p);
        return p;
    }

    public Structure createStructure(Project p) {
        Structure m = createEmptyStructure(p.getId());

        HazardInformation hazardInfos = new HazardInformation();
        hazardInfos.setAttention(true);
        hazardInfos.setIrritant(true);
        hazardInfos.setHazardStatements(hazardStatement);
        hazardInfos.setPrecautionaryStatements(precautionaryStatement);
        m.setHazards(hazardInfos);

        StorageClassInformation storageInfos = new StorageClassInformation();
        storageInfos.setKeepCool(true);
        storageInfos.setKeepFrozen(true);
        storageInfos.setStorageClass(new StorageClass(1, "1"));
        storageInfos.setRemarks(storageClassRemark);
        m.setStorageInformation(storageInfos);

        //Initialisieng the material detail rights
        Map<MaterialDetailType, ACList> detailTemplates = new HashMap<>();
        detailTemplates.put(MaterialDetailType.COMMON_INFORMATION, p.getUserGroups());
        detailTemplates.put(MaterialDetailType.INDEX, p.getUserGroups());
        detailTemplates.put(MaterialDetailType.HAZARD_INFORMATION, p.getUserGroups());
        detailTemplates.put(MaterialDetailType.STORAGE_CLASSES, p.getUserGroups());
        detailTemplates.put(MaterialDetailType.STRUCTURE_INFORMATION, p.getUserGroups());
        p.setDetailTemplates(detailTemplates);

        m.getIndices().add(new IndexEntry(2, "Gestis Number", null));
        m.getIndices().add(new IndexEntry(3, "cas Number", null));
        m.getIndices().add(new IndexEntry(4, "crs Number", null));

        m.getNames().add(new MaterialName("Test-Struktur", "de", 0));
        m.getNames().add(new MaterialName("Test-Structure", "en", 1));

        return m;
    }
}
