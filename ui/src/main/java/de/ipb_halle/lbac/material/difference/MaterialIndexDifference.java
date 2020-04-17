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
package de.ipb_halle.lbac.material.difference;

import com.google.common.base.Objects;
import de.ipb_halle.lbac.material.entity.MaterialIndexHistoryEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author fmauz
 */
public class MaterialIndexDifference implements MaterialDifference {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private int materialId;
    protected Date mDate;
    protected UUID actorID;
    protected List<String> valuesOld = new ArrayList<>();
    protected List<String> valuesNew = new ArrayList<>();
    protected List<String> languageOld = new ArrayList<>();
    protected List<String> languageNew = new ArrayList<>();
    protected List<Integer> typeId = new ArrayList<>();
    protected List<Integer> rankOld = new ArrayList<>();
    protected List<Integer> rankNew = new ArrayList<>();
    protected String digest;

    public MaterialIndexDifference() {
    }

    public MaterialIndexDifference(List<MaterialIndexHistoryEntity> dbEntities) {
        if (dbEntities.isEmpty()) {
            return;
        }
        this.materialId = dbEntities.get(0).getMaterialid();
        this.mDate = dbEntities.get(0).getmDate();
        this.actorID = dbEntities.get(0).getActorid();
        for (MaterialIndexHistoryEntity dbe : dbEntities) {
            valuesOld.add(dbe.getValue_old());
            valuesNew.add(dbe.getValue_new());
            languageOld.add(dbe.getLanguage_old());
            languageNew.add(dbe.getLanguage_new());
            typeId.add(dbe.getTypeid());
            rankOld.add(dbe.getRank_old());
            rankNew.add(dbe.getRank_new());
        }

    }

    @Override
    public void initialise(int materialId, UUID actorID, Date mDate) {
        this.materialId = materialId;
        this.mDate = mDate;
        this.actorID = actorID;
    }

    @Override
    public Date getModificationDate() {
        return mDate;
    }

    public List<String> getValuesOld() {
        return valuesOld;
    }

    public void setValuesOld(List<String> valuesOld) {
        this.valuesOld = valuesOld;
    }

    public List<String> getValuesNew() {
        return valuesNew;
    }

    public void setValuesNew(List<String> valuesNew) {
        this.valuesNew = valuesNew;
    }

    public List<Integer> getTypeId() {
        return typeId;
    }

    public void setTypeId(List<Integer> typeId) {
        this.typeId = typeId;
    }

    public List<Integer> getRankOld() {
        return rankOld;
    }

    public void setRankOld(List<Integer> rankOld) {
        this.rankOld = rankOld;
    }

    public List<Integer> getRankNew() {
        return rankNew;
    }

    public void setRankNew(List<Integer> rankNew) {
        this.rankNew = rankNew;
    }

    public List<String> getLanguageOld() {
        return languageOld;
    }

    public void setLanguageOld(List<String> languageOld) {
        this.languageOld = languageOld;
    }

    public List<String> getLanguageNew() {
        return languageNew;
    }

    public void setLanguageNew(List<String> languageNew) {
        this.languageNew = languageNew;
    }

    public boolean differenceFound() {
        return !valuesOld.isEmpty();
    }

    public int getEntries() {
        return languageNew.size();
    }

    public void clearRedundantEntries() {
        for (int j = getEntries() - 1; j >= 0; j--) {
            boolean sameRank = Objects.equal(rankOld.get(j), (rankNew.get(j)));
            boolean sameLanguage = Objects.equal(languageOld.get(j), (languageNew.get(j)));
            boolean sameValue = Objects.equal(valuesOld.get(j), (valuesNew.get(j)));
            if (sameLanguage && sameRank && sameValue) {
                rankOld.remove(j);
                rankNew.remove(j);
                languageOld.remove(j);
                languageNew.remove(j);
                valuesOld.remove(j);
                valuesNew.remove(j);
                typeId.remove(j);
            }
        }
    }

    public List<MaterialIndexHistoryEntity> createDbEntities(
            int materialId,
            Date mDate,
            UUID userID) {
        List<MaterialIndexHistoryEntity> dbEntities = new ArrayList<>();
        for (int i = 0; i < getEntries(); i++) {
            MaterialIndexHistoryEntity entity = new MaterialIndexHistoryEntity();

            entity.setMaterialid(materialId);
            entity.setTypeid(typeId.get(i));
            entity.setmDate(mDate);
            entity.setActorid(userID);
            entity.setDigest(null);
            entity.setValue_old(valuesOld.get(i));
            entity.setValue_new(valuesNew.get(i));
            entity.setRank_new(rankNew.get(i));
            entity.setRank_old(rankOld.get(i));
            entity.setLanguage_new(languageNew.get(i));
            entity.setLanguage_old(languageOld.get(i));
            dbEntities.add(entity);
        }
        return dbEntities;
    }

    public int getMaterialId() {
        return materialId;
    }

    public Date getmDate() {
        return mDate;
    }

    public UUID getActorID() {
        return actorID;
    }

}
