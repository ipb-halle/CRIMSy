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
package de.ipb_halle.lbac.items;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.entity.ItemHistoryEntity;
import de.ipb_halle.lbac.items.entity.ItemHistoryId;
import de.ipb_halle.lbac.project.Project;
import java.util.Date;

/**
 *
 * @author fmauz
 */
public class ItemHistory implements DTO, ItemDifference {

    protected String action;
    protected User actor;
    protected ACList acListNew;
    protected ACList acListOld;
    protected Double amountNew;
    protected Double amountOld;
    protected Double concentrationNew;
    protected Double concentrationOld;
    protected String descriptionNew;
    protected String descriptionOld;
    protected Item item;
    protected Date mdate;
    protected User ownerNew;
    protected User ownerOld;
    protected Project projectOld;
    protected Project projectNew;
    protected String purityNew;
    protected String purityOld;
    protected Container parentContainerNew;
    protected Container parentContainerOld;

    public ItemHistory() {
    }

    /**
     * Constructor by entity and injected objects
     *
     * @param entity
     * @param actor
     * @param ownerNew
     * @param ownerOld
     * @param item
     * @param projectOld
     * @param projectNew
     * @param oldParentContainer
     * @param newParentContainer
     * @param aclistOld
     * @param aclistNew
     *
     */
    public ItemHistory(ItemHistoryEntity entity,
            User actor,
            User ownerOld,
            User ownerNew,
            Item item,
            Project projectOld,
            Project projectNew,
            Container oldParentContainer,
            Container newParentContainer,
            ACList aclistOld,
            ACList aclistNew) {
        this.actor = actor;
        this.ownerNew = ownerNew;
        this.ownerOld = ownerOld;
        this.item = item;
        this.projectOld = projectOld;
        this.projectNew = projectNew;
        this.action = entity.getAction();
        this.amountNew = entity.getAmount_new();
        this.amountOld = entity.getAmount_old();
        this.concentrationNew = entity.getConcentration_new();
        this.concentrationOld = entity.getConcentration_old();
        this.descriptionNew = entity.getDescription_new();
        this.descriptionOld = entity.getDescription_old();
        this.purityNew = entity.getPurity_new();
        this.purityOld = entity.getPurity_old();
        this.mdate = entity.getId().getMdate();
        this.parentContainerOld = oldParentContainer;
        this.parentContainerNew = newParentContainer;
        this.acListNew = aclistNew;
        this.acListOld = aclistOld;

    }

    /**
     * Create a db entity
     *
     * @return
     */
    @Override
    public ItemHistoryEntity createEntity() {
        ItemHistoryEntity entity = new ItemHistoryEntity();
        entity.setId(new ItemHistoryId(item.getId(), actor.getId(), mdate));
        entity.setAction(action);
        entity.setAmount_new(amountNew);
        entity.setAmount_old(amountOld);
        entity.setConcentration_new(concentrationNew);
        entity.setConcentration_old(concentrationOld);
        entity.setDescription_new(descriptionNew);
        entity.setDescription_old(descriptionOld);
        if (ownerNew != null) {
            entity.setOwner_new(ownerNew.getId());
        }
        if (ownerOld != null) {
            entity.setOwner_old(ownerOld.getId());
        }
        if (projectNew != null) {
            entity.setProjectid_new(projectNew.getId());
        }
        if (projectOld != null) {
            entity.setProjectid_old(projectOld.getId());
        }
        entity.setPurity_new(purityNew);
        entity.setPurity_old(purityOld);
        if (parentContainerNew != null) {
            entity.setParent_containerid_new(parentContainerNew.getId());
        }
        if (parentContainerOld != null) {
            entity.setParent_containerid_old(parentContainerOld.getId());
        }
        if (acListNew != null) {
            entity.setAclistid_new(acListNew.getId());
        }
        if (acListOld != null) {
            entity.setAclistid_old(acListOld.getId());
        }
        return entity;
    }

    public String getAction() {
        return action;
    }

    public User getActor() {
        return actor;
    }

    public Double getAmountNew() {
        return amountNew;
    }

    public Double getAmountOld() {
        return amountOld;
    }

    public Double getConcentrationNew() {
        return concentrationNew;
    }

    public Double getConcentrationOld() {
        return concentrationOld;
    }

    public String getDescriptionNew() {
        return descriptionNew;
    }

    public String getDescriptionOld() {
        return descriptionOld;
    }

    public Item getItem() {
        return item;
    }

    public Date getMdate() {
        return mdate;
    }

    public User getOwnerNew() {
        return ownerNew;
    }

    public User getOwnerOld() {
        return ownerOld;
    }

    public Project getProjectOld() {
        return projectOld;
    }

    public Project getProjectNew() {
        return projectNew;
    }

    public String getPurityNew() {
        return purityNew;
    }

    public String getPurityOld() {
        return purityOld;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public void setAmountNew(Double amountNew) {
        this.amountNew = amountNew;
    }

    public void setAmountOld(Double amountOld) {
        this.amountOld = amountOld;
    }

    public void setConcentrationNew(Double concentrationNew) {
        this.concentrationNew = concentrationNew;
    }

    public void setConcentrationOld(Double concentrationOld) {
        this.concentrationOld = concentrationOld;
    }

    public void setDescriptionNew(String descriptionNew) {
        this.descriptionNew = descriptionNew;
    }

    public void setDescriptionOld(String descriptionOld) {
        this.descriptionOld = descriptionOld;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    public void setOwnerNew(User ownerNew) {
        this.ownerNew = ownerNew;
    }

    public void setOwnerOld(User ownerOld) {
        this.ownerOld = ownerOld;
    }

    public void setProjectOld(Project projectOld) {
        this.projectOld = projectOld;
    }

    public void setProjectNew(Project projectNew) {
        this.projectNew = projectNew;
    }

    public void setPurityNew(String purityNew) {
        this.purityNew = purityNew;
    }

    public void setPurityOld(String purityOld) {
        this.purityOld = purityOld;
    }

    public Container getParentContainerNew() {
        return parentContainerNew;
    }

    public void setParentContainerNew(Container parentContainerNew) {
        this.parentContainerNew = parentContainerNew;
    }

    public Container getParentContainerOld() {
        return parentContainerOld;
    }

    public void setParentContainerOld(Container parentContainerOld) {
        this.parentContainerOld = parentContainerOld;
    }

    public void setAcListChange(ACList oldAcl, ACList newAcl) {
        this.acListNew = newAcl;
        this.acListOld = oldAcl;
    }

}
