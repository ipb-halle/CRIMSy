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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.biomaterial.BioMaterialDifference;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.bean.StorageInformationBuilder;
import de.ipb_halle.lbac.material.composition.CompositionDifference;
import de.ipb_halle.lbac.material.composition.CompositionHistoryController;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.project.ProjectBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides methods to apply one ore more differences bewetween two
 * states of a material.
 *
 * @author fmauz
 */
public class HistoryOperation implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected ProjectBean projectBean;
    protected MaterialEditState materialEditState;
    protected MaterialNameBean materialNameBean;
    protected MaterialIndexBean indexBean;
    protected StructureInformation structureInfos;
    protected MaterialCompositionBean compositionBean;
    protected StorageInformationBuilder storageInformationBuilder;
    protected TaxonomySelectionController taxonomySelectionController;
    protected List<HazardType> possibleHazards = new ArrayList<>();
    protected MaterialBean materialBean;

    /**
     * Initialises the functionality by neccessary services and the history
     * (collection of all differences at some timepoints) in the
     * materialEditState variable.
     *
     * @param materialBean
     * @param materialEditState
     * @param projectBean
     * @param nameBean
     * @param indexBean
     * @param structureInfos
     * @param storageInformationBuilder
     * @param taxonomySelectionController
     * @param possibleHazards
     * @param compositionBean
     */
    public HistoryOperation(
            MaterialBean materialBean,
            MaterialEditState materialEditState,
            ProjectBean projectBean,
            MaterialNameBean nameBean,
            MaterialIndexBean indexBean,
            StructureInformation structureInfos,
            StorageInformationBuilder storageInformationBuilder,
            TaxonomySelectionController taxonomySelectionController,
            List<HazardType> possibleHazards,
            MaterialCompositionBean compositionBean) {
        this.projectBean = projectBean;
        this.materialEditState = materialEditState;
        this.materialNameBean = nameBean;
        this.indexBean = indexBean;
        this.structureInfos = structureInfos;
        this.storageInformationBuilder = storageInformationBuilder;
        this.taxonomySelectionController = taxonomySelectionController;
        this.possibleHazards = possibleHazards;
        this.compositionBean = compositionBean;
        this.materialBean = materialBean;
    }

    /**
     * Based on the current timepoint applies all differences of the next
     * timepoint and set t as the current timepoint.
     *
     */
    public void applyNextPositiveDifference() {
        materialEditState.changeVersionDateToNext(materialEditState.getCurrentVersiondate());

        for (MaterialDifference difference : materialEditState.getMaterialBeforeEdit().getHistory().getChanges().get(materialEditState.getCurrentVersiondate())) {
            difference.createHistoryController(materialBean).applyPositiveDifference(difference);
        }

    }

    /**
     * Based on the current timepoint applies all differences(negative) of the
     * last timepoint and set t as the current timepoint.
     *
     */
    public void applyNextNegativeDifference() {

        for (MaterialDifference difference : materialEditState.getMaterialBeforeEdit().getHistory().getChanges().get(materialEditState.getCurrentVersiondate())) {
            difference.createHistoryController(materialBean).applyNegativeDifference(difference);
        }
        materialEditState.changeVersionDateToPrevious(materialEditState.getCurrentVersiondate());
    }

    /**
     * @return true if the currently shown material state is the original
     * version
     */
    public boolean isOriginalMaterial() {
        return materialEditState.getCurrentVersiondate() == null;
    }

}
