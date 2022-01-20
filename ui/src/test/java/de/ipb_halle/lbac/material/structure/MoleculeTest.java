/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.structure;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class MoleculeTest {

    String benzene = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
            + "  6  6  0  0  0  0  0  0  0  0999 V2000\n"
            + "    5.9375  -10.0000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    5.9375  -11.5000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    7.2365  -12.2500   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.5356  -11.5000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    8.5356  -10.0000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
            + "    7.2365   -9.2500   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + "  1  2  2  0  0  0  0\n"
            + "  2  3  1  0  0  0  0\n" + "  3  4  2  0  0  0  0\n" + "  4  5  1  0  0  0  0\n"
            + "  5  6  2  0  0  0  0\n" + "  6  1  1  0  0  0  0\n" + "M  END";
    String empty = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
            + "  0  0  0  0  0  0  0  0  0  0999 V2000\n"
            + "M  END";

    @Test
    public void test001_checkEmptyMolecule() {
        Molecule mol = new Molecule(null, 0);
        Assert.assertTrue(mol.isEmptyMolecule());
        mol = new Molecule("", 0);
        Assert.assertTrue(mol.isEmptyMolecule());
        mol = new Molecule("no valide molecule", 0);
        Assert.assertTrue(mol.isEmptyMolecule());
        mol = new Molecule(empty, 0);
        Assert.assertTrue(mol.isEmptyMolecule());
        mol = new Molecule(benzene, 0);
        Assert.assertFalse(mol.isEmptyMolecule());

    }
}
