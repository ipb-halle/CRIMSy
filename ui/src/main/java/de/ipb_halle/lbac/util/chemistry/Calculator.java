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
package de.ipb_halle.lbac.util.chemistry;

import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import java.io.StringReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.iterator.IteratingSMILESReader;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 *
 * @author fmauz
 */
public class Calculator {

    private Logger logger = LogManager.getLogger(this.getClass());

    public StructureInformation calculate(StructureInformation structure) {
        ChemistryParser parser = new ChemistryParser();

        try {
            IAtomContainer cdkMolecule = parser.parseMolecule(structure.getStructureModel());

            IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
            IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula(cdkMolecule);

            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkMolecule);
            CDKHydrogenAdder.getInstance(builder).addImplicitHydrogens(cdkMolecule);
            Kekulization.kekulize(cdkMolecule);

            structure.setSumFormula(MolecularFormulaManipulator.getString(cdkFormula));
            structure.setAverageMolarMass(MolecularFormulaManipulator.getMass(cdkFormula,
                    MolecularFormulaManipulator.MolWeight));
            structure.setExactMolarMass(MolecularFormulaManipulator.getMass(cdkFormula,
                    MolecularFormulaManipulator.MonoIsotopic));

            // ToDo: calculate InChIKey
        } catch (Exception e) {
            this.logger.warn("calculate caught and IOException", (Throwable) e);
        }
        return structure;
    }

    public Structure calculate(Structure structure) {
        StructureInformation info = new StructureInformation();
        info.setStructureModel(structure.getMolecule().getStructureModel());
        calculate(info);
        structure.setAverageMolarMass(info.getAverageMolarMass());
        structure.setExactMolarMass(info.getExactMolarMass());
        structure.setSumFormula(info.getSumFormula());
        return structure;
    }
}
