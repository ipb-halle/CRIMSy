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

import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author fblocal
 */
public class SqlInsertBuilderTest {
    @Test
    public void testInsert() {
        MaterialEntity mat = new MaterialEntity();
        
        EntityGraph graph = new EntityGraph(mat.getClass());
        SqlInsertBuilder builder = new SqlInsertBuilder(graph);
        
        // assertEquals("Insert statement does not match", builder.getInsertSql(), "INSERT INTO materials (bla, blub) VALUES (?,?)");
        String stmnt = builder.getInsertSql();
        assertTrue("Insert statement starts ...", stmnt.startsWith("INSERT INTO materials ("));
        assertTrue("Insert statement ends ...", stmnt.endsWith("?, ?, ?, ?)"));
    }
}
