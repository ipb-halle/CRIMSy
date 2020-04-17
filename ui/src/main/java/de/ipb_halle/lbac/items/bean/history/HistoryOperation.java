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
package de.ipb_halle.lbac.items.bean.history;

import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.bean.ItemState;

/**
 *
 * @author fmauz
 */
public class HistoryOperation {

    private ItemState itemState;

    public HistoryOperation(ItemState itemState) {
        this.itemState = itemState;
    }

    public void applyNextNegativeDifference() {
        itemState.setCurrentHistoryDate(itemState.getPreviousKey(itemState.getCurrentHistoryDate()));
        ItemHistory history = itemState.getEditedItem().getHistory().get(itemState.getCurrentHistoryDate());
        if (history.getAmountOld() != null) {
            itemState.getEditedItem().setAmount(history.getAmountOld());
        }
        if (history.getConcentrationOld() != null) {
            itemState.getEditedItem().setConcentration(history.getConcentrationOld());
        }
        if (history.getDescriptionOld() != null) {
            itemState.getEditedItem().setDescription(history.getDescriptionOld());
        }
        if (history.getOwnerOld() != null) {
            itemState.getEditedItem().setOwner(history.getOwnerOld());
        }
        if (history.getProjectOld() != null) {
            itemState.getEditedItem().setProject(history.getProjectOld());
        }
        if (history.getPurityOld() != null) {
            itemState.getEditedItem().setPurity(history.getPurityOld());
        }
    }

    public void applyNextPositiveDifference() {
        ItemHistory history = itemState.getEditedItem().getHistory().get(itemState.getCurrentHistoryDate());
        if (history.getAmountNew() != null) {
            itemState.getEditedItem().setAmount(history.getAmountNew());
        }
        if (history.getConcentrationNew() != null) {
            itemState.getEditedItem().setConcentration(history.getConcentrationNew());
        }
        if (history.getDescriptionNew() != null) {
            itemState.getEditedItem().setDescription(history.getDescriptionNew());
        }
        if (history.getOwnerNew() != null) {
            itemState.getEditedItem().setOwner(history.getOwnerNew());
        }
        if (history.getProjectNew() != null) {
            itemState.getEditedItem().setProject(history.getProjectNew());
        }
        if (history.getPurityNew() != null) {
            itemState.getEditedItem().setPurity(history.getPurityNew());
        }
        itemState.setCurrentHistoryDate(itemState.getFollowingKey(itemState.getCurrentHistoryDate()));
    }

}
