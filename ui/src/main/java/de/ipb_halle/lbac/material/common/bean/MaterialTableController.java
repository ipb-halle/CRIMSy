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
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialTableController {

    private MaterialService materialService;
    private int firstResult;
    private int chunkSize;
    private int maxResults;

    private List<Material> shownMaterials;
    private Map<String, Object> lastcmap;
    private User lastUser;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialTableController(MaterialService materialService) {
        this.materialService = materialService;
        firstResult = 0;
        chunkSize = 10;
    }

    public void reloadShownMaterial(User user, Map<String, Object> cmap) {
        lastcmap = cmap;
        lastUser = user;
        shownMaterials = materialService.getReadableMaterials(user, cmap, firstResult, chunkSize);
        maxResults = materialService.loadMaterialAmount(user, cmap);
    }

    public List<Material> getShownMaterials() {
        return shownMaterials;
    }

    public void actionFirstResult() {
        firstResult = 0;
        shownMaterials = materialService.getReadableMaterials(lastUser, lastcmap, firstResult, chunkSize);
    }

    public void actionLastResult() {
        firstResult = maxResults - chunkSize;
        firstResult = Math.max(0, firstResult);
        shownMaterials = materialService.getReadableMaterials(lastUser, lastcmap, firstResult, chunkSize);
    }

    public void actionPriorResults() {
        firstResult -= chunkSize;
        firstResult = Math.max(0, firstResult);
        shownMaterials = materialService.getReadableMaterials(lastUser, lastcmap, firstResult, chunkSize);
    }

    public void actionNextResults() {
        firstResult += chunkSize;
        firstResult = Math.min(firstResult, maxResults - chunkSize);
        shownMaterials = materialService.getReadableMaterials(lastUser, lastcmap, firstResult, chunkSize);
    }

    public boolean isPriorButtonGroupDisabled() {
        return firstResult == 0;
    }

    public boolean isNextButtonGroupDisabled() {
        return (maxResults - firstResult) <= chunkSize;
    }

    public String getNavigationInfos() {
        int leftBorder = firstResult + 1;
        int rightBorder = (int) Math.min(chunkSize + firstResult, maxResults);
        if (maxResults > 0) {
            return String.format("%d - %d of %d items shown", leftBorder, rightBorder, maxResults);
        } else {
            return "No materials with active filters found";
        }
    }

}
