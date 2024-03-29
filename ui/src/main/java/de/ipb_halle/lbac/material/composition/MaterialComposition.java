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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.util.units.Unit;
import de.ipb_halle.lbac.material.MaterialType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class MaterialComposition extends Material {

    private static final long serialVersionUID = 1L;
    private final CompositionType compositionType;

    protected List<Concentration> components
            = new ArrayList<>();

    public MaterialComposition(int projectId, CompositionType compositionType) {
        super(null, new ArrayList<>(), projectId, new HazardInformation(), new StorageInformation());
        type = MaterialType.COMPOSITION;
        this.compositionType = compositionType;
    }

    public MaterialComposition(Integer materialId, int projectId, CompositionType compositionType) {
        super(materialId, new ArrayList<>(), projectId, new HazardInformation(), new StorageInformation());
        type = MaterialType.COMPOSITION;
        this.compositionType = compositionType;
    }

    public MaterialComposition(
            Integer id,
            List<MaterialName> names,
            int projectId,
            HazardInformation hazards,
            StorageInformation storageInfos,
            CompositionType compositionType) {
        super(id, names, projectId, hazards, storageInfos);
        type = MaterialType.COMPOSITION;
        this.compositionType = compositionType;

    }

    public MaterialComposition addComponent(Material comp, Double concentration, Unit unit) {
        if (!canHoldType(comp.getType())) {
            throw new IllegalArgumentException("Composition " + compositionType + " must not hold material of type " + comp.getType());
        }
        components.add(new Concentration(comp, concentration, unit));
        return this;
    }

    public List<Concentration> getComponents() {
        return components;
    }

    @Override
    public MaterialComposition copyMaterial() {
        MaterialComposition copy = new MaterialComposition(
                id,
                getCopiedNames(),
                projectId,
                hazards.copy(),
                storageInformation.copy(),
                compositionType);
        copy.setOwner(this.getOwner());
        copy.setACList(getACList());
        copy.setCreationTime(creationTime);
        copy.setHistory(history);
        copy.setIndices(getCopiedIndices());

        for (Concentration conc : components) {
            copy.getComponents().add(new Concentration(conc.getMaterial(), conc.getConcentration(), conc.getUnit()));
        }
        return copy;
    }

    /**
     *
     * @return
     */
    @Override
    public CompositionEntity createEntity() {
        CompositionEntity entity = new CompositionEntity();
        entity.setMaterialid(id);
        entity.setType(compositionType.toString());
        return entity;
    }

    @Override
    public List<MaterialCompositionEntity> createCompositionEntities() {
        List<MaterialCompositionEntity> entities = new ArrayList<>();
        for (Concentration m : components) {
            MaterialCompositionEntity entity = new MaterialCompositionEntity()
                    .setId(new MaterialCompositionId(id, m.getMaterialId()));
            entity.setConcentration(m.getConcentration());
            entity.setUnit(m.getUnitString());
            entities.add(entity);
        }
        entities.addAll(super.createCompositionEntities());
        return entities;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof MaterialComposition)) {
            return false;
        }
        MaterialComposition otherUser = (MaterialComposition) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

    public List<MaterialType> getPossibleTypesOfComponents() {
        return compositionType.getAllowedTypes();
    }

    public boolean canHoldType(MaterialType type) {
        return getPossibleTypesOfComponents().contains(type);
    }

    public CompositionType getCompositionType() {
        return compositionType;
    }

}
