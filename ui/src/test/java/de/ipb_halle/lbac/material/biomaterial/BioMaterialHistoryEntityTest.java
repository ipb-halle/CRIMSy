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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.material.common.history.HistoryEntityId;
import static de.ipb_halle.lbac.material.common.history.HistoryEntityId_.mdate;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class BioMaterialHistoryEntityTest {

    @Test
    public void test001_entityTest() {
        Date date = new Date();
        int id = 1;
        int userId = 2;
        int taxoIdOld = 3;
        int taxoIdNew = 4;
        int tissueOld = 5;
        int tissueNew = 6;
        BioMaterialHistoryEntity entity = new BioMaterialHistoryEntity();
        entity.setAction("EDIT");
        entity.setDigest("DIGEST");
        entity.setId(new HistoryEntityId(id, date, userId));
        entity.setTaxoid_new(taxoIdNew);
        entity.setTaxoid_old(taxoIdOld);
        entity.setTissueid_old(tissueOld);
        entity.setTissueid_new(tissueNew);

        Assert.assertEquals("EDIT", entity.getAction());
        Assert.assertEquals("DIGEST", entity.getDigest());
        Assert.assertEquals(userId, entity.getId().getActorid(), 0);
        Assert.assertEquals(id, entity.getId().getId(), 0);
        Assert.assertEquals(date, entity.getId().getMdate());
        Assert.assertEquals(taxoIdNew, entity.getTaxoid_new(), 0);
        Assert.assertEquals(taxoIdOld, entity.getTaxoid_old(), 0);
        Assert.assertEquals(tissueNew, entity.getTissueid_new(), 0);
        Assert.assertEquals(tissueOld, entity.getTissueid_old(), 0);

    }
}
