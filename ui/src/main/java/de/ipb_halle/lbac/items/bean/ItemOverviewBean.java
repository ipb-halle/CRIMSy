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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.MemberService;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ItemOverviewBean implements Serializable{

    private final int PAGE_SIZE = 10;

    private String materialSearchName;
    private String itemSearchId;
    private String searchUserName;
    private String searchProject;
    private String searchLocation;
    private String searchDescription;

    private int firstResult;
    private int itemAmount;
    @Inject
    private ItemService itemService;

    @Inject
    private ItemBean itemBean;

    @Inject
    private MemberService memberService;

    @Inject
    private Navigator navigator;

    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;
    @Inject
    private ContainerService containerService;

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private List<Item> items;
    private User currentUser;

    public List<Item> getItems() {
        return items;
    }

    public void reloadItems() {
        Map<String, String> cmap = new HashMap<>();
        if (materialSearchName != null && !materialSearchName.isEmpty()) {
            cmap.put("MATERIAL_NAME", materialSearchName);
        }
        if (itemSearchId != null && !itemSearchId.isEmpty()) {
            cmap.put("ITEM_ID", itemSearchId);
        }
        if (searchUserName != null && !searchUserName.isEmpty()) {
            cmap.put("OWNER_NAME", searchUserName);
        }
        if (searchProject != null && !searchProject.isEmpty()) {
            cmap.put("PROJECT_NAME", searchProject);
        }
        if (searchDescription != null && !searchDescription.isEmpty()) {
            cmap.put("DESCRIPTION", searchDescription);
        }
        if (searchLocation != null && !searchLocation.isEmpty()) {
            cmap.put("LOCATION_NAME", searchLocation);
        }

        itemAmount = itemService.getItemAmount(currentUser, cmap);
        items = itemService.loadItems(currentUser, cmap, firstResult, PAGE_SIZE);
    }

    public void actionClearSearchFilter() {
        materialSearchName = null;
        itemSearchId = null;
        searchUserName = null;
        searchProject = null;
        searchLocation = null;
        searchDescription = null;
    }

    public void actionApplySearchFilter() {
        reloadItems();
    }

    public void actionStartItemEdit(Item i) {
        itemBean.actionStartItemEdit(i);
        navigator.navigate("/item/itemEdit");
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        firstResult = 0;
        reloadItems();
    }

    public String getAmountString(Item i) {
        return i.getAmount() + " " + i.getUnit();
    }

    public String getMaterialName(Item i) {
        String back = "";
        for (MaterialName mn : i.getMaterial().getNames()) {
            back += mn.getValue() + "<br/>";
        }
        return back;
    }

    public String getOwnerString(Item i) {
        String back = "";
        if (i.getProject() != null) {
            back = i.getProject().getName();
        }
        back += "/" + i.getOwner().getName();
        return back;
    }

    public String getLocationOfItem(Item i) {
        return i.getNestedLocation();
    }

    public String getDatesOfItem(Item i) {
        String back = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (i.getcTime() != null) {
            back = "created     : " + sdf.format(i.getcTime()) + "<br/>";
        }
        if (!i.getHistory().isEmpty()) {
            back += "modified: - " + sdf.format(i.getHistory().firstKey());
        }
        return back;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public void actionLastItems() {

        firstResult -= PAGE_SIZE;
        firstResult = Math.max(0, firstResult);
        reloadItems();
    }

    public void actionNextItems() {
        firstResult += PAGE_SIZE;
        firstResult = Math.min(firstResult, itemAmount - PAGE_SIZE);
        reloadItems();
    }

    public void actionFirstItems() {
        firstResult = 0;
        reloadItems();
    }

    public void actionEndItems() {
        firstResult = itemAmount - PAGE_SIZE;
        firstResult = Math.max(0, firstResult);
        reloadItems();
    }

    public boolean isBackDeactivated() {
        return firstResult == 0;
    }

    public boolean isForwardDeactivated() {
        return (itemAmount - firstResult) < PAGE_SIZE;
    }

    public String getItemNavigationInfo() {
        int leftBorder = firstResult + 1;
        int rightBorder = (int) Math.min(PAGE_SIZE + firstResult, itemAmount);
        if (itemAmount > 0) {
            return String.format("%d - %d of %d items shown", leftBorder, rightBorder, itemAmount);
        } else {
            return "no items with active filters found";
        }
    }

    public String getMaterialSearchName() {
        return materialSearchName;
    }

    public void setMaterialSearchName(String materialSearchName) {
        this.materialSearchName = materialSearchName;
    }

    public List<String> getSimilarMaterialNames(String input) {
        List<String> names = materialService.getSimilarMaterialNames(input, currentUser);
        return names;
    }

    public List<String> getSimilarProjectNames(String input) {
        return projectService.getSimilarProjectNames(input, currentUser);
    }

    public List<String> getSimilarUserNames(String input) {
        return new ArrayList<>(memberService.loadSimilarUserNames(input));
    }

    public List<String> getSimilarContainerNames(String input) {
        return new ArrayList<>(containerService.getSimilarContainerNames(input, currentUser));
    }

    public String getItemSearchId() {
        return itemSearchId;
    }

    public void setItemSearchId(String itemSearchId) {
        this.itemSearchId = itemSearchId;
    }

    public String getSearchUserName() {
        return searchUserName;
    }

    public void setSearchUserName(String searchUserName) {
        this.searchUserName = searchUserName;
    }

    public String getSearchProject() {
        return searchProject;
    }

    public void setSearchProject(String searchProject) {
        this.searchProject = searchProject;
    }

    public String getSearchLocation() {
        return searchLocation;
    }

    public void setSearchLocation(String searchLocation) {
        this.searchLocation = searchLocation;
    }

    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

}
