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

import static de.ipb_halle.lbac.util.units.Quality.MASS;
import static de.ipb_halle.lbac.util.units.Quality.MASS_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.MOLAR_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.VOLUME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.calculation.MassConcentrationCalculations;
import de.ipb_halle.lbac.util.calculation.MolarConcentrationCalculations;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * 
 * @author flange
 */
@SessionScoped
@Named
public class CreateSolutionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private transient MessagePresenter messagePresenter;

    private Item parentItem;
    private Quantity molarMassFromItem;
    private List<Unit> availableConcentrationUnits; // r
    private List<Unit> availableVolumeUnits = Unit.getVisibleUnitsOfQuality(VOLUME); // r
    private List<Unit> availableMassUnits = Unit.getVisibleUnitsOfQuality(MASS); // r

    // step (1)
    private Double targetConcentration; // rw
    private Unit targetConcentrationUnit; // rw
    private Double targetVolume; // rw
    private Unit targetVolumeUnit = Unit.getUnit("ml"); // rw
    private Double targetMass; // r
    private Unit targetMassUnit; // rw
    private boolean userChangedMassUnit = false; // rw
    private Double availableMassFromItem; // r
    private Unit availableMassFromItemUnit; // r

    @PostConstruct
    public void init() {
        resetValues();
    }

    private void resetValues() {
        parentItem = null;
        molarMassFromItem = null;
        availableConcentrationUnits = Collections.emptyList();

        targetConcentration = null;
        targetConcentrationUnit = null;
        targetVolume = null;
        targetMass = null;
        targetMassUnit = null;
        userChangedMassUnit = false;
        availableMassFromItem = null;
        availableMassFromItemUnit = null;
    }

    /*
     * Actions
     */
    public void actionStartCreateSolution(Item item) {
        resetValues();
        parentItem = item;

        setMolarMassFromParentItem();
        loadAvailableConcentrationUnits();

        Quantity massFromItem = massFromItem();
        if (massFromItem != null) {
            targetMassUnit = massFromItem.getUnit();
            availableMassFromItem = massFromItem.getValue();
            availableMassFromItemUnit = massFromItem.getUnit();
        }
    }

    private void setMolarMassFromParentItem() {
        Material materialOfItem = parentItem.getMaterial();
        if (materialOfItem == null) {
            return;
        }
        if (!MaterialType.STRUCTURE.equals(materialOfItem.getType())) {
            return;
        }
        molarMassFromItem = ((Structure) materialOfItem).getAverageMolarMassAsQuantity();
    }

    private void loadAvailableConcentrationUnits() {
        List<Unit> massConcentrations = Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION);
        List<Unit> molarConcentrations = Unit.getVisibleUnitsOfQuality(MOLAR_CONCENTRATION);

        availableConcentrationUnits = new ArrayList<>();
        if (molarMassFromItem != null) {
            availableConcentrationUnits.addAll(molarConcentrations);
            availableConcentrationUnits.addAll(massConcentrations);
            targetConcentrationUnit = Unit.getUnit("mM");
        } else {
            availableConcentrationUnits.addAll(massConcentrations);
            targetConcentrationUnit = Unit.getUnit("g/l");
        }
    }

    private Quantity massFromItem() {
        return parentItem.getAmountAsQuantity();
    }

    public void actionUpdateTargetMass() {
        targetMass = null;

        Quantity concentration = targetConcentrationAsQuantity();
        Quantity volume = targetVolumeAsQuantity();

        if ((concentration == null) || (volume == null)) {
            return;
        }

        Quantity targetMassAsQuantity = calculateTargetMass(concentration, volume, targetMassUnit);
        if (!userChangedMassUnit) {
            targetMassAsQuantity = targetMassAsQuantity.toHumanReadableUnit(Unit.getVisibleUnitsOfQuality(MASS));
        }
        targetMass = targetMassAsQuantity.getValue();
        targetMassUnit = targetMassAsQuantity.getUnit();

        Quantity massFromItem = massFromItem();
        if (targetMassAsQuantity.isGreaterThanOrEqualTo(massFromItem)) {
            messagePresenter.error("itemCreateSolution_error_targetMassTooHigh");
        }
    }

    private Quantity calculateTargetMass(Quantity concentration, Quantity volume, Unit targetMassUnit) {
        if ((molarMassFromItem == null) || (MASS_CONCENTRATION == concentration.getUnit().getQuality())) {
            return MassConcentrationCalculations.calculateMass(concentration, volume, targetMassUnit);
        } else {
            return MolarConcentrationCalculations.calculateMass(concentration, molarMassFromItem, volume,
                    targetMassUnit);
        }
    }

    private Quantity targetConcentrationAsQuantity() {
        if ((targetConcentration == null) || (targetConcentrationUnit == null)) {
            return null;
        }
        return new Quantity(targetConcentration, targetConcentrationUnit);
    }

    private Quantity targetVolumeAsQuantity() {
        if ((targetVolume == null) || (targetVolumeUnit == null)) {
            return null;
        }
        return new Quantity(targetVolume, targetVolumeUnit);
    }

    /*
     * PrimeFaces wizard
     */
    static final String STEP1 = "step1_inputConcAndVol";
    static final String STEP2 = "step2_weigh";

    public String onFlowProcess(FlowEvent event) {
        if (STEP2.equals(event.getNewStep())) {
//            Quantity targetMass = calculateTargetMass();
//            Quantity massFromItem = massFromItem();
//
//            if (massFromItem.isGreaterThanOrEqualTo(targetMass)) {
//                // ok for step 2
//                return STEP2;
//            } else {
//                // FacesMessage ...
//                return STEP1;
//            }
        }

        return event.getNewStep();
    }

    /*
     * Getters/setters
     */
    public List<Unit> getAvailableConcentrationUnits() {
        return availableConcentrationUnits;
    }

    public List<Unit> getAvailableVolumeUnits() {
        return availableVolumeUnits;
    }

    public List<Unit> getAvailableMassUnits() {
        return availableMassUnits;
    }

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

    public Double getTargetMass() {
        return targetMass;
    }

    public Unit getTargetMassUnit() {
        return targetMassUnit;
    }

    public void setTargetMassUnit(Unit targetMassUnit) {
        this.targetMassUnit = targetMassUnit;
    }

    public boolean isUserChangedMassUnit() {
        return userChangedMassUnit;
    }

    public void setUserChangedMassUnit(boolean userChangedMassUnit) {
        this.userChangedMassUnit = userChangedMassUnit;
    }

    public Double getAvailableMassFromItem() {
        return availableMassFromItem;
    }

    public Unit getAvailableMassFromItemUnit() {
        return availableMassFromItemUnit;
    }
}