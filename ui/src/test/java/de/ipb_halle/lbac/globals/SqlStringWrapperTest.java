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

import de.ipb_halle.lbac.admission.ACPermission;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SqlStringWrapperTest {

    @Test
    public void test001_aclTableJoin() {

        String originalString = String.format(
                "SELECT a.a,a.b FROM columns a %s"
                + " WHERE %s AND a.id=0",
                SqlStringWrapper.JOIN_KEYWORD, SqlStringWrapper.WHERE_KEYWORD);
        String enchantedString = SqlStringWrapper.aclWrapper(originalString, "a.aclistid", "m.owner_id", ACPermission.permREAD);

        Assert.assertEquals(
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND (:userid=me.member_id OR m.owner_id=:userid) "
                + "AND a.id=0", enchantedString);

        originalString = String.format("SELECT a.a,a.b FROM columns a %s WHERE %s", SqlStringWrapper.JOIN_KEYWORD, SqlStringWrapper.WHERE_KEYWORD);
        enchantedString = SqlStringWrapper.aclWrapper(
                originalString,
                "a.aclistid",
                "m.ownerid",
                ACPermission.permREAD);
        Assert.assertEquals(enchantedString,
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND (:userid=me.member_id OR m.ownerid=:userid)"
        );

        originalString = String.format("SELECT a.a,a.b FROM columns a %s WHERE %s GROUP BY a.x", SqlStringWrapper.JOIN_KEYWORD, SqlStringWrapper.WHERE_KEYWORD);
        enchantedString = SqlStringWrapper.aclWrapper(originalString, "a.aclistid", "m.ownerid", ACPermission.permREAD);
        Assert.assertEquals(
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace "
                + "ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND (:userid=me.member_id OR m.ownerid=:userid) "
                + "GROUP BY a.x", enchantedString);

        originalString = String.format("SELECT a.a,a.b FROM columns a %s WHERE %s AND a=0 GROUP BY a.x", SqlStringWrapper.JOIN_KEYWORD, SqlStringWrapper.WHERE_KEYWORD);
        enchantedString = SqlStringWrapper.aclWrapper(originalString, "a.aclistid", "m.ownerid", ACPermission.permREAD, ACPermission.permEDIT);
        Assert.assertEquals(
                "SELECT a.a,a.b "
                + "FROM columns a "
                + "JOIN acentries ace ON ace.aclist_id=a.aclistid "
                + "JOIN memberships me ON ace.member_id=me.group_id "
                + "WHERE ace.permread=true "
                + "AND ace.permedit=true "
                + "AND (:userid=me.member_id OR m.ownerid=:userid) "
                + "AND a=0 "
                + "GROUP BY a.x", enchantedString);

    }
}
