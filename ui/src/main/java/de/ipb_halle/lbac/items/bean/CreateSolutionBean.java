/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.units.Quality;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * 
 * @author flange
 */
@SessionScoped
@Named
public class CreateSolutionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Item parentItem;
    private Double targetConcentration;
    private Unit targetConcentrationUnit;
    private List<Unit> availableConcentrationUnits;
    private Double targetVolume;
    private Unit targetVolumeUnit;
    private List<Unit> availableVolumeUnits;

    @PostConstruct
    public void init() {
        resetValues();
    }

    private void resetValues() {
        parentItem = null;
        targetConcentration = null;
        targetConcentrationUnit = null;
        availableConcentrationUnits = Collections.emptyList();
        targetVolume = null;
        targetVolumeUnit = null;
        availableVolumeUnits = Collections.emptyList();
    }

    /*
     * Actions
     */
    public void actionStartCreateSolution(Item item) {
        resetValues();
        parentItem = item;

        loadAvailableConcentrationUnits();

        loadAvailableVolumeUnits();
        targetVolumeUnit = Unit.getUnit("ml");
    }

    private void loadAvailableConcentrationUnits() {
        Double molarMass = molarMassFromItem(parentItem);
        List<Unit> massConcentrations = Unit.getUnitsOfQuality(Quality.MASS_CONCENTRATION);
        List<Unit> molarConcentrations = Unit.getUnitsOfQuality(Quality.MOLAR_CONCENTRATION);

        availableConcentrationUnits = new ArrayList<>();
        if ((molarMass != null) && (molarMass > 0d)) {
            availableConcentrationUnits.addAll(molarConcentrations);
            availableConcentrationUnits.addAll(massConcentrations);
            targetConcentrationUnit = Unit.getUnit("mM");
        } else {
            availableConcentrationUnits.addAll(massConcentrations);
            targetConcentrationUnit = Unit.getUnit("g/l");
        }
    }

    private Double molarMassFromItem(Item item) {
        Material materialOfItem = item.getMaterial();
        if (materialOfItem == null) {
            return null;
        }
        if (!MaterialType.STRUCTURE.equals(materialOfItem.getType())) {
            return null;
        }
        return ((Structure) materialOfItem).getAverageMolarMass();
    }

    private void loadAvailableVolumeUnits() {
        availableVolumeUnits = Unit.getUnitsOfQuality(Quality.VOLUME);
    }

    /*
     * PrimeFaces wizard
     */
    public String onFlowProcess(FlowEvent event) {
        return event.getNewStep();
    }

    /*
     * Getters/setters
     */
    public Double getTargetConcentration() {
        return targetConcentration;
    }

    public void setTargetConcentration(Double concentration) {
        this.targetConcentration = concentration;
    }

    public Unit getTargetConcentrationUnit() {
        return targetConcentrationUnit;
    }

    public void setTargetConcentrationUnit(Unit concentrationUnit) {
        this.targetConcentrationUnit = concentrationUnit;
    }

    public List<Unit> getAvailableConcentrationUnits() {
        return availableConcentrationUnits;
    }

    public Double getTargetVolume() {
        return targetVolume;
    }

    public void setTargetVolume(Double targetVolume) {
        this.targetVolume = targetVolume;
    }

    public Unit getTargetVolumeUnit() {
        return targetVolumeUnit;
    }

    public void setTargetVolumeUnit(Unit targetVolumeUnit) {
        this.targetVolumeUnit = targetVolumeUnit;
    }

    public List<Unit> getAvailableVolumeUnits() {
        return availableVolumeUnits;
    }
}