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
package de.ipb_halle.lbac.search.lang;

import de.ipb_halle.lbac.entity.NodeEntity;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class OrderByTest {
    
    @Test
    public void test001_orderBy(){
        EntityGraph graph = new EntityGraph(ItemEntity.class);
        SqlBuilder builder = new SqlBuilder(graph);

        DbField labelField = new DbField()
                .setColumnName("id")
                .setTableName("items")
                .setOrderDirection(OrderDirection.DESC);
        
        List<DbField> orderList = new ArrayList<> ();
        orderList.add(labelField);

        String sql = builder.query(null, orderList);
        sql=sql.replaceAll("\n", "");
        assertEquals("Invalid ORDER BY clause", "SELECT DISTINCT a.solventid, a.aclist_id, a.amount, a.purity, a.owner_id, a.expiry_date, a.articleid, a.description, a.containersize, a.concentration, a.label, a.materialid, a.containertype, a.unit, a.ctime, a.id, a.containerid, a.projectid, a.concentrationunit FROM items AS a ORDER BY a.id DESC ", sql);
    }
    
}
