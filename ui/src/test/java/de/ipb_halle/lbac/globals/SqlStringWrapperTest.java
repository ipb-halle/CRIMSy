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
package de.ipb_halle.lbac.globals;

import de.ipb_halle.lbac.entity.ACPermission;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SqlStringWrapperTest {

    @Test
    public void test001_aclTableJoin() {

        String originalString = "SELECT a.a,a.b FROM columns a WHERE a.id=0";

        String enchantedString = SqlStringWrapper.aclWrapper(originalString, "a.aclistid", ACPermission.permREAD);

        Assert.assertEquals(
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND (CAST(:userid AS UUID)=me.member_id OR m.ownerid=CAST(:userid AS UUID)) "
                + "AND a.id=0", enchantedString);

        originalString = "SELECT a.a,a.b FROM columns a";
        enchantedString = SqlStringWrapper.aclWrapper(originalString, "a.aclistid", ACPermission.permREAD);
        Assert.assertEquals(enchantedString.trim(),
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND (CAST(:userid AS UUID)=me.member_id OR m.ownerid=CAST(:userid AS UUID))"
        );

        originalString = "SELECT a.a,a.b FROM columns a GROUP BY a.x";
        enchantedString = SqlStringWrapper.aclWrapper(originalString, "a.aclistid", ACPermission.permREAD);
        Assert.assertEquals(
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace "
                + "ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND (CAST(:userid AS UUID)=me.member_id OR m.ownerid=CAST(:userid AS UUID)) "
                + "GROUP BY a.x", enchantedString);

        originalString = "SELECT a.a,a.b FROM columns a WHERE a=0 GROUP BY a.x";
        enchantedString = SqlStringWrapper.aclWrapper(originalString, "a.aclistid", ACPermission.permREAD, ACPermission.permEDIT);
        Assert.assertEquals(
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND ace.permedit=true "
                + "AND (CAST(:userid AS UUID)=me.member_id OR m.ownerid=CAST(:userid AS UUID)) "
                + "AND a=0 "
                + "GROUP BY a.x", enchantedString);

    }
}
