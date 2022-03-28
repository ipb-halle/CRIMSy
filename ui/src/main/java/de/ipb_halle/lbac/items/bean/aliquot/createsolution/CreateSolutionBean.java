/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean.aliquot.createsolution;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemUtils;
import de.ipb_halle.lbac.items.bean.aliquot.createsolution.consumepartofitem.ConsumePartOfItemStrategyController;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.util.units.Quality;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * 
 * @author flange
 */
@SessionScoped
@Named
public class CreateSolutionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private Navigator navigator;

    @Inject
    private ConsumePartOfItemStrategyController consumePartOfItemStrategyController;

    private Item parentItem; // r
    private Quantity molarMassFromParentItem; // r

    @PostConstruct
    public void init() {
    }

    /*
     * Actions
     */
    public void actionStartCreateSolution(Item parentItem) {
        this.parentItem = parentItem;
        molarMassFromParentItem = ItemUtils.molarMassFromItem(parentItem);
        consumePartOfItemStrategyController.init(parentItem);
        navigator.navigate("/item/aliquot/createSolution/createSolution");
    }

    public void actionCancel() {
        navigator.navigate("item/items");
    }

    /*
     * Getters with logic
     */
    public boolean isItemNotSoluble(Item item) {
        return !isItemSoluble(item);
    }

    private boolean isItemSoluble(Item item) {
        boolean itemIsNotSolved = (item.getConcentration() == null) && (item.getConcentrationUnit() == null)
                && (item.getSolvent() == null);
        boolean itemHasMassUnit = false;
        Unit unit = item.getUnit();
        if (unit != null) {
            itemHasMassUnit = Quality.MASS.equals(unit.getQuality());
        }

        return itemIsNotSolved && itemHasMassUnit;
    }

    public boolean isParentItemHasMolarMass() {
        return molarMassFromParentItem != null;
    }

    /*
     * Getters/setters
     */
    public Item getParentItem() {
        return parentItem;
    }

    public Quantity getMolarMassFromParentItem() {
        return molarMassFromParentItem;
    }

    public ConsumePartOfItemStrategyController getConsumePartOfItemStrategyController() {
        return consumePartOfItemStrategyController;
    }
}