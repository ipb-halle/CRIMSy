/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.bean.save;

import de.ipb_halle.lbac.material.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.structure.MoleculeStructureModel;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.Tissue;
import de.ipb_halle.lbac.project.Project;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialCreationSaver {

    protected MoleculeService moleculeService;
    protected MaterialNameBean materialNameBean;
    protected MaterialService materialService;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialCreationSaver(
            MoleculeService moleculeService,
            MaterialNameBean materialNameBean,
            MaterialService materialService) {
        this.moleculeService = moleculeService;
        this.materialNameBean = materialNameBean;
        this.materialService = materialService;
    }

    public void saveNewStructure(
            boolean calculateFormulaAndMassesByDb,
            MoleculeStructureModel moleculeModel,
            StructureInformation structureInfos,
            Project project,
            HazardInformation hazards,
            StorageClassInformation storageClassInformation,
            List<IndexEntry> indices
    ) {

        try {
            if (calculateFormulaAndMassesByDb && !moleculeModel.isEmptyMolecule(structureInfos.getStructureModel())) {
                structureInfos.setSumFormula(moleculeService.getMolFormulaOfMolecule(structureInfos.getStructureModel()));
                structureInfos.setExactMolarMass(moleculeService.getExactMolarMassOfMolecule(structureInfos.getStructureModel()));
                structureInfos.setMolarMass(moleculeService.getMolarMassOfMolecule(structureInfos.getStructureModel()));
            }

            if (moleculeModel.isEmptyMolecule(structureInfos.getStructureModel())) {
                structureInfos.setStructureModel(null);
            }
        } catch (Exception e) {
            logger.error("Molecule model is not valide: " + structureInfos.getStructureModel());
            structureInfos.setStructureModel(null);
        }

        Structure struc = new Structure(
                structureInfos.getSumFormula(),
                structureInfos.getMolarMass(),
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
}
