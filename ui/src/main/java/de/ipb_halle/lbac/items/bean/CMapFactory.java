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
package de.ipb_halle.lbac.items.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class CMapFactory implements Serializable {

    private final static long serialVersionUID = 1L;

    public Map<String, String> createCmap(SearchMaskValues searchMaskValues) {
        Map<String, String> cmap = new HashMap<>();
        if (searchMaskValues.getMaterialName() != null && !searchMaskValues.getMaterialName().isEmpty()) {
            cmap.put("MATERIAL_NAME", searchMaskValues.getMaterialName());
        }
        if (searchMaskValues.getItemId() != null && !searchMaskValues.getItemId().isEmpty()) {
            cmap.put("ITEM_ID", searchMaskValues.getItemId());
        }
        if (searchMaskValues.getUserName() != null && !searchMaskValues.getUserName().isEmpty()) {
            cmap.put("OWNER_NAME", searchMaskValues.getUserName());
        }
        if (searchMaskValues.getProjectName() != null && !searchMaskValues.getProjectName().isEmpty()) {
            cmap.put("PROJECT_NAME", searchMaskValues.getProjectName());
        }
        if (searchMaskValues.getDescription() != null && !searchMaskValues.getDescription().isEmpty()) {
            cmap.put("DESCRIPTION", "%"+searchMaskValues.getDescription()+"%");
            
        }
        if (searchMaskValues.getLocation() != null && !searchMaskValues.getLocation().isEmpty()) {
            cmap.put("LOCATION_NAME", "%"+searchMaskValues.getLocation()+"%");
        }
        return cmap;
    }
}
