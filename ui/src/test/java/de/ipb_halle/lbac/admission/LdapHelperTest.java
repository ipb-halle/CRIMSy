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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.InfoObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * NOTE: This test runs NOT within an Arquillian context and therefore ignores
 * the @PostConstruct annotation in class LdapProperties (which otherwise would
 * override the LDAP_GROUP_FILTER_DN setting unless it is saved here).
 *
 * @author fmauz
 */
public class LdapHelperTest {

    private LdapHelper instance;

    public LdapHelperTest() {

        InfoObject ie = new InfoObject(
                "LDAP_GROUP_FILTER_DN",
                "OU=BioactivesCloud,OU=group,DC=ipb-halle,DC=de");
        HashMap<String, Integer> map = new HashMap<>();
        map.put("LDAP_GROUP_FILTER_DN", 0);
        List<InfoObject> ieList = new ArrayList<>();
        ieList.add(ie);

        LdapProperties props = new LdapProperties(ieList, map);
        instance = new LdapHelper();
        instance.setLdapProperties(props);

    }

    @Test
    public void filterGroupTest() {
        boolean b = instance.filterGroup(
                "CN=LBAC_User,OU=BioactivesCloud,OU=group,DC=ipb-halle,DC=de");
        Assert.assertTrue(b);

        b = instance.filterGroup(
                "CN=Irrelevant_Group,OU=SomeOU,DC=ipb-halle,DC=de");
        Assert.assertFalse(b);
    }
}
