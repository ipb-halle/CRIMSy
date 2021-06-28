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

import java.io.IOException;
import java.io.StringReader;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.iterator.IteratingSMILESReader;

/**
 *
 * @author fmauz
 */
public class ChemistryParser {

    public IAtomContainer parseMolecule(String moleculeString) throws IOException, CDKException {
        IAtomContainer cdkMolecule;

        ReaderFactory factory = new ReaderFactory();
        try (ISimpleChemObjectReader reader = factory.createReader(
                new StringReader(
                        moleculeString))) {
            if (reader == null) {
                IteratingSMILESReader ireader = new IteratingSMILESReader(
                        new StringReader(moleculeString),
                        DefaultChemObjectBuilder.getInstance());

                cdkMolecule = ireader.next();
            } else {
                cdkMolecule = reader.read(new AtomContainer());
            }

            return cdkMolecule;
        }
    }
}
