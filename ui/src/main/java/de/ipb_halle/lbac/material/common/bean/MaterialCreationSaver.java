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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.Material;
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
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.consumable.Consumable;
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

    private static final long serialVersionUID = 1L;

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
            StorageInformation storageInfos,
            User owner) {
        if (storageInfos == null) {
            storageInfos = new StorageInformation();
        }
        if (hazards == null) {
            hazards = new HazardInformation();
        }

        BioMaterial bm = new BioMaterial(0, names, project.getId(), hazards, storageInfos, taxonomy, tissue);
        materialService.saveMaterialToDB(
                bm,
                project.getUserGroups().getId(),
                project.getDetailTemplates(),
                owner);

    }

    public void saveNewStructure(
            boolean autoCalc,
            StructureInformation structureInfos,
            Project project,
            HazardInformation hazards,
            StorageInformation storageClassInformation,
            List<IndexEntry> indices,
            User owner) {
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
                project.getDetailTemplates(),
                owner
        );

    }

    public void saveMaterialOverview(Material m, Project p, User owner) {
        if (m.getStorageInformation() == null) {
            m.setStorageInformation(new StorageInformation());
        }

        if (m.getHazards() == null) {
            m.setHazards(new HazardInformation());

        }
        materialService.saveMaterialToDB(m, p.getUserGroups().getId(), p.getDetailTemplates(), owner);
    }

    public void saveConsumable(
            Project project,
            HazardInformation hazards,
            StorageInformation storageClassInformation,
            List<IndexEntry> indices,
            User owner) {
        Consumable consumable = new Consumable(0, materialNameBean.getNames(), project.getId(), hazards, storageClassInformation);
        consumable.getIndices().addAll(indices);
        saveMaterialOverview(consumable, project, owner);
    }
    
    
    
    

}
