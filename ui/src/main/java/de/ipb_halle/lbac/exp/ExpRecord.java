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

import de.ipb_halle.lbac.entity.DTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.primefaces.model.chart.BarChartModel;

/**
 * Experiment records (<code>ExpRecord</code>) are the abstract base class for
 * the single elements, which form an experiment. Given enough time, we plan to
 * deliver implementations for the following use cases:
 *
 * - (Bio-) assays - simple or formatted text records - chemical reactions -
 * attached pictures and documents - breeding / cultivation of organisms -
 * manipulation of DNA / RNA sequences - etc.
 *
 * @author fbroda
 */
public abstract class ExpRecord implements DTO, LinkedDataHolder {

    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Long exprecordid;
    private Experiment experiment;
    private ExpRecordType type;
    private List<LinkedData> linkedData;
    private boolean hasHiddenLinkedData;
    private Date creationtime;
    private Date changetime;
    private Long next;
    private int revision;
    private SimpleDateFormat dateFormatter;
    private transient boolean isEdit = false;
    private transient boolean isFirst = false;
    private transient boolean isLast = false;
    private transient int index;
    private transient int linkedDataMaxRank;

    public enum ValidationError {
        NO_TARGET,
        ASSAY_RECORD_HAS_NO_OBJECT
    }

    protected ExpRecord() {
        this.creationtime = new Date();
        this.changetime = new Date();
        this.revision = 0;
        this.dateFormatter = new SimpleDateFormat(DATE_FORMAT);
        this.hasHiddenLinkedData = false;
        this.linkedData = new ArrayList<LinkedData>(2);
        this.linkedDataMaxRank = -1;
    }

    public void activateEditModeForRecord(LinkedData recordToEdit) {
        for (LinkedData record : this.linkedData) {
            record.setEdit(false);
        }
        recordToEdit.setEdit(true);
    }

    /**
     * perform any actions necessary when cloning this record from an experiment
     * template. Copies linkedDataRecords.
     */
    public void copy() {
        for (LinkedData rec : this.linkedData) {
            rec.setExpRecord(this);
            rec.setRecordId(null);
        }
    }

    public ExpRecordEntity createExpRecordEntity() {
        return new ExpRecordEntity()
                .setCreationTime(this.creationtime)
                .setChangeTime(changetime)
                .setExpRecordId(this.exprecordid)
                .setExperimentId(this.experiment.getExperimentId())
                .setNext(this.next)
                .setRevision(this.revision)
                .setType(this.type);
    }

    public BarChartModel getBarChart() {
        return null;
    }

    public Date getChangeTime() {
        return this.changetime;
    }

    public Date getCreationTime() {
        return this.creationtime;
    }

    /**
     * @return true if the record is in edit mode
     */
    public boolean getEdit() {
        return this.isEdit;
    }

    /**
     * @return true if the record is in edit mode and not part of an experiment
     * template
     */
    public boolean getEditRecord() {
        return (this.isEdit && (!getTemplate()));
    }

    /**
     * @return true if the record is in edit mode and part of an experiment
     * template
     */
    public boolean getEditTemplate() {
        return (this.isEdit && getTemplate());
    }

    public Experiment getExperiment() {
        return this.experiment;
    }

    public String getExpRecordDetails() {
        // Messages.getString(MESSAGE_BUNDLE, "expBean_ChangeTime", null);
        return "Changed: " + this.dateFormatter.format(this.changetime);
    }

    public Long getExpRecordId() {
        return this.exprecordid;
    }

    public String getExpRecordInfo() {
        StringBuilder sb = new StringBuilder();
        // Messages.getString(MESSAGE_BUNDLE, "expBean_New", null);
        sb.append((this.exprecordid == null) ? "#" : this.exprecordid.toString());
        sb.append(" -- ");
        sb.append(this.dateFormatter.format(this.creationtime));
        return sb.toString();
    }

    /**
     * name of the component for rendering
     */
    public String getFacelet() {
        return this.type.toString();
    }

    public boolean getFirst() {
        return this.isFirst;
    }

    public boolean getHasHiddenLinkedData() {
        return this.hasHiddenLinkedData;
    }

    /**
     * the index in a list of ExpRecords
     */
    public int getIndex() {
        return this.index;
    }

    public boolean getLast() {
        return this.isLast;
    }

    public List<LinkedData> getLinkedData() {
        return this.linkedData;
    }

    public int getLinkedDataNextRank() {
        this.linkedDataMaxRank++;
        return this.linkedDataMaxRank;
    }

    public Long getNext() {
        return this.next;
    }

    public int getRevision() {
        return this.revision;
    }

    /**
     * whether to disable the MoveDown button (move record to higher index)
     */
    public boolean getMoveDownDisabled() {
        return (!this.isEdit) || this.isLast;
    }

    /**
     * whether to disable the MoveUp button (move record to lower index)
     */
    public boolean getMoveUpDisabled() {
        return (!this.isEdit) || this.isFirst;
    }

    /**
     * @return the value of the template flag for this records experiment or
     * false if experiment is not set.
     */
    public boolean getTemplate() {
        if (this.experiment != null) {
            return this.experiment.getTemplate();
        }
        return false;
    }

    public ExpRecordType getType() {
        return this.type;
    }

    /**
     * increment the revision of this record and update the changetime.
     */
    public void incrementRevision() {
        this.changetime = new Date();
        this.revision += 1;
    }

    public void reIndexLinkedData() {
        int i = 0;
        for (LinkedData d : this.linkedData) {
            d.setIndex(i);
            i++;
        }
    }

    public ExpRecord setCreationTime(Date creationtime) {
        this.creationtime = creationtime;
        return this;
    }

    public ExpRecord setEdit(boolean isEdit) {
        this.isEdit = isEdit;
        return this;
    }

    public ExpRecord setExperiment(Experiment experiment) {
        this.experiment = experiment;
        return this;
    }

    /**
     * update the current object from the entity
     *
     * @param entity
     * @return
     */
    public ExpRecord setExpRecordEntity(ExpRecordEntity entity) {
        this.changetime = entity.getChangeTime();
        this.creationtime = entity.getCreationTime();
        this.exprecordid = entity.getExpRecordId();
        this.next = entity.getNext();
        this.revision = entity.getRevision();
        this.type = entity.getType();
        return this;
    }

    public ExpRecord setExpRecordId(Long exprecordid) {
        this.exprecordid = exprecordid;
        return this;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public void setHasHiddenLinkedData() {
        this.hasHiddenLinkedData = true;
    }

    public ExpRecord setIndex(int index) {
        this.index = index;
        return this;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    public void setLinkedData(List<LinkedData> data) {
        this.linkedData = data;
    }

    public void setLinkedDataMaxRank(int rank) {
        this.linkedDataMaxRank = rank;
    }

    public ExpRecord setNext(Long next) {
        this.next = next;
        return this;
    }

    public ExpRecord setRevision(int revision) {
        this.revision = revision;
        return this;
    }

    public ExpRecord setType(ExpRecordType type) {
        this.type = type;
        return this;
    }

    public List<String> getLinkNames() {
        List<String> names = new ArrayList<>();
        for (LinkedData ld : getLinkedData()) {
            if (ld.getLinkedDataType() == LinkedDataType.LINK_MATERIAL
                    || ld.getLinkedDataType() == LinkedDataType.LINK_ITEM) {
                LinkText linkText = (LinkText) ld.getPayload();
                names.add(linkText.getText());
            }
        }
        return names;
    }

    public abstract boolean validate();

    public abstract Set<ValidationError> getErrors();
}
