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
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.entity.MaterialCompositionEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialCompositionId;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class MaterialComposition extends Material {
    
    private static final long serialVersionUID = 1L;
    private CompositionType compositionType;
    
    protected Map<Material, Double> components = new HashMap<>();
    
    public MaterialComposition(
            int id,
            List<MaterialName> names,
            int projectId,
            HazardInformation hazards,
            StorageInformation storageInfos,
            CompositionType compositionType) {
        super(id, names, projectId, hazards, storageInfos);
        type = MaterialType.COMPOSITION;
        this.compositionType = compositionType;
    }
    
    public MaterialComposition addComponent(Material comp, Double concentration) {
        components.put(comp, concentration);
        return this;
    }
    
    public Map<Material, Double> getComponents() {
        return components;
    }
    
    @Override
    public MaterialComposition copyMaterial() {
        MaterialComposition copy = new MaterialComposition(
                id, getCopiedNames(),
                projectId,
                hazards.copy(),
                storageInformation.copy(),
                compositionType);
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
        for (Material m : components.keySet()) {
            MaterialCompositionEntity entity = new MaterialCompositionEntity()
                    .setId(new MaterialCompositionId(id, m.getId()));
            entity.setConcentration(components.get(m));
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
    
    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.COMPOSITION);
    }
    
}
