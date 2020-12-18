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

import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.structure.V2000;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.MemberService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialSearchMaskController implements Serializable {

    private String id;
    private String indexTypeName;
    private boolean isIndexActive;
    private boolean isMaterialTypeActive;
    private MaterialTableController tableController;
    private MaterialType materialType;
    private MaterialOverviewBean overviewBean;
    private String name;
    private String projectName;
    private String userName;
    private MaterialService materialService;
    private ProjectService projectService;
    private String index;
    private MemberService memberService;
    private List<MaterialType> materialTypes;
    private String molecule;
    private MaterialSearchMaskValues values;

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialSearchMaskController(
            MaterialOverviewBean overviewBean,
            MaterialTableController tableController,
            MaterialService materialService,
            ProjectService projectService,
            MemberService memberService,
            List<MaterialType> materialTypes) {
        this.overviewBean = overviewBean;
        this.tableController = tableController;
        this.materialService = materialService;
        this.projectService = projectService;
        this.memberService = memberService;
        this.materialTypes = materialTypes;

    }

    public List<MaterialType> getMaterialTypes() {
        return materialTypes;
    }

    public void setMaterialTypes(List<MaterialType> materialTypes) {
        this.materialTypes = materialTypes;
    }

    /**
     * ToDo: localize the material type names
     *
     * @param mt
     * @return
     */
    public String getLocalizedMaterialTypeName(MaterialType mt) {
        if (mt == null) {
            return "Alle";
        }
        return mt.toString();
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void actionClearSearchFilter() {
        clearInputFields();
        tableController.reloadShownMaterial(overviewBean.getCurrentUser(), getValues());
    }

    public void clearInputFields() {
        id = null;
        indexTypeName = null;
        isIndexActive = false;
        isMaterialTypeActive = false;
        name = null;
        projectName = null;
        userName = null;
        index = null;
        materialType = null;
        molecule = "";
    }

    public void actionStartMaterialSearch() {
        tableController.reloadShownMaterial(overviewBean.getCurrentUser(), getValues());
    }

    private MaterialSearchMaskValues getValues() {
        values = new MaterialSearchMaskValues();
        if (name != null && !name.trim().isEmpty()) {
            values.materialName = name;
        }
        if (id != null && !id.trim().isEmpty()) {
            values.id = Integer.parseInt(id);
        }
        if (projectName != null && !projectName.trim().isEmpty()) {
            values.projectName = projectName;
        }
        if (index != null && !index.trim().isEmpty()) {
            values.index = index;
        }
        if (userName != null && !userName.trim().isEmpty()) {
            values.userName = userName;
        }
        if (materialType != null) {
            values.type.add(materialType);
        } else {
            values.type.add(MaterialType.BIOMATERIAL);
            values.type.add(MaterialType.COMPOSITION);
            values.type.add(MaterialType.CONSUMABLE);
            values.type.add(MaterialType.SEQUENCE);
            values.type.add(MaterialType.STRUCTURE);
            values.type.add(MaterialType.TISSUE);
        }
        try {
            if (!new V2000().isEmptyMolecule(molecule)) {
                values.molecule = molecule;
            }
        } catch (Exception e) {
            logger.error("Could not parse molecule", e);
        }

        return values;
    }

    public List<String> getSimilarMaterialNames(String pattern) {
        return materialService.getSimilarMaterialNames(pattern, overviewBean.getCurrentUser());
    }

    public List<String> getSimilarUserNames(String pattern) {
        return new ArrayList<>(memberService.loadSimilarUserNames(pattern));
    }

    public List<String> getSimilarProjectNames(String pattern) {
        return projectService.getSimilarProjectNames(pattern, overviewBean.getCurrentUser());
    }

    public boolean isIsIndexActive() {
        return isIndexActive;
    }

    public boolean isIsMaterialTypeActive() {
        return isMaterialTypeActive;
    }

    public String getId() {
        return id;
    }

    public String getIndexTypeName() {
        return indexTypeName;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public String getMolecule() {
        return molecule;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getUserName() {
        return userName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIndexTypeName(String indexTypeName) {
        this.indexTypeName = indexTypeName;
    }

    public void setIsIndexActive(boolean isIndexActive) {
        this.isIndexActive = isIndexActive;
    }

    public void setIsMaterialTypeActive(boolean isMaterialTypeActive) {
        this.isMaterialTypeActive = isMaterialTypeActive;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
