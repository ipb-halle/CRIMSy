/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.bean.history;

import de.ipb_halle.lbac.material.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.bean.manipulation.MaterialEditState;
import de.ipb_halle.lbac.material.bean.history.HistoryOperation;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.material.bean.mock.ProjectBeanMock;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.difference.MaterialIndexDifference;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class HistoryOperationStructureTest {

    StructureInformation strucInfo;
    Structure s;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    MaterialStructureDifference sdiff;

    ;

    @Before
    public void init() {

        s = new Structure("H2O", 0, 0, 0, new ArrayList<>(), 0, new HazardInformation(), new StorageClassInformation(), new Molecule("h2o", 0));
        currentDate = new Date();
        mes = new MaterialEditState();
        mes.setMaterialBeforeEdit(s);
        mes.setCurrentVersiondate(currentDate);
        strucInfo = new StructureInformation();

        instance = new HistoryOperation(mes, new ProjectBeanMock(), new MaterialNameBean(), new MaterialIndexBean(), strucInfo, new StorageClassInformation());
    }

    @Test
    public void test01_strcutureDifferenceOperations() {

        //################
        //Testcase 1: remove a molecule from the strucutre
        sdiff = new MaterialStructureDifference();
        sdiff.setModificationTime(currentDate);
        Molecule m = new Molecule("H20", 1);
        m.setModelType(Molecule.MoleculeFormat.V2000);

        strucInfo.setMolarMass(12d);

        strucInfo.setMolarMass(12d);
        strucInfo.setExactMolarMass(11d);
        strucInfo.setSumFormula("H2O");
        strucInfo.setStructureModel("xxx-xxx");
        sdiff.setMoleculeId_old(null);
        sdiff.setMoleculeId_new(m);

        s.getHistory().addDifference(sdiff);
        instance.applyNextNegativeDifference();

        Assert.assertNull("Testcase 1 - no molecule found", strucInfo.getStructureModel());
    }

}
