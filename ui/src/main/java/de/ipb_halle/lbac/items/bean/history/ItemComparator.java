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
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.items.Item;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class ItemComparator {

    private Logger logger;

    public ItemComparator() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public ItemHistory compareItems(Item itemOld, Item itemNew, User actor) {
        boolean differenceFound = false;
        ItemHistory history = new ItemHistory();
        history.setAction("EDIT");
        history.setActor(actor);
        BigDecimal amountOld = new BigDecimal(itemOld.getAmount());
        BigDecimal amountNew = new BigDecimal(itemNew.getAmount());
        if (amountOld.compareTo(amountNew) != 0) {
            differenceFound = true;
            history.setAmountOld(amountOld.doubleValue());
            history.setAmountNew(amountNew.doubleValue());
        }
        if (!Objects.equals(itemOld.getConcentration(), itemNew.getConcentration())) {
            BigDecimal concentrationOld = new BigDecimal(itemOld.getConcentration());
            BigDecimal concentrationNew = new BigDecimal(itemNew.getConcentration());
            if (concentrationNew.compareTo(concentrationOld) != 0) {
                differenceFound = true;
                history.setConcentrationNew(concentrationNew.doubleValue());
                history.setConcentrationOld(concentrationOld.doubleValue());
            }
        }
        if (!Objects.equals(itemOld.getDescription(), itemNew.getDescription())) {
            differenceFound = true;
            history.setDescriptionNew(itemNew.getDescription());
            history.setDescriptionOld(itemOld.getDescription());
        }
        history.setItem(itemNew);
        history.setMdate(new Date());
        if (!itemOld.getOwner().getId().equals(itemNew.getOwner().getId())) {
            differenceFound = true;
            history.setOwnerNew(itemNew.getOwner());
            history.setOwnerOld(itemOld.getOwner());
        }
        if (itemNew.getProject().getId() != itemOld.getProject().getId()) {
            differenceFound = true;
            history.setProjectNew(itemNew.getProject());
            history.setProjectOld(itemOld.getProject());
        }
        if (!itemNew.getPurity().equals(itemOld.getPurity())) {
            differenceFound = true;
            history.setPurityNew(itemNew.getPurity());
            history.setPurityOld(itemOld.getPurity());
        }
        if (differenceFound) {
            return history;
        } else {
            return null;
        }
    }
}
