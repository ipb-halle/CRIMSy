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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardMaterialId;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardsMaterialsEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author fmauz
 */
public class HazardInformation {

    public static final Integer HAZARD_STATEMENT = 12;
    public static final Integer PRECAUTIONARY_STATEMENT = 13;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    Set<Hazard> hazards = new HashSet<>();

    private String hazardStatements;
    private String precautionaryStatements;

    public HazardInformation() {

    }

    public HazardInformation(Material m) {
        hazards.addAll(m.getHazards().getHazards());
        hazardStatements = m.getHazards().getHazardStatements();
        precautionaryStatements = m.getHazards().getPrecautionaryStatements();
    }

    public boolean isExplosive() {
        return hazards.contains(Hazard.explosive);
    }

    public void setExplosive(boolean explosive) {
        if (explosive) {
            hazards.add(Hazard.explosive);
        } else {
            hazards.remove(Hazard.explosive);
        }

    }

    public boolean isHighlyFlammable() {
        return hazards.contains(Hazard.highlyFlammable);

    }

    public void setHighlyFlammable(boolean highlyFlammable) {
        if (highlyFlammable) {
            hazards.add(Hazard.highlyFlammable);
        } else {
            hazards.remove(Hazard.highlyFlammable);
        }
    }

    public boolean isOxidizing() {
        return hazards.contains(Hazard.oxidizing);

    }

    public void setOxidizing(boolean oxidizing) {
        if (oxidizing) {
            hazards.add(Hazard.oxidizing);
        } else {
            hazards.remove(Hazard.oxidizing);
        }
    }

    public boolean isCompressedGas() {
        return hazards.contains(Hazard.compressedGas);

    }

    public void setCompressedGas(boolean compressedGas) {
        if (compressedGas) {
            hazards.add(Hazard.compressedGas);
        } else {
            hazards.remove(Hazard.compressedGas);
        }
    }

    public boolean isCorrosive() {
        return hazards.contains(Hazard.corrosive);

    }

    public void setCorrosive(boolean corrosive) {
        if (corrosive) {
            hazards.add(Hazard.corrosive);
        } else {
            hazards.remove(Hazard.corrosive);
        }
    }

    public boolean isPoisonous() {
        return hazards.contains(Hazard.poisonous);

    }

    public void setPoisonous(boolean poisonous) {
        if (poisonous) {
            hazards.add(Hazard.poisonous);
        } else {
            hazards.remove(Hazard.poisonous);
        }
    }

    public boolean isIrritant() {
        return hazards.contains(Hazard.irritant);

    }

    public void setIrritant(boolean irritant) {
        if (irritant) {
            hazards.add(Hazard.irritant);
        } else {
            hazards.remove(Hazard.irritant);
        }
    }

    public boolean isUnhealthy() {
        return hazards.contains(Hazard.unhealthy);

    }

    public void setUnhealthy(boolean unhealthy) {
        if (unhealthy) {
            hazards.add(Hazard.unhealthy);
        } else {
            hazards.remove(Hazard.unhealthy);
        }
    }

    public boolean isEnvironmentallyHazardous() {
        return hazards.contains(Hazard.environmentallyHazardous);

    }

    public void setEnvironmentallyHazardous(boolean environmentallyHazardous) {
        if (environmentallyHazardous) {
            hazards.add(Hazard.environmentallyHazardous);
        } else {
            hazards.remove(Hazard.environmentallyHazardous);
        }
    }

    public boolean isDanger() {
        return hazards.contains(Hazard.danger);

    }

    public void setDanger(boolean danger) {
        if (danger) {
            hazards.add(Hazard.danger);
        } else {
            hazards.remove(Hazard.danger);
        }
    }

    public boolean isAttention() {
        return hazards.contains(Hazard.attention);

    }

    public void setAttention(boolean attention) {
        if (attention) {
            hazards.add(Hazard.attention);
        } else {
            hazards.remove(Hazard.attention);
        }
    }

    public String getHazardStatements() {
        return hazardStatements;
    }

    public void setHazardStatements(String hazardStatements) {
        this.hazardStatements = hazardStatements;
    }

    public String getPrecautionaryStatements() {
        return precautionaryStatements;
    }

    public void setPrecautionaryStatements(String precautionaryStatements) {
        this.precautionaryStatements = precautionaryStatements;
    }

    public List<HazardsMaterialsEntity> createDBInstances(int materialId) {
        List<HazardsMaterialsEntity> entities = new ArrayList<>();
        for (Hazard h : hazards) {
            entities.add(
                    new HazardsMaterialsEntity(
                            new HazardMaterialId(
                                    h.getTypeId(),
                                    materialId), ""));

        }
        if (hazardStatements != null && !hazardStatements.isEmpty()) {
            entities.add(
                    new HazardsMaterialsEntity(
                            new HazardMaterialId(
                                    12,
                                    materialId), hazardStatements));
        }
        if (precautionaryStatements != null && !precautionaryStatements.isEmpty()) {
            entities.add(
                    new HazardsMaterialsEntity(
                            new HazardMaterialId(
                                    13,
                                    materialId), precautionaryStatements));
        }

        return entities;
    }

    public Set<Hazard> getHazards() {
        return hazards;
    }

    public void setHazards(Set<Hazard> hazards) {
        this.hazards = hazards;
    }

    public static HazardInformation createObjectFromDbEntity(List<HazardsMaterialsEntity> dbes) {
        HazardInformation hi = new HazardInformation();
        for (HazardsMaterialsEntity dbe : dbes) {
            switch (dbe.getId().getTypeID()) {
                case 12:
                    hi.setHazardStatements(dbe.getRemarks());
                    break;
                case 13:
                    hi.setPrecautionaryStatements(dbe.getRemarks());
                    break;
                default:
                    hi.getHazards().add(Hazard.getHazardById(dbe.getId().getTypeID()));
                    break;
            }
        }
        return hi;
    }

    public HazardInformation copy() {
        HazardInformation copy = new HazardInformation();
        copy.setHazardStatements(hazardStatements);
        copy.setPrecautionaryStatements(precautionaryStatements);
        copy.getHazards().addAll(hazards);
        return copy;
    }

}
