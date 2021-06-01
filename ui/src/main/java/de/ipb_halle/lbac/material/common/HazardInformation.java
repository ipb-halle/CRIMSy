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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardMaterialId;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardsMaterialsEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class HazardInformation implements Serializable {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    Map<HazardType, String> hazards = new HashMap<>();

    public HazardInformation() {

    }

    public HazardInformation(List<HazardsMaterialsEntity> dbes, List<HazardType> possibleTypes) {
        for (HazardsMaterialsEntity dbe : dbes) {
            hazards.put(
                    getHazardById(dbe.getId().getTypeID(), possibleTypes),
                    dbe.getRemarks());
        }
    }

    public HazardInformation(Material m) {
        hazards = new HashMap<>((m.getHazards().getHazards()));
    }

    public List<HazardsMaterialsEntity> createEntity(int materialId) {
        List<HazardsMaterialsEntity> entities = new ArrayList<>();
        for (HazardType h : hazards.keySet()) {
            entities.add(
                    new HazardsMaterialsEntity(
                            new HazardMaterialId(
                                    h.getId(),
                                    materialId), hazards.get(h)));
        }
        return entities;
    }

    public Map<HazardType, String> getHazards() {
        return hazards;
    }

    public void setHazards(Map<HazardType, String> hazards) {
        this.hazards = hazards;
    }

    public HazardInformation copy() {
        HazardInformation copy = new HazardInformation();
        Map<HazardType, String> copiedHazards = new HashMap<>(hazards);
        copy.setHazards(copiedHazards);
        return copy;
    }

    private HazardType getHazardById(int id, List<HazardType> possibleHazards) {
        for (HazardType hazard : possibleHazards) {
            if (hazard.getId() == id) {
                return hazard;
            }
        }
        throw new RuntimeException(String.format("Could not find hazard with id : %d", id));
    }

}
