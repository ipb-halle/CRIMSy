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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.Tissue;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.util.chemistry.Calculator;
import java.io.Serializable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialCreationSaver implements Serializable {

    protected MaterialNameBean materialNameBean;
    protected MaterialService materialService;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialCreationSaver(
            MaterialNameBean materialNameBean,
            MaterialService materialService) {
        this.materialNameBean = materialNameBean;
        this.materialService = materialService;
    }

    public void saveNewBioMaterial(
            Project project,
            List<MaterialName> names,
            Taxonomy taxonomy,
            Tissue tissue,
            HazardInformation hazards,
            StorageClassInformation storageInfos) {
        if (storageInfos == null) {
            storageInfos = new StorageClassInformation();
        }
        if (storageInfos.getStorageClass() == null) {
            storageInfos.setStorageClass(storageInfos.getPossibleStorageClasses().get(0));
        }
        if (hazards == null) {
            hazards = new HazardInformation();
        }

        BioMaterial bm = new BioMaterial(0, names, project.getId(), hazards, storageInfos, taxonomy, tissue);
        materialService.saveMaterialToDB(bm, project.getUserGroups().getId(), project.getDetailTemplates());

    }

    public void saveNewStructure(
            boolean autoCalc,
            StructureInformation structureInfos,
            Project project,
            HazardInformation hazards,
            StorageClassInformation storageClassInformation,
            List<IndexEntry> indices
    ) {
        try {
            Molecule mol = new Molecule(structureInfos.getStructureModel(), -1);
            if (mol.isEmptyMolecule()) {
                structureInfos.setStructureModel(null);
            } else {
                if (autoCalc) {
                    Calculator calc = new Calculator();
                    structureInfos = calc.calculate(structureInfos);
                }
            }

        } catch (Exception e) {
            logger.error("Molecule model is not valide: " + structureInfos.getStructureModel(), e);
            structureInfos.setStructureModel(null);
        }
        Structure struc = new Structure(
                structureInfos.getSumFormula(),
                structureInfos.getAverageMolarMass(),
                structureInfos.getExactMolarMass(),
                -1,
                materialNameBean.getNames(),
                project.getId(),
                hazards,
                storageClassInformation,
                new Molecule(structureInfos.getStructureModel(), 0));

        struc.getIndices().addAll(indices);

        materialService.saveMaterialToDB(
                struc,
                project.getUserGroups().getId(),
                project.getDetailTemplates()
        );
    }

}
