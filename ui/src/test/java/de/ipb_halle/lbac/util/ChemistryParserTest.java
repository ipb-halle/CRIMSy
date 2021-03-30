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
import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertNotNull(molecule);
    }

    @Test
    public void parseMDLV2000Model() throws IOException, CDKException {
        IAtomContainer molecule = parser.parseMolecule(benzene);
        Assert.assertNotNull(molecule);
    }

    @Test
    public void parseMDLV3000Model() throws IOException, CDKException {
        IAtomContainer molecule = parser.parseMolecule(lAlanine);
        Assert.assertNotNull(molecule);
    }

    private String benzene = "\n" + "  Marvin  10310613082D          \n" + "\n"
            + "  6  6  0  0  0  0            999 V2000\n"
            + "    0.7145   -0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.0000   -0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.7145    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   -0.7145   -0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "   -0.7145    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "  2  1  2  0  0  0  0\n" + "  3  1  1  0  0  0  0\n"
            + "  4  3  2  0  0  0  0\n" + "  5  2  1  0  0  0  0\n"
            + "  6  4  1  0  0  0  0\n" + "  5  6  2  0  0  0  0\n" + "M  END\n"
            + "";

    private String lAlanine
            = "\n"
            + "L-Alanine \n"
            + "GSMACCS-II07189510252D 1 0.00366 0.00000 0 \n"
            + "Figure 1, J. Chem. Inf. Comput. Sci., Vol 32, No. 3., 1992 \n "
            + "0 0 0 0 0 999 V3000 \n"
            + "M V30 BEGIN CTAB \n"
            + "M V30 COUNTS 6 5 0 0 1 \n"
            + "M V30 BEGIN ATOM\n"
            + "M V30 1 C -0.6622 0.5342 0 0 CFG=2\n"
            + "M V30 2 C 0.6622 -0.3 0 0\n"
            + "M V30 3 C -0.7207 2.0817 0 0 MASS=13\n"
            + "M V30 4 N -1.8622 -0.3695 0 0 CHG=1\n"
            + "M V30 5 O 0.622 -1.8037 0 0\n"
            + "M V30 6 O 1.9464 0.4244 0 0 CHG=-1\n"
            + "M V30 END ATOM \n"
            + "M V30 BEGIN BOND\n"
            + "M V30 1 1 1 2\n"
            + "M V30 2 1 1 3 CFG=1\n"
            + "M V30 3 1 1 4\n"
            + "M V30 4 2 2 5\n"
            + "M V30 5 1 2 6\n"
            + "M V30 END BOND \n"
            + "M V30 END CTAB\n"
            + "M END";

}
