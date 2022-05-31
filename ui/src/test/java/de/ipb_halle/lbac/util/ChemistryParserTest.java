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
package de.ipb_halle.lbac.util;

import de.ipb_halle.lbac.util.chemistry.ChemistryParser;
import java.io.IOException;
import java.util.stream.StreamSupport;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 *
 * @author fmauz
 */
public class ChemistryParserTest {

    private ChemistryParser parser = new ChemistryParser();

    @Test
    public void parseSmilesModel() throws IOException, CDKException {
        IAtomContainer molecule = parser.parseMolecule("CCOCC");
        Assert.assertEquals(5, StreamSupport.stream(molecule.atoms().spliterator(), false).count());

    }

    @Test
    public void parseMDLV2000Model() throws IOException, CDKException {
        String benzene = ResourceUtils.readResourceFile("molfiles/Benzene.mol");

        IAtomContainer molecule = parser.parseMolecule(benzene);
        Assert.assertEquals(6, StreamSupport.stream(molecule.atoms().spliterator(), false).count());
    }

    @Test
    public void parseMDLV3000Model() throws IOException, CDKException {
        IAtomContainer molecule = parser.parseMolecule(v3000Model);
        Assert.assertEquals(31, StreamSupport.stream(molecule.atoms().spliterator(), false).count());
    }

    private String v3000Model
            = " \n   "
            + "   10040611432D 0   0.00000     0.00000     0\n"
            + "HDR: RoxyName (Build 030), Copyright (c) 2001-2005. All rights reserved.\n"
            + "  0  0  0     0  0            999 V3000\n"
            + "M  V30 BEGIN CTAB\n"
            + "M  V30 COUNTS 31 34 0 0 0\n"
            + "M  V30 BEGIN ATOM\n"
            + "M  V30 1 C 10.4341 5.1053 0 0 CFG=1\n"
            + "M  V30 2 C 6.70192 2.9471 0 0 CFG=1\n"
            + "M  V30 3 C 6.70192 1.4823 0 0\n"
            + "M  V30 4 C 10.4341 3.6779 0 0 CFG=2\n"
            + "M  V30 5 C 7.94492 3.6779 0 0 CFG=2\n"
            + "M  V30 6 C 9.18962 2.9471 0 0 CFG=1\n"
            + "M  V30 7 C 11.6773 5.8376 0 0 CFG=2\n"
            + "M  V30 8 C 7.94492 0.7874 0 0\n"
            + "M  V30 9 C 9.18962 5.8376 0 0\n"
            + "M  V30 10 C 9.18962 1.5197 0 0\n"
            + "M  V30 11 C 7.94492 5.1053 0 0\n"
            + "M  V30 12 C 12.9579 3.6779 0 0\n"
            + "M  V30 13 C 5.45732 3.6779 0 0\n"
            + "M  V30 14 C 12.9579 5.1412 0 0\n"
            + "M  V30 15 C 5.45732 0.75 0 0\n"
            + "M  V30 16 C 11.6516 7.33738 0 0 CFG=1\n"
            + "M  V30 17 C 10.4338 6.6053 0 0\n"
            + "M  V30 18 C 4.17682 2.9471 0 0\n"
            + "M  V30 19 C 4.17682 1.5197 0 0 CFG=2\n"
            + "M  V30 20 C 6.7015 4.4471 0 0\n"
            + "M  V30 21 O 2.87123 0.781175 0 0\n"
            + "M  V30 22 C 12.912 9.60929 0 0\n"
            + "M  V30 23 C 12.9376 8.10952 0 0\n"
            + "M  V30 24 C 10.3399 8.06502 0 0\n"
            + "M  V30 25 C 14.198 10.3814 0 0\n"
            + "M  V30 26 C 14.1723 11.8812 0 0 CFG=3\n"
            + "M  V30 27 C 15.4583 12.6533 0 0\n"
            + "M  V30 28 C 12.8606 12.6089 0 0\n"
            + "M  V30 29 H 10.8277 2.23045 0 0\n"
            + "M  V30 30 H 7.94492 2.1787 0 0\n"
            + "M  V30 31 H 9.18962 4.4462 0 0\n"
            + "M  V30 END ATOM\n"
            + "M  V30 BEGIN BOND\n"
            + "M  V30 1 1 1 4\n"
            + "M  V30 2 1 1 7\n"
            + "M  V30 3 1 1 9\n"
            + "M  V30 4 1 1 17 CFG=1\n"
            + "M  V30 5 1 2 3\n"
            + "M  V30 6 1 2 5\n"
            + "M  V30 7 1 2 13\n"
            + "M  V30 8 1 2 20 CFG=1\n"
            + "M  V30 9 2 3 8\n"
            + "M  V30 10 1 3 15\n"
            + "M  V30 11 1 4 6\n"
            + "M  V30 12 1 4 12\n"
            + "M  V30 13 1 5 6\n"
            + "M  V30 14 1 5 11\n"
            + "M  V30 15 1 6 10\n"
            + "M  V30 16 1 7 14\n"
            + "M  V30 17 1 7 16 CFG=1\n"
            + "M  V30 18 1 8 10\n"
            + "M  V30 19 1 9 11\n"
            + "M  V30 20 1 12 14\n"
            + "M  V30 21 1 13 18\n"
            + "M  V30 22 1 15 19\n"
            + "M  V30 23 1 16 23\n"
            + "M  V30 24 1 16 24 CFG=3\n"
            + "M  V30 25 1 18 19\n"
            + "M  V30 26 1 19 21 CFG=1\n"
            + "M  V30 27 1 22 23\n"
            + "M  V30 28 1 22 25\n"
            + "M  V30 29 1 25 26\n"
            + "M  V30 30 1 26 27\n"
            + "M  V30 31 1 26 28\n"
            + "M  V30 32 1 4 29 CFG=3\n"
            + "M  V30 33 1 5 30 CFG=3\n"
            + "M  V30 34 1 6 31 CFG=1\n"
            + "M  V30 END BOND\n"
            + "M  V30 BEGIN COLLECTION\n"
            + "M  V30 MDLV30/STEABS ATOMS=(8 1 2 4 5 6 7 16 19)\n"
            + "M  V30 END COLLECTION\n"
            + "M  V30 END CTAB\n"
            + "M  END";

}
