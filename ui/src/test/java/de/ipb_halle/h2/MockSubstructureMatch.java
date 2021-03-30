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
package de.ipb_halle.h2;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;



/**
 * Stored procedure for mocking substructure matching in the H2 
 * database.
 *
 * @author fbroda
 */
public class MockSubstructureMatch {

    public static boolean substructure(String structure, String substructure) {

        try {
            ReaderFactory factory = new ReaderFactory();
            ISimpleChemObjectReader reader = factory.createReader(
                    new StringReader(structure));
            IAtomContainer cdkMolecule = reader.read(new AtomContainer());
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkMolecule);

            reader = factory.createReader(new StringReader(substructure));
            IAtomContainer cdkSub = reader.read(new AtomContainer());
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkSub);

            Pattern pattern = Pattern.findSubstructure(cdkSub);
            return pattern.matches(cdkMolecule);

        } catch(Exception e) {
        }
        return false;
    }

    @Test
    public void testSubstructure() {
        assertTrue("is substructure", substructure("C(Cl)CCCOCC", "CCOCC"));
        assertFalse("is not substructure", substructure("CCC(CC)CCCCCC", "CCOCC"));
    }
}
