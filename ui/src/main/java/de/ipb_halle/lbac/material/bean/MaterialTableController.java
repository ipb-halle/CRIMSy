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

import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.service.MaterialService;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class MaterialTableController {

    private MaterialService materialService;
    private int firstResult;
    private int chunkSize;

    private List<Material> shownMaterials;

    public MaterialTableController(MaterialService materialService) {
        this.materialService = materialService;
        firstResult = 0;
        chunkSize = 10;
    }

    public void reloadShownMaterial(User user, Map<String, Object> cmap) {
        shownMaterials = materialService.getReadableMaterials(user, cmap, firstResult, chunkSize);
    }

    public List<Material> getShownMaterials() {
        return shownMaterials;
    }
    
    
}
