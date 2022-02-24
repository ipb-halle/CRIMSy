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
package de.ipb_halle.lbac.items.bean.history;

import de.ipb_halle.lbac.items.ItemDifference;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.ItemPositionHistoryList;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import de.ipb_halle.lbac.items.bean.ContainerController;
import de.ipb_halle.lbac.items.bean.ItemState;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class HistoryOperation {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private ItemState itemState;
    private ContainerController containerController;

    public HistoryOperation(
            ItemState itemState,
            ContainerController containerController) {
        this.itemState = itemState;
        this.containerController = containerController;
    }

    public void applyNextNegativeDifference() {
        if (!itemState.isStartingHistoryItem()) {
            itemState.setCurrentHistoryDate(itemState.getPreviousKey(itemState.getCurrentHistoryDate()));

            for (ItemDifference diff : orderDifferences(itemState.getEditedItem().getHistory().get(itemState.getCurrentHistoryDate()))) {
                if (diff instanceof ItemHistory) {
                    applyNegativeItemHistory((ItemHistory) diff);
                }
                if (diff instanceof ItemPositionHistoryList) {
                    applyNegativePositionDiff((ItemPositionHistoryList) diff);
                }
            }
        }
    }

    public void applyNextPositiveDifference() {
        if (!itemState.isLastHistoryItem()) {
            for (ItemDifference diff : orderDifferences(itemState.getEditedItem().getHistory().get(itemState.getCurrentHistoryDate()))) {
                if (diff instanceof ItemHistory) {
                    applyPositiveItemHistory((ItemHistory) diff);
                }
                if (diff instanceof ItemPositionHistoryList) {
                    applyPositivePositionDiff((ItemPositionHistoryList) diff);
                }
            }
            itemState.setCurrentHistoryDate(itemState.getFollowingKey(itemState.getCurrentHistoryDate()));
        }
    }

    private void applyPositivePositionDiff(ItemPositionHistoryList diffs) {
//        containerController.removeItemFromPosition();
//        for (ItemPositionsHistory diff : diffs.getPositionAdds()) {
//            containerController.setItemAtPosition(diff.getRowNew(), diff.getColNew());
//        }
    }

    private void applyNegativePositionDiff(ItemPositionHistoryList diffs) {
//        containerController.removeItemFromPosition();
//        for (ItemPositionsHistory diff : diffs.getPositionRemoves()) {
//            containerController.setItemAtPosition(diff.getRowOld(), diff.getColOld());
//        }
    }

    private void applyPositiveItemHistory(ItemHistory history) {
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
        containerController.actionChangeContainer(history.getParentContainerNew());

    }

    private void applyNegativeItemHistory(ItemHistory history) {
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
        containerController.actionChangeContainer(history.getParentContainerOld());
    }

    private List<ItemDifference> orderDifferences(List<ItemDifference> unorderedDiffs) {
        List<ItemDifference> orderedDiffs = new ArrayList<>();
        for (ItemDifference d : unorderedDiffs) {
            if (d instanceof ItemHistory) {
                orderedDiffs.add(d);
            }
        }
        for (ItemDifference d : unorderedDiffs) {
            if (!(d instanceof ItemHistory)) {
                orderedDiffs.add(d);
            }
        }
        return orderedDiffs;
    }

}
