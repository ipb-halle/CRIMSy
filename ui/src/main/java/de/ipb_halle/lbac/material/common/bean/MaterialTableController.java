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
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialTableController implements TableController {

    private static final long serialVersionUID = 1L;

    private String noMaterialsFoundPattern = "No materials with active filters found";
    private String MaterialsFoundPattern = "%d - %d of %d items shown";
    private MaterialService materialService;

    private int maxResults;
    private DataTableNavigationController tableController;

    private List<Material> shownMaterials;
    private MaterialSearchMaskValues lastValues;
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
        SearchResult result = materialService.loadReadableMaterials(createRequest());
        shownMaterials = extractAllMaterialsFromResponse(result);

    }

    public void reloadShownMaterial(User user, MaterialSearchMaskValues values) {
        lastValues = values;
        lastUser = user;
        reloadDataTableItems();
        maxResults = materialService.loadMaterialAmount(createRequest());
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

    private SearchRequest createRequest() {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(lastUser,
                tableController.getFirstResult(),
                tableController.getCHUNK_SIZE());
        builder.setSearchValues(lastValues);
        return builder.build();
    }

    private List<Material> extractAllMaterialsFromResponse(SearchResult response) {
        List<Material> materials = new ArrayList<>();
        for (NetObject no : response.getAllFoundObjects()) {
            materials.add((Material) no.getSearchable());
        }
        return materials;

    }

}
