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
package de.ipb_halle.lbac.material.structure;

import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.MaterialType;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Structure extends Material {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    protected String sumFormula;
    protected Molecule molecule;
    private Double molarMass;
    private Double exactMolarMass;

    public Structure(
            String sumFormula,
            Double molarMass,
            Double exactMolarMass,
            int id,
            List<MaterialName> names,
            Integer projectId,
            HazardInformation hazards,
            StorageClassInformation storageInfos,
            Molecule molecule) {
        super(id, names, projectId, hazards, storageInfos);
        this.sumFormula = sumFormula;
        this.molarMass = molarMass;
        this.exactMolarMass = exactMolarMass;
        this.molecule = molecule;
        this.type = MaterialType.STRUCTURE;
    }

    public Structure(
            String sumFormula,
            Double molarMass,
            Double exactMolarMass,
            int id,
            List<MaterialName> names,
            Integer projectId) {
        this(
                sumFormula,
                molarMass,
                exactMolarMass,
                id,
                names,
                projectId,
                new HazardInformation(),
                new StorageClassInformation(),
                null);

    }

    @Override
    public String getNumber() {
        String back = "";

        return back;
    }

    public String getSumFormula() {
        return sumFormula;
    }

    public void setSumFormula(String sumFormula) {
        this.sumFormula = sumFormula;
    }

    @Override
    public List<IndexEntry> getIndices() {
        return indices;
    }

    public void setIndices(List<IndexEntry> indices) {
        this.indices = indices;
    }

    public Molecule getMolecule() {
        return molecule;
    }

    public void setMolecule(Molecule molecule) {
        this.molecule = molecule;
    }

    public Double getMolarMass() {
        return molarMass;
    }

    public void setMolarMass(Double molarMass) {
        this.molarMass = molarMass;
    }

    public Double getExactMolarMass() {
        return exactMolarMass;
    }

    public void setExactMolarMass(Double exactMolarMass) {
        this.exactMolarMass = exactMolarMass;
    }

    public static Structure createInstanceFromDB(
            MaterialEntity mE,
            HazardInformation hazardInfos,
            StorageClassInformation storageInfos,
            List<MaterialIndexEntryEntity> indices,
            StructureEntity strcutureEntity,
            String molecule,
            int moleculeId,
            String moleculeFormat
    ) {

        String sumFormula = strcutureEntity.getSumformula();
        String stuctureModel = molecule;
        Double molarMass = strcutureEntity.getMolarmass();
        Double exactMass = strcutureEntity.getExactmolarmass();

        List<MaterialName> names = new ArrayList<>();
        List<IndexEntry> inices = new ArrayList<>();

        for (MaterialIndexEntryEntity mie : indices) {
            if (mie.getTypeid() > 1) {
                inices.add(new IndexEntry(mie.getTypeid(), mie.getValue(), mie.getLanguage()));
            } else {
                names.add(new MaterialName(mie.getValue(), mie.getLanguage(), mie.getRank()));
            }
        }
        Structure s = new Structure(
                sumFormula,
                molarMass,
                exactMass,
                mE.getMaterialid(),
                names,
                mE.getProjectid(),
                hazardInfos,
                storageInfos,
                new Molecule(stuctureModel, moleculeId)
        );
        s.setId(mE.getMaterialid());
        s.setIndices(inices);
        s.setCreationTime(mE.getCtime());
        return s;
    }

    public StructureEntity createDbEntity(int materialId, Integer moleculeId) {
        StructureEntity sE = new StructureEntity();
        sE.setSumformula(sumFormula);
        sE.setExactmolarmass(exactMolarMass);
        sE.setMolarmass(molarMass);
        sE.setId(materialId);
        sE.setMoleculeid(moleculeId);
        return sE;

    }

    @Override
    public Material copyMaterial() {
        Structure copy = new Structure(
                sumFormula,
                molarMass,
                exactMolarMass,
                id,
                getCopiedNames(),
                projectId,
                hazards.copy(),
                storageInformation.copy(),
                new Molecule(molecule.getStructureModel(), molecule.getId(), molecule.getModelType().toString()));

        copy.setIndices(getCopiedIndices());
        copy.setNames(getCopiedNames());
        copy.setDetailRights(getCopiedDetailRights());
        copy.setOwner(getOwner());
        copy.setACList(getACList());
        copy.setCreationTime(creationTime);
        copy.setHistory(history);

        return copy;
    }

    @Override
    public Object createEntity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
