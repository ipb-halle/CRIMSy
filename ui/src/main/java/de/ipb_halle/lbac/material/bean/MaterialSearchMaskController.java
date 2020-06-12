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
package de.ipb_halle.lbac.material.bean;

import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.subtype.MaterialType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class MaterialSearchMaskController {

    private String id;
    private Map<Integer, String> indexTypes;
    private String indexTypeName;
    private List<String> indexTypeNames;
    private boolean isIndexActive;
    private boolean isMaterialTypeActive;
    private MaterialTableController tableController;
    private MaterialType materialType;
    private MaterialOverviewBean overviewBean;
    private String name;
    private String projectName;
    private String userName;
    private MaterialService materialService;

    public MaterialSearchMaskController(
            MaterialOverviewBean overviewBean,
            MaterialTableController tableController,
            Map<Integer, String> indexCategories,
            MaterialService materialService) {
        this.indexTypes = indexCategories;
        this.indexTypeNames = new ArrayList<>(indexCategories.values());
        this.overviewBean = overviewBean;
        this.tableController = tableController;
        this.materialService = materialService;

    }

    public void actionClearSearchFilter() {
        id = null;
        indexTypeName = null;
        isIndexActive = false;
        isMaterialTypeActive = false;
        name = null;
        projectName = null;
        userName = null;
        tableController.reloadShownMaterial(overviewBean.getCurrentUser(), generateCmap());

    }

    public void actionStartMaterialSearch() {
        tableController.reloadShownMaterial(overviewBean.getCurrentUser(), generateCmap());
    }

    private Map<String, Object> generateCmap() {
        Map<String, Object> cmap = new HashMap<>();
        if (name != null && !name.trim().isEmpty()) {
            cmap.put("NAME", name);
        }
        return cmap;
    }

    public List<String> getSimmilarMaterialNames(String pattern) {
        return materialService.getSimilarMaterialNames(pattern, overviewBean.getCurrentUser());
    }

    public List<String> getSimmilarUserNames(String pattern) {
        return new ArrayList<>();
    }

    public List<String> getSimmilarProjectNames(String pattern) {
        return new ArrayList<>();
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

    public List<String> getIndexTypeNames() {
        return indexTypeNames;
    }

    public String getIndexTypeName() {
        return indexTypeName;
    }

    public MaterialType getMaterialType() {
        return materialType;
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
