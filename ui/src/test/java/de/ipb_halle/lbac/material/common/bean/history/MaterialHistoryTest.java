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
package de.ipb_halle.lbac.material.common.bean.history;

import de.ipb_halle.lbac.material.common.history.MaterialHistory;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.material.common.history.MaterialOverviewDifference;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class MaterialHistoryTest {

    @Test
    public void test01_addMaterialDifferences() {

        MaterialHistory hist = new MaterialHistory();
        Calendar c = Calendar.getInstance();
        c.set(2019, 2, 3, 12, 13, 25);
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        Date d_20190203 = c.getTime();
        MaterialOverviewDifference diff1 = new MaterialOverviewDifference();
        diff1.initialise(1, userId1, d_20190203);
        diff1.setProjectIdNew(2);
        diff1.setProjectIdOld(1);

        MaterialStructureDifference diff2 = new MaterialStructureDifference();
        diff2.initialise(1, userId1, d_20190203);
        diff2.setMolarMass_new(2d);
        diff2.setMolarMass_new(1d);

        c.set(2018, 2, 3, 12, 13, 25);
        Date d_20180203 = c.getTime();
        MaterialOverviewDifference diff3 = new MaterialOverviewDifference();
        diff3.initialise(1, userId2, d_20180203);

        c.set(2015, 2, 3, 12, 13, 25);
        Date d_20150203 = c.getTime();
        MaterialOverviewDifference diff4 = new MaterialOverviewDifference();
        diff4.initialise(1, userId2, d_20150203);

        hist.addDifference(diff3);
        hist.addDifference(diff2);
        hist.addDifference(diff1);
        hist.addDifference(diff4);

        SortedMap<Date, List<MaterialDifference>> result = hist.getChanges();
        Assert.assertEquals(1, result.get(result.firstKey()).size());
        Assert.assertEquals(2, result.get(result.lastKey()).size());

        Assert.assertEquals(d_20150203, hist.getPreviousKey(d_20180203));
        Assert.assertEquals(d_20180203, hist.getPreviousKey(d_20190203));
        Assert.assertNull(hist.getPreviousKey(d_20150203));

        Assert.assertEquals(d_20180203, hist.getFollowingKey(d_20150203));
        Assert.assertEquals(d_20190203, hist.getFollowingKey(d_20180203));
        Assert.assertNull(hist.getFollowingKey(d_20190203));

    }
}
