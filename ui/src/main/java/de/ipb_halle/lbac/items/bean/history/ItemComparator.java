/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.admission.User;
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
    private Date mdate;
    private Item itemOld;
    private Item itemNew;
    private boolean differenceFound;
    private ItemHistory history = new ItemHistory();

    public ItemComparator(Date mdate) {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.mdate = mdate;
    }

    public ItemComparator() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        mdate = new Date();
    }

    public ItemHistory compareItems(Item itemOld, Item itemNew, User actor) {
        this.itemNew = itemNew;
        this.itemOld = itemOld;
        this.differenceFound = false;

        initHistoryObject(actor);
        checkAmount();
        checkConcentration();
        checkDescription();
        checkOwner();
        checkProject();
        checkPurity();
        checkContainer();

        return differenceFound ? history : null;

    }

    private void initHistoryObject(User actor) {
        history.setAction("EDIT");
        history.setActor(actor);
        history.setItem(itemNew);
        history.setMdate(mdate);
    }

    private void checkAmount() {
        BigDecimal amountOld = new BigDecimal(itemOld.getAmount());
        BigDecimal amountNew = new BigDecimal(itemNew.getAmount());
        if (amountOld.compareTo(amountNew) != 0) {
            differenceFound = true;
            history.setAmountOld(amountOld.doubleValue());
            history.setAmountNew(amountNew.doubleValue());
        }
    }

    private void checkConcentration() {
        if (!Objects.equals(itemOld.getConcentration(), itemNew.getConcentration())) {
            if (itemOld.getConcentration() == null) {
                history.setConcentrationNew(itemNew.getConcentration());
                history.setConcentrationOld(null);
            } else if (itemNew.getConcentration() == null) {
                history.setConcentrationNew(null);
                history.setConcentrationOld(itemOld.getConcentration());
            } else {
                BigDecimal concentrationOld = new BigDecimal(itemOld.getConcentration());
                BigDecimal concentrationNew = new BigDecimal(itemNew.getConcentration());
                if (concentrationNew.compareTo(concentrationOld) != 0) {
                    history.setConcentrationNew(concentrationNew.doubleValue());
                    history.setConcentrationOld(concentrationOld.doubleValue());
                }
            }
            differenceFound = true;
        }
    }

    private void checkDescription() {
        if (!Objects.equals(itemOld.getDescription(), itemNew.getDescription())) {
            differenceFound = true;
            history.setDescriptionNew(itemNew.getDescription());
            history.setDescriptionOld(itemOld.getDescription());
        }
    }

    private void checkOwner() {
        if (!itemOld.getOwner().getId().equals(itemNew.getOwner().getId())) {
            differenceFound = true;
            history.setOwnerNew(itemNew.getOwner());
            history.setOwnerOld(itemOld.getOwner());
        }
    }

    private void checkProject() {
        if (itemNew.getProject().getId() != itemOld.getProject().getId()) {
            differenceFound = true;
            history.setProjectNew(itemNew.getProject());
            history.setProjectOld(itemOld.getProject());
        }
    }

    private void checkPurity() {
        if (!Objects.equals(itemNew.getPurity(), itemOld.getPurity())) {
            differenceFound = true;
            history.setPurityNew(itemNew.getPurity());
            history.setPurityOld(itemOld.getPurity());
        }
    }

    private void checkContainer() {
        if (!areContainerEqual(itemOld.getContainer(), itemNew.getContainer())) {
            differenceFound = true;
            history.setParentContainerNew(itemNew.getContainer());
            history.setParentContainerOld(itemOld.getContainer());
        }
    }

    private boolean areContainerEqual(Container oldContainer, Container newContainer) {
        Integer idOld = null;
        if (oldContainer != null) {
            idOld = oldContainer.getId();
        }
        Integer idNew = null;
        if (newContainer != null) {
            idNew = newContainer.getId();
        }
        return Objects.equals(idOld, idNew);
    }

}
