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

import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.util.ResourceUtils;

/**
 *
 * @author fmauz
 */
public class MoleculeTest {
    @Test
    public void test001_checkEmptyMolecule() throws IOException {
        String benzene = ResourceUtils.readResourceFile("molfiles/Benzene.mol");
        String empty = ResourceUtils.readResourceFile("molfiles/Empty.mol");

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
