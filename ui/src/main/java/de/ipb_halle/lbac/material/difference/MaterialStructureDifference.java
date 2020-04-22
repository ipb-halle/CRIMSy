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

import de.ipb_halle.lbac.material.bean.ModificationType;
import de.ipb_halle.lbac.material.subtype.structure.Molecule;

import de.ipb_halle.lbac.material.entity.structure.StructureHistEntity;
import de.ipb_halle.lbac.material.entity.structure.StructureHistEntityId;
import java.util.Date;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author fmauz
 */
public class MaterialStructureDifference implements MaterialDifference {

    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    private Date modificationTime;
    private UUID actorId;
    private int materialId;
    private ModificationType action;
    private String sumFormula_old;
    private String sumFormula_new;
    private Double molarMass_old;
    private Double molarMass_new;
    private Double exactMolarMass_old;
    private Double exactMolarMass_new;
    private Molecule molecule_old;
    private Molecule molecule_new;

    public MaterialStructureDifference(
            StructureHistEntity dbE,
            Molecule moleculeOld,
            Molecule moleculeNew) {
        this.modificationTime = dbE.getId().getMtime();
        this.actorId = dbE.getActorid();
        this.materialId = dbE.getId().getId();
        this.sumFormula_new = dbE.getSumformula_new();
        this.sumFormula_old = dbE.getSumformula_old();
        this.molarMass_new = dbE.getMolarmass_new();
        this.molarMass_old = dbE.getMolarmass_old();
        this.exactMolarMass_new = dbE.getExactmolarmass_new();
        this.exactMolarMass_old = dbE.getExactmolarmass_old();
        this.molecule_new = moleculeNew;
        this.molecule_old = moleculeOld;

    }

    public MaterialStructureDifference() {

    }

    public StructureHistEntity createDbInstance() {
        StructureHistEntity dbentity = new StructureHistEntity();
        dbentity.setId(new StructureHistEntityId(materialId, modificationTime));
        dbentity.setActorid(actorId);
        dbentity.setExactmolarmass_new(exactMolarMass_new);
        dbentity.setExactmolarmass_new(exactMolarMass_old);
        dbentity.setMolarmass_new(molarMass_new);
        dbentity.setMolarmass_old(molarMass_old);
        if (molecule_old != null && molecule_old.getId() > 0) {
            dbentity.setMoleculeid_old(molecule_old.getId());
        }
        if (molecule_new != null && molecule_new.getId() > 0) {
            dbentity.setMoleculeid_new(molecule_new.getId());
        }
        dbentity.setSumformula_new(sumFormula_new);
        dbentity.setSumformula_old(sumFormula_old);
        return dbentity;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public ModificationType getAction() {
        return action;
    }

    public void setAction(ModificationType action) {
        this.action = action;
    }

    public String getSumFormula_old() {
        return sumFormula_old;
    }

    public void setSumFormula_old(String sumFormula_old) {
        this.sumFormula_old = sumFormula_old;
    }

    public String getSumFormula_new() {
        return sumFormula_new;
    }

    public void setSumFormula_new(String sumFormula_new) {
        this.sumFormula_new = sumFormula_new;
    }

    public Double getMolarMass_old() {
        return molarMass_old;
    }

    public void setMolarMass_old(Double molarMass_old) {
        this.molarMass_old = molarMass_old;
    }

    public Double getMolarMass_new() {
        return molarMass_new;
    }

    public void setMolarMass_new(Double molarMass_new) {
        this.molarMass_new = molarMass_new;
    }

    public Double getExactMolarMass_old() {
        return exactMolarMass_old;
    }

    public void setExactMolarMass_old(Double exactMolarMass_old) {
        this.exactMolarMass_old = exactMolarMass_old;
    }

    public Double getExactMolarMass_new() {
        return exactMolarMass_new;
    }

    public void setExactMolarMass_new(Double exactMolarMass_new) {
        this.exactMolarMass_new = exactMolarMass_new;
    }

    public Molecule getMoleculeId_old() {
        return molecule_old;
    }

    public void setMoleculeId_old(Molecule molecule_old) {
        this.molecule_old = molecule_old;
    }

    public Molecule getMoleculeId_new() {
        return molecule_new;
    }

    public void setMoleculeId_new(Molecule molecule_new) {
        this.molecule_new = molecule_new;
    }

    public boolean differenceFound() {
        return sumFormula_new != null
                || molarMass_new != null
                || exactMolarMass_new != null
                || molecule_new != null;
    }

    @Override
    public void initialise(int materialId, UUID actorID, Date mDate) {
        this.materialId = materialId;
        this.action = ModificationType.EDIT;
        this.actorId = actorID;
        this.modificationTime = mDate;
    }

    @Override
    public Date getModificationDate() {
        return modificationTime;
    }

}
