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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.datalink.LinkedData;
import de.ipb_halle.lbac.exp.assay.Assay;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;

/**
 * interface for experiment record controllers
 *
 * @author fbroda
 */
public abstract class ExpRecordController implements ItemHolder, MaterialHolder {

    protected ExperimentBean bean;
    private int linkedDataIndex;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private MessagePresenter messagePresenter;
    private Map<ExpRecord.ValidationError, String> errorMessages = new HashMap<>();

    protected ExpRecordController(ExperimentBean bean) {
        this.bean = bean;
        bean.getMaterialAgent().setMaterialHolder(null);
        bean.getItemAgent().setItemHolder(null);
        messagePresenter = bean.getMessagePresenter();
        errorMessages.put(ExpRecord.ValidationError.NO_TARGET, "expAddRecord_no_target");
        errorMessages.put(ExpRecord.ValidationError.ASSAY_RECORD_HAS_NO_OBJECT, "expAddRecord_no_recordObject");
    }

    public void actionCancel() {
        ExpRecord rec = getExpRecord();
        if (rec != null) {
            if (rec.getExpRecordId() == null) {
                this.bean.getExpRecords().remove(rec.getIndex());
            } else {
                int index = rec.getIndex();
                rec = this.bean.loadExpRecordById(rec.getExpRecordId());
                this.bean.getExpRecords().set(index, rec);
            }
        }
        this.bean.reIndex();
        this.bean.cleanup();
    }

    public void actionSaveRecord() {
        try {
            ExpRecord rec = getExpRecord();
            if (!rec.validate()) {
                for (ExpRecord.ValidationError error : rec.getErrors()) {
                    messagePresenter.error(errorMessages.get(error));
                }
                if (FacesContext.getCurrentInstance() != null) {
                    // sets the JavaScript variable OmniFaces.Ajax.data.validationFailed 
                    Ajax.data("validationFailed", true);
                }
                return;
            }
            if (rec == null) {
                throw new NullPointerException("attempt to save non-existent ExpRecord");
            }
            rec = this.bean.saveExpRecord(rec);
            this.bean.adjustOrder(rec);
            this.bean.cleanup();
            this.bean.reIndex();
            messagePresenter.info("expAddRecord_add_success");
            if (FacesContext.getCurrentInstance() != null) {
                // sets the JavaScript variable OmniFaces.Ajax.data.validationFailed 
                Ajax.data("validationFailed", false);
            }
        } catch (Exception e) {
            messagePresenter.error("expAddRecord_error");
            this.logger.warn("actionSaveRecord() caught an exception: ", (Throwable) e);
        }
    }

    public boolean getEdit() {
        return false;
    }

    public ExperimentBean getExperimentBean() {
        return this.bean;
    }

    public ExpRecord getExpRecord() {
        int i = this.bean.getExpRecordIndex();
        if ((i >= 0) && (i < this.bean.getExpRecords().size())) {
            return this.bean.getExpRecords().get(i);
        }
        return null;
    }

    public Item getItem() {
        List<LinkedData> list = getExpRecord().getLinkedData();
        int index = getLinkedDataIndex();
        if ((index >= 0) && (index < list.size())) {
            return list.get(index).getItem();
        }
        return null;
    }

    /**
     * @return the current record in edit mode
     */
    public int getLinkedDataIndex() {
        return this.linkedDataIndex;
    }

    public Material getMaterial() {
        List<LinkedData> list = getExpRecord().getLinkedData();
        int index = getLinkedDataIndex();
        if ((index >= 0) && (index < list.size())) {
            return list.get(index).getMaterial();
        }
        return null;
    }

    public List<MaterialType> getMaterialTypes() {
        return Arrays.asList(
                MaterialType.COMPOSITION,
                MaterialType.BIOMATERIAL,
                MaterialType.STRUCTURE);
    }

    public abstract ExpRecord getNewRecord();

    public boolean isDiagrammButtonVisible(Assay assay) {
        List<LinkedData> linkedData = assay.getLinkedData();

        // There needs to be at least one result. (n.b.: The first list element is the ASSAY_TARGET.)
        if (linkedData.size() < 2) {
            return false;
        }

        return (linkedData.get(0).getMaterial() != null) || (linkedData.get(0).getItem() != null);
    }

    public void setItem(Item item) {
        List<LinkedData> list = getExpRecord().getLinkedData();
        if ((this.linkedDataIndex >= 0) && (this.linkedDataIndex < list.size())) {
            list.get(this.linkedDataIndex).setItem(item);
        }
    }

    /**
     * select a LinkedData record for editing
     */
    public void setLinkedDataIndex(int index) {
        this.linkedDataIndex = index;
    }

    public void setMaterial(Material material) {
        List<LinkedData> list = getExpRecord().getLinkedData();

        if ((this.linkedDataIndex >= 0) && (this.linkedDataIndex < list.size())) {
            list.get(this.linkedDataIndex).setMaterial(material);
        }
    }

    /**
     * Returns JavaScript code to be executed in the onclick event of the "save"
     * commandButton for this experiment record before executing its AJAX call.
     * <p>
     * Clients of this class can overwrite this method to execute their own
     * JavaScript code. This code may not use the <a href=
     * "https://showcase.bootsfaces.net/forms/ajax.jsf#basic_usage">BootsFaces-specific
     * prefixes</a> {@code javascript:} and {@code ajax:}.
     * 
     * @return JavaScript code to be executed; the default implementation
     *         returns an empty string
     */
    public String getSaveButtonOnClick() {
        return "";
    }
}
