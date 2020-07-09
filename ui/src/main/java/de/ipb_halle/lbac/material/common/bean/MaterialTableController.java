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

import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialTableController implements TableController {

    private String noMaterialsFoundPattern = "No materials with active filters found";
    private String MaterialsFoundPattern = "%d - %d of %d items shown";
    private MaterialService materialService;

    private int maxResults;
    private DataTableNavigationController tableController;

    private List<Material> shownMaterials;
    private Map<String, Object> lastcmap = new HashMap<>();
    private User lastUser;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialTableController(MaterialService materialService) {
        this.materialService = materialService;
        tableController = new DataTableNavigationController(
                this,
                noMaterialsFoundPattern,
                MaterialsFoundPattern,
                maxResults
        );
    }

    @Override
    public void reloadDataTableItems() {
        shownMaterials = materialService.getReadableMaterials(
                lastUser,
                lastcmap,
                tableController.getFirstResult(),
                tableController.getCHUNK_SIZE());
    }

    public void reloadShownMaterial(User user, Map<String, Object> cmap) {
        lastcmap = cmap;
        lastUser = user;
        shownMaterials = materialService.getReadableMaterials(
                user,
                cmap,
                tableController.getFirstResult(),
                tableController.getCHUNK_SIZE());
        maxResults = materialService.loadMaterialAmount(user, cmap);
        tableController.setMaxResults(maxResults);

    }

    public List<Material> getShownMaterials() {
        return shownMaterials;
    }

    public DataTableNavigationController getTableController() {
        return tableController;
    }

    @Override
    public void setLastUser(User u) {
        this.lastUser = u;
    }

}
