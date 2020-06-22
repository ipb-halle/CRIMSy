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
package de.ipb_halle.lbac.material.common.bean.history;

import de.ipb_halle.lbac.material.common.Hazard;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.common.bean.mock.ProjectBeanMock;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.history.MaterialHazardDifference;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
import java.util.Calendar;
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
public class HistoryOperationHazardsTest {

    List<IndexEntry> indices;
    Structure s;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    MaterialIndexDifference mid;
    MaterialIndexBean mib;

    @Before
    public void init() {
        indices = new ArrayList<>();
        s = new Structure("H2O", 0, 0, 0, new ArrayList<>(), 0, new HazardInformation(), new StorageClassInformation(), new Molecule("h2o", 0));
        currentDate = new Date();
        mes = new MaterialEditState();
        mes.setMaterialBeforeEdit(s);

        mes.setCurrentVersiondate(currentDate);
        mib = new MaterialIndexBean();
        s.setIndices(indices);
        mid = new MaterialIndexDifference();
        mid.initialise(0, UUID.randomUUID(), currentDate);
        instance = new HistoryOperation(mes, new ProjectBeanMock(), new MaterialNameBean(), mib, new StructureInformation(), new StorageClassInformation());
    }

    /**
     * Description: The material is created without a Hazard. After that the
     * Hazard 'corrosive' is added and then at a later time the hazards
     * 'irritant' and 'unhealthy' are added and 'corrosive' removed, h and
     * p-statements are added.
     */
    @Test
    public void test01_HazardDifferenceOperations() {
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        UUID userID = UUID.randomUUID();

        //First edit: add corrosive
        MaterialHazardDifference d1 = new MaterialHazardDifference();
        d1.addDifference(null, Hazard.corrosive.getTypeId(), null, null);
        c.add(Calendar.MONTH, -2);
        Date date1 = c.getTime();
        d1.initialise(s.getId(), userID, date1);
        s.getHistory().addDifference(d1);

        //Second edit: add irritant and unhealthy, remove corrosive and 
        // add a precautionary statement with a remark
        MaterialHazardDifference d2 = new MaterialHazardDifference();
        d2.addDifference(null, Hazard.unhealthy.getTypeId(), null, null);
        d2.addDifference(null, Hazard.irritant.getTypeId(), null, null);
        d2.addDifference(null, 12, null, "h - test statement");
        d2.addDifference(null, 13, null, "p - test statement");
        d2.addDifference(Hazard.corrosive.getTypeId(), null, null, null);
        c.add(Calendar.MONTH, 1);
        Date date2 = c.getTime();
        d2.initialise(s.getId(), userID, date2);
        s.getHistory().addDifference(d2);

        //create current state 
        mes.getHazards().getHazards().add(Hazard.irritant);
        mes.getHazards().getHazards().add(Hazard.unhealthy);
        mes.getHazards().setPrecautionaryStatements("p - test statement");
        mes.getHazards().setHazardStatements("h - test statement");
        mes.setCurrentVersiondate(date2);

        // apply second edit: (irritant,unhealthy) -> (corrosive)
        instance.applyNextNegativeDifference();
        Assert.assertEquals(1, mes.getHazards().getHazards().size());
        Assert.assertNull(mes.getHazards().getPrecautionaryStatements());
        Assert.assertNull(mes.getHazards().getHazardStatements());
        Assert.assertTrue(mes.getHazards().getHazards().contains(Hazard.corrosive));

        // apply first edit: (corrosive) -> ( )
        instance.applyNextNegativeDifference();
        Assert.assertTrue(mes.getHazards().getHazards().isEmpty());
        Assert.assertNull(mes.getHazards().getPrecautionaryStatements());
        Assert.assertNull(mes.getHazards().getHazardStatements());

        // apply first edit: ( )  -> (corrosive) 
        instance.applyNextPositiveDifference();
        Assert.assertEquals(1, mes.getHazards().getHazards().size());
        Assert.assertTrue(mes.getHazards().getHazards().contains(Hazard.corrosive));
        Assert.assertNull(mes.getHazards().getPrecautionaryStatements());
        Assert.assertNull(mes.getHazards().getHazardStatements());

        // apply second edit: (corrosive) -> (irritant,unhealthy)
        instance.applyNextPositiveDifference();
        Assert.assertEquals(2, mes.getHazards().getHazards().size());
        Assert.assertTrue(mes.getHazards().getHazards().contains(Hazard.irritant));
        Assert.assertTrue(mes.getHazards().getHazards().contains(Hazard.unhealthy));
        Assert.assertEquals("p - test statement", mes.getHazards().getPrecautionaryStatements());
        Assert.assertEquals("h - test statement", mes.getHazards().getHazardStatements());

    }

}
