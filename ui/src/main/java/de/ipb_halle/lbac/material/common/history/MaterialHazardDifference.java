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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.common.ModificationType;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardsMaterialHistEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author fmauz
 */
public class MaterialHazardDifference implements MaterialDifference {
    
    protected int materialID;
    protected Integer actorID;
    protected Date mDate;
    protected String digest;
    protected ModificationType action;
    protected List<Integer> typeIdsOld = new ArrayList<>();
    protected List<Integer> typeIdsNew = new ArrayList<>();
    protected List<String> remarksOld = new ArrayList<>();
    protected List<String> remarksNew = new ArrayList<>();
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    
    public MaterialHazardDifference() {
    }
    
    public MaterialHazardDifference(List<HazardsMaterialHistEntity> dbEntities) {
        if (dbEntities != null && dbEntities.size() > 0) {
            this.mDate = dbEntities.get(0).getmDate();
            this.actorID = dbEntities.get(0).getActorid();
            this.action = ModificationType.EDIT;
            this.materialID = dbEntities.get(0).getMaterialid();
            
        }
        for (HazardsMaterialHistEntity dbEntity : dbEntities) {
            addDifference(
                    dbEntity.getTypeid_old(),
                    dbEntity.getTypeid_new(),
                    dbEntity.getRemarks_old(),
                    dbEntity.getRemarks_new());
        }
        
    }
    
    public final void addDifference(
            Integer oldTypeId,
            Integer newTypeId,
            String oldRemark,
            String newRemark) {
        typeIdsOld.add(oldTypeId);
        typeIdsNew.add(newTypeId);
        remarksOld.add(oldRemark);
        remarksNew.add(newRemark);
    }
    
    public List<HazardsMaterialHistEntity> createDbInstances() {
        List<HazardsMaterialHistEntity> dbEntities = new ArrayList<>();
        for (int i = 0; i < getEntries(); i++) {
            HazardsMaterialHistEntity dbEntity = new HazardsMaterialHistEntity();
            dbEntity.setActorid(actorID);
            dbEntity.setMaterialid(materialID);
            dbEntity.setmDate(mDate);
            dbEntity.setRemarks_new(remarksNew.get(i));
            dbEntity.setRemarks_old(remarksOld.get(i));
            dbEntity.setTypeid_new(typeIdsNew.get(i));
            dbEntity.setTypeid_old(typeIdsOld.get(i));
            dbEntities.add(dbEntity);
        }
        return dbEntities;
        
    }
    
    @Override
    public void initialise(int materialId, Integer actorID, Date mDate) {
        this.materialID = materialId;
        this.actorID = actorID;
        this.mDate = mDate;
    }
    
    @Override
    public Date getModificationDate() {
        return mDate;
    }
    
    public int getMaterialID() {
        return materialID;
    }
    
    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }
    
    public Integer getActorID() {
        return actorID;
    }
    
    public void setActorID(Integer actorID) {
        this.actorID = actorID;
    }
    
    public Date getmDate() {
        return mDate;
    }
    
    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }
    
    public String getDigest() {
        return digest;
    }
    
    public void setDigest(String digest) {
        this.digest = digest;
    }
    
    public List<Integer> getTypeIdsOld() {
        return typeIdsOld;
    }
    
    public List<Integer> getTypeIdsNew() {
        return typeIdsNew;
    }
    
    public List<String> getRemarksOld() {
        return remarksOld;
    }
    
    public List<String> getRemarksNew() {
        return remarksNew;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    public int getEntries() {
        return remarksOld.size();
    }
    
    public void addHazardRemovement(Integer typeId,String oldRemarks) {
        typeIdsOld.add(typeId);
        typeIdsNew.add(null);
        remarksOld.add(oldRemarks);
        remarksNew.add(null);
    }
    
    public void addHazardExpansion(Integer typeId,String newRemarks) {
        typeIdsOld.add(null);
        typeIdsNew.add(typeId);
        remarksOld.add(null);
        remarksNew.add(newRemarks);
    }

    @Override
    public Integer getActorId() {
        return actorID;
    }
    
}
