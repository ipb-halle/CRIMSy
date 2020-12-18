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

import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.MemberService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
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
public class MaterialOverviewBean implements Serializable, ACObjectBean {

    private List<Material> materials = new ArrayList<>();
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private MaterialTableController tableController;
    private ACObjectController acObjectController;
    @Inject
    private MaterialBean materialEditBean;

    @Inject
    private MaterialService materialService;

    @Inject
    private Navigator navigator;

    private User currentUser;

    @Inject
    private ProjectService projectService;

    @Inject
    private MemberService memberService;

    private MaterialSearchMaskController searchController;
    private Material materialInFocus;

    @Inject
    ItemBean itemBean;

    @PostConstruct
    public void init() {
        tableController = new MaterialTableController(materialService);
        this.searchController = new MaterialSearchMaskController(
                this,
                tableController,
                materialService,
                projectService,
                memberService,
                Arrays.asList(
                        MaterialType.COMPOSITION,
                        MaterialType.BIOMATERIAL,
                        MaterialType.CONSUMABLE,
                        MaterialType.SEQUENCE,
                        MaterialType.STRUCTURE));

    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        tableController.setLastUser(currentUser);
        searchController.clearInputFields();

    }

    public MaterialTableController getTableController() {
        return tableController;
    }

    public String getWrappedNames(Material material, int maxNamesShown) {
        String back = "";
        for (int i = 0; i < Math.min(material.getNames().size(), maxNamesShown); i++) {
            back += material.getNames().get(i).getValue() + "<br>";
        }
        if (material.getNames().size() > maxNamesShown) {
            back += "...";
        }

        if (back.endsWith("<br>")) {
            back = back.substring(0, back.length() - "<br>".length());
        }
        return back;
    }

    public List<Material> getReadableMaterials() {
        return tableController.getShownMaterials();

    }

    public boolean isDetailSubComponentVisisble(String type, Material mat) {
        return MaterialType.valueOf(type) == mat.getType();
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
                currentUser);
    }

    public void actionCreateNewItem(Material m) {
        itemBean.actionStartItemCreation(m);
        navigator.navigate("item/itemEdit");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public MaterialSearchMaskController getSearchController() {
        return searchController;
    }

    @Override
    public void applyAclChanges() {
        materialService.updateMaterialAcList(materialInFocus);
        searchController.actionStartMaterialSearch();
        materialInFocus = null;

    }

    @Override
    public void cancelAclChanges() {

        materialInFocus = null;
    }

    @Override
    public void actionStartAclChange(ACObject aco) {
        materialInFocus = (Material) aco;
        acObjectController = new ACObjectController(materialInFocus, memberService.loadGroups(new HashMap<>()), this, materialInFocus.getFirstName());
    }

    @Override
    public ACObjectController getAcObjectController() {
        return acObjectController;
    }

}
