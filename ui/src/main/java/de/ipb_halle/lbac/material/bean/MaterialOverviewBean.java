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
package de.ipb_halle.lbac.material.bean;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.navigation.Navigator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class MaterialOverviewBean implements Serializable {

    private List<Material> materials = new ArrayList<>();
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private MaterialBean materialEditBean;

    @Inject
    private MaterialService materialService;

    @Inject
    private Navigator navigator;

    @Inject
    private UserBean userBean;
    
    @Inject ItemBean itemBean;

    @PostConstruct
    public void init() {

    }

    public List<Material> getReadableMaterials() {

        return materialService.getReadableMaterials();
    }

    public boolean isNotAllowed(Material m, String action) {
        return false;
    }

    public void actionCreateNewMaterial() {
        materialEditBean.startMaterialCreation();
        navigator.navigate("material/materialsEdit");
    }

    public void actionEditMaterial(Material m) {
        try {
            m.setHistory(materialService.loadHistoryOfMaterial(m.getId()));

            materialEditBean.startMaterialEdit(m);
        } catch (Exception e) {
            logger.error(e);
        }
        navigator.navigate("material/materialsEdit");
    }

    public void actionDeactivateMaterial(Material m) {
        materialService.deactivateMaterial(
                m.getId(),
                userBean.getCurrentAccount());
    }
    
    public void actionCreateNewItem(Material m){
        itemBean.actionStartItemCreation(m);
         navigator.navigate("item/itemEdit");
    }

}
