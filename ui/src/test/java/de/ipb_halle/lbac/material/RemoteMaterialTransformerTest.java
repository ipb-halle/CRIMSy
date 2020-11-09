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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.search.SearchTarget;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class RemoteMaterialTransformerTest {

    int projectId = 10;
    int materialId = 1;

    @Test
    public void test001_transformStructure() {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("test001_name1", "de", 1));
        names.add(new MaterialName("test001_name2", "en", 1));
        Structure struc1 = new Structure("CO2", 1d, 1d, materialId, names, projectId);
        struc1.getIndices().add(new IndexEntry(3, "test001_index_id_3", "de"));

        RemoteMaterialTransformer transformer = new RemoteMaterialTransformer(struc1);
        RemoteMaterial remoteMat = transformer.transformToRemote(struc1);

        Assert.assertEquals(2, remoteMat.names.size());
        Assert.assertEquals("test001_name1", remoteMat.names.get(0));
        Assert.assertEquals("test001_name2", remoteMat.names.get(1));
        Assert.assertEquals(1, remoteMat.getIndices().size());
        Assert.assertEquals("test001_index_id_3", remoteMat.getIndices().get(3));
        Assert.assertEquals("CO2", remoteMat.getSumFormula());
        Assert.assertEquals(SearchTarget.MATERIAL, remoteMat.getTypeToDisplay().getGeneralType());
        Assert.assertEquals(MaterialType.STRUCTURE, remoteMat.getTypeToDisplay().getMaterialType());
        Assert.assertNull(remoteMat.getMoleculeString());

        struc1.setMolecule(new Molecule("MOLECULE", 100));
        transformer = new RemoteMaterialTransformer(struc1);
        remoteMat = transformer.transformToRemote(struc1);

        Assert.assertEquals("MOLECULE", remoteMat.getMoleculeString());

    }
}
